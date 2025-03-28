/*-
 * Copyright (c) 2015-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.commons.io.output.TeeOutputStream;
import org.fedoraproject.xmvn.it.AbstractIntegrationTest;

/**
 * Abstract base class for integration tests that involve invoking XMvn tools (resolve, install,
 * subst).
 *
 * @author Mikolaj Izdebski
 */
public abstract class AbstractToolIntegrationTest extends AbstractIntegrationTest {
    private Path getToolLibDir(String tool) {
        String subDir;
        if ("xmvn-install".equals(tool)) {
            subDir = "installer";
        } else if ("xmvn-resolve".equals(tool)) {
            subDir = "resolver";
        } else {
            subDir = tool.replaceAll("^xmvn-", "");
        }

        Path libDir = getMavenHome().resolve("lib").resolve(subDir);
        assertThat(Files.isDirectory(libDir, LinkOption.NOFOLLOW_LINKS)).isTrue();
        return libDir;
    }

    private Path getJar(String tool, String glob) throws Exception {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(getToolLibDir(tool), glob)) {
            Iterator<Path> it = ds.iterator();
            assertThat(it).as("JAR not found for glob: " + glob).hasNext();
            Path jar = it.next();
            assertThat(it.hasNext()).as("More than one JAR found for glob: " + glob).isFalse();
            assertThat(Files.isRegularFile(jar, LinkOption.NOFOLLOW_LINKS)).isTrue();
            return jar;
        }
    }

    private Path findToolJar(String tool) throws Exception {
        return getJar(tool, tool + "-*.jar");
    }

    /**
     * For XMvn JARs, replace path to JAR corresponding with path to target/classes so that
     * debugging works out of the box.
     *
     * @throws IOException
     */
    private Path jar2classes(Path jar) throws IOException {
        String jarName = jar.getFileName().toString();
        if (!jarName.startsWith("xmvn-")) {
            return jar;
        }
        String projectName = jarName.substring(5, 5 + jarName.substring(5).indexOf('-'));
        Path dep = Path.of(getTestProperty("xmvn.it.dep." + projectName));
        assertThat(Files.exists(dep, LinkOption.NOFOLLOW_LINKS)).isTrue();
        return dep;
    }

    private Attributes readManifest(Path jar) throws Exception {
        URL mfUrl = new URL("jar:" + jar.toUri().toURL() + "!/META-INF/MANIFEST.MF");
        try (InputStream is = mfUrl.openConnection().getInputStream()) {
            return new Manifest(is).getMainAttributes();
        }
    }

    public int invokeTool(String tool, String... args) throws Exception {
        return invokeToolWithInput("", tool, args);
    }

    public int invokeToolWithInput(String input, String tool, String... args) throws Exception {
        Path jar = findToolJar(tool);
        Attributes mf = readManifest(jar);
        List<URL> classPathList = new ArrayList<>();
        classPathList.add(jar2classes(jar).toUri().toURL());
        for (String cpJar : mf.getValue("Class-Path").split(" ")) {
            classPathList.add(jar2classes(getJar(tool, cpJar)).toUri().toURL());
        }
        URL[] classPath = classPathList.stream().toArray(URL[]::new);

        InputStream oldStdin = System.in;
        PrintStream oldStdout = System.out;
        PrintStream oldStderr = System.err;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Properties oldProperties = (Properties) System.getProperties().clone();

        System.setProperty("xmvn.config.sandbox", "true");

        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader().getParent();
        try (InputStream stdin =
                        new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII));
                OutputStream saveStdout = Files.newOutputStream(getWorkDir().resolve(STDOUT));
                OutputStream saveStderr = Files.newOutputStream(getWorkDir().resolve(STDERR));
                OutputStream teeStdout = new TeeOutputStream(System.out, saveStdout);
                OutputStream teeStderr = new TeeOutputStream(System.err, saveStderr);
                PrintStream printSaveStdout = new PrintStream(saveStdout);
                PrintStream printSaveStderr = new PrintStream(saveStderr);
                PrintStream printTeeStdout = new PrintStream(teeStdout);
                PrintStream printTeeStderr = new PrintStream(teeStderr);
                PrintStream stdout = isPrintOutput() ? printTeeStdout : printSaveStdout;
                PrintStream stderr = isPrintOutput() ? printTeeStderr : printSaveStderr;
                URLClassLoader toolClassLoader = new URLClassLoader(classPath, parentClassLoader)) {
            Thread.currentThread().setContextClassLoader(toolClassLoader);
            System.setIn(stdin);
            System.setOut(stdout);
            System.setErr(stderr);

            Class<?> mainClass = toolClassLoader.loadClass(mf.getValue("Main-Class"));
            return (Integer)
                    mainClass.getMethod("doMain", String[].class).invoke(null, (Object) args);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            System.setIn(oldStdin);
            System.setOut(oldStdout);
            System.setErr(oldStderr);
            System.setProperties(oldProperties);
        }
    }
}
