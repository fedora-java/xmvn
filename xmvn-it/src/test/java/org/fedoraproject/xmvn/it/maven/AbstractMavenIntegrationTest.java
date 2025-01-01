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
package org.fedoraproject.xmvn.it.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.output.TeeOutputStream;
import org.fedoraproject.xmvn.it.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base class for integration tests that involve invoking XMvn (Maven with extensions).
 *
 * @author Mikolaj Izdebski
 */
public abstract class AbstractMavenIntegrationTest extends AbstractIntegrationTest {
    private boolean expectFailure;

    @BeforeEach
    public void expectSuccess() {
        expectFailure = false;
    }

    public void expectFailure() {
        expectFailure = true;
    }

    private URL[] getBootClasspath() throws IOException {
        Set<URL> bootClassPath = new LinkedHashSet<>();
        try (DirectoryStream<Path> dir =
                Files.newDirectoryStream(getMavenHome().resolve("boot"), "*.jar")) {
            for (Path jar : dir) {
                bootClassPath.add(jar.toUri().toURL());
            }
        }

        return bootClassPath.toArray(new URL[bootClassPath.size()]);
    }

    public void performTest(String... args) throws Exception {
        Deque<String> argList = new ArrayDeque<>(Arrays.asList(args));
        performTest(argList);
    }

    public void performTest(Deque<String> argList) throws Exception {
        argList.addFirst("--batch-mode");
        String[] args = argList.toArray(new String[argList.size()]);

        try (OutputStream saveStdout = Files.newOutputStream(getWorkDir().resolve(STDOUT));
                OutputStream saveStderr = Files.newOutputStream(getWorkDir().resolve(STDERR));
                OutputStream teeStdout = new TeeOutputStream(System.out, saveStdout);
                OutputStream teeStderr = new TeeOutputStream(System.err, saveStderr);
                PrintStream printSaveStdout = new PrintStream(saveStdout);
                PrintStream printSaveStderr = new PrintStream(saveStderr);
                PrintStream printTeeStdout = new PrintStream(teeStdout);
                PrintStream printTeeStderr = new PrintStream(teeStderr);
                PrintStream stdout = isPrintOutput() ? printTeeStdout : printSaveStdout;
                PrintStream stderr = isPrintOutput() ? printTeeStderr : printSaveStderr) {
            assertEquals(expectFailure ? 1 : 0, run(stdout, stderr, args));
        }

        assertFalse(getStderr().findAny().isPresent());

        if (expectFailure) {
            assertTrue(getStdout().anyMatch("[INFO] BUILD FAILURE"::equals));
        }
    }

    private int run(PrintStream out, PrintStream err, String... args) throws Exception {
        Properties originalProperties = System.getProperties();
        System.setProperties(null);
        System.setProperty("maven.home", getMavenHome().toString());
        System.setProperty("user.dir", getWorkDir().toString());
        System.setProperty("maven.multiModuleProjectDirectory", getWorkDir().toString());
        System.setProperty("xmvn.config.sandbox", "true");
        System.setProperty("xmvn.debug", "true");
        System.setProperty("xmvn.it.dep.api", getTestProperty("xmvn.it.dep.api"));
        System.setProperty("xmvn.it.dep.core", getTestProperty("xmvn.it.dep.core"));
        System.setProperty("xmvn.it.dep.connector", getTestProperty("xmvn.it.dep.connector"));

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader().getParent();
        try (URLClassLoader bootClassLoader =
                new URLClassLoader(getBootClasspath(), parentClassLoader)) {
            Thread.currentThread().setContextClassLoader(bootClassLoader);

            Class<?> launcherClass =
                    bootClassLoader.loadClass("org.codehaus.plexus.classworlds.launcher.Launcher");
            Object launcher = launcherClass.getConstructor().newInstance();

            try (InputStream config = Files.newInputStream(getResourcesDir().resolve("m2.conf"))) {
                launcherClass.getMethod("configure", InputStream.class).invoke(launcher, config);
            }

            Object classWorld = launcherClass.getMethod("getWorld").invoke(launcher);
            ClassLoader classRealm =
                    (ClassLoader) launcherClass.getMethod("getMainRealm").invoke(launcher);

            Class<?> cliClass = (Class<?>) launcherClass.getMethod("getMainClass").invoke(launcher);
            Object mavenCli =
                    cliClass.getConstructor(classWorld.getClass()).newInstance(classWorld);

            Thread.currentThread().setContextClassLoader(classRealm);
            return (int)
                    cliClass.getMethod(
                                    "doMain",
                                    String[].class,
                                    String.class,
                                    PrintStream.class,
                                    PrintStream.class)
                            .invoke(mavenCli, args, getWorkDir().toString(), out, err);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            System.setProperties(originalProperties);
        }
    }
}
