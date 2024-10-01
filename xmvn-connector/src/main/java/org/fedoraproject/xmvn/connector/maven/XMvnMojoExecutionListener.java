/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.xmvn.connector.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Listens to various MOJO executions and captures useful information.
 *
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnMojoExecutionListener implements ResolutionListener {
    private static class MojoGoal {
        private final String groupId;

        private final String artifactId;

        private final String goal;

        MojoGoal(String groupId, String artifactId, String goal) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.goal = goal;
        }

        boolean equals(MojoExecution execution) {
            return execution.getGroupId().equals(groupId)
                    && execution.getArtifactId().equals(artifactId)
                    && execution.getGoal().equals(goal);
        }
    }

    private static final MojoGoal JAVADOC_AGGREGATE =
            new MojoGoal("org.apache.maven.plugins", "maven-javadoc-plugin", "aggregate");

    private static final MojoGoal MAVEN_COMPILE =
            new MojoGoal("org.apache.maven.plugins", "maven-compiler-plugin", "compile");

    private static final MojoGoal TYCHO_COMPILE = new MojoGoal("org.eclipse.tycho", "tycho-compiler-plugin", "compile");

    private static final MojoGoal XMVN_BUILDDEP = new MojoGoal("org.fedoraproject.xmvn", "xmvn-mojo", "builddep");

    private static final MojoGoal XMVN_JAVADOC = new MojoGoal("org.fedoraproject.xmvn", "xmvn-mojo", "javadoc");

    private MavenPluginManager mavenPluginManager;

    private LegacySupport legacySupport;

    private Path xmvnStateDir = Path.of(".xmvn");

    void setXmvnStateDir(Path xmvnStateDir) {
        this.xmvnStateDir = xmvnStateDir;
    }

    private Object dispatchBuildPluginManagerMethodCall(
            @SuppressWarnings("unused") Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(mavenPluginManager, args);

        if ("getConfiguredMojo".equals(method.getName())) {
            beforeMojoExecution(ret, (MojoExecution) args[2]);
        } else if ("releaseMojo".equals(method.getName())) {
            afterMojoExecution(
                    args[0], (MojoExecution) args[1], legacySupport.getSession().getCurrentProject());
        }

        return ret;
    }

    XMvnMojoExecutionListener() {}

    @Inject
    public XMvnMojoExecutionListener(
            BuildPluginManager buildPluginManager, MavenPluginManager mavenPluginManager, LegacySupport legacySupport) {
        this.mavenPluginManager = mavenPluginManager;
        this.legacySupport = legacySupport;

        Object proxy = Proxy.newProxyInstance(
                XMvnMojoExecutionListener.class.getClassLoader(),
                new Class<?>[] {MavenPluginManager.class},
                this::dispatchBuildPluginManagerMethodCall);
        trySetBeanProperty(buildPluginManager, "mavenPluginManager", proxy);
    }

    private final List<String[]> resolutions = new ArrayList<>();

    private static String getBeanProperty(Object bean, String... getterNames) {
        try {
            for (String getterName : getterNames) {
                for (Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                    try {
                        Method getter = clazz.getDeclaredMethod(getterName);
                        getter.setAccessible(true);
                        Object value = getter.invoke(bean);
                        if (value != null) {
                            return value.toString();
                        }
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
            return null;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get bean property", e);
        }
    }

    private static void trySetBeanProperty(Object bean, String fieldName, Object value) {
        try {
            for (Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(bean, value);
                    return;
                } catch (NoSuchFieldException e) {
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get bean property", e);
        }
    }

    private void createApidocsSymlink(Path javadocDir) {
        try {
            Path apidocsSymlink = xmvnStateDir.resolve("apidocs");

            if (!Files.exists(xmvnStateDir)) {
                Files.createDirectory(xmvnStateDir);
            }

            if (Files.isSymbolicLink(apidocsSymlink)) {
                Files.delete(apidocsSymlink);
            }

            if (Files.isDirectory(javadocDir)) {
                Files.createSymbolicLink(apidocsSymlink, javadocDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create apidocs symlink", e);
        }
    }

    private void setProjectProperty(MavenProject project, String key, String value) {
        Properties properties = new Properties();

        try {
            Path propertiesFile = xmvnStateDir.resolve("properties");

            if (!Files.exists(xmvnStateDir)) {
                Files.createDirectory(xmvnStateDir);
            }

            if (Files.exists(propertiesFile)) {
                try (InputStream stream = Files.newInputStream(propertiesFile)) {
                    properties.load(stream);
                }
            }

            String projectKey = project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion();
            properties.setProperty(projectKey + "/" + key, value);

            try (OutputStream stream = Files.newOutputStream(propertiesFile)) {
                properties.store(stream, "XMvn project properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to set project property", e);
        }
    }

    void afterMojoExecution(Object mojo, MojoExecution execution, MavenProject project) {
        if (JAVADOC_AGGREGATE.equals(execution) || XMVN_JAVADOC.equals(execution)) {
            String javadocDir = getBeanProperty(mojo, "getReportOutputDirectory", "getOutputDir");
            if (javadocDir != null) {
                createApidocsSymlink(Path.of(javadocDir));
            }
        } else if (MAVEN_COMPILE.equals(execution) || TYCHO_COMPILE.equals(execution)) {
            String target = getBeanProperty(mojo, "getRelease", "getReleaseLevel", "getTarget", "getTargetLevel");
            if (target != null) {
                setProjectProperty(project, "compilerTarget", target);
            }
        }
    }

    void beforeMojoExecution(Object mojo, MojoExecution execution) {
        // Disable doclint
        if (JAVADOC_AGGREGATE.equals(execution)) {
            // maven-javadoc-plugin < 3.0.0
            trySetBeanProperty(mojo, "additionalparam", "-Xdoclint:none");
            // maven-javadoc-plugin >= 3.0.0
            trySetBeanProperty(mojo, "additionalOptions", new String[] {"-Xdoclint:none"});
        } else if (XMVN_BUILDDEP.equals(execution)) {
            trySetBeanProperty(mojo, "resolutions", Collections.unmodifiableList(new ArrayList<>(resolutions)));
        }
    }

    @Override
    public void resolutionRequested(ResolutionRequest request) {
        // Nothing to do
    }

    @Override
    public void resolutionCompleted(ResolutionRequest request, ResolutionResult result) {
        if (result.getArtifactPath() != null) {
            String[] tuple =
                    new String[] {request.getArtifact().toString(), result.getCompatVersion(), result.getNamespace()};
            resolutions.add(tuple);
        }
    }
}
