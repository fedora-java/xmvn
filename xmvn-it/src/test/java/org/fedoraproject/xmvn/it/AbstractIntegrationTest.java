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
package org.fedoraproject.xmvn.it;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Abstract base class for all integration tests.
 *
 * @author Mikolaj Izdebski
 */
public abstract class AbstractIntegrationTest {
    public static final String STDOUT = "stdout.txt";
    public static final String STDERR = "stderr.txt";

    private Path workDir;
    private Path saveDir;
    private Path mavenHome;
    private Path resourcesDir;
    private Path dependencyDir;
    private boolean printOutput;

    public Path getWorkDir() {
        return workDir;
    }

    public Path getMavenHome() {
        return mavenHome;
    }

    public Path getResourcesDir() {
        return resourcesDir;
    }

    public Path getDependencyDir() {
        return dependencyDir;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    public void expandBaseDir(String source, String target) throws Exception {
        String metadata = new String(Files.readAllBytes(Path.of(source)), StandardCharsets.UTF_8);
        metadata = metadata.replaceAll("@\\{xmvn.it.workDir}", workDir.toString());
        metadata = metadata.replaceAll("@\\{xmvn.it.resourcesDir}", resourcesDir.toString());
        metadata = metadata.replaceAll("@\\{xmvn.it.dependencyDir}", dependencyDir.toString());
        Files.write(Path.of(target), metadata.getBytes(StandardCharsets.UTF_8));
    }

    public void expandBaseDirInPlace(String sourceAndTarget) throws Exception {
        expandBaseDir(sourceAndTarget, sourceAndTarget);
    }

    @BeforeEach
    public void initParams(TestInfo testInfo) throws Exception {
        String testName = testInfo.getTestMethod().get().getName();

        String value = System.getProperty("xmvn.it.workDir");
        if (value == null) {
            throw new IllegalArgumentException("Property xmvn.it.workDir must be set");
        }
        workDir = Path.of(value);
        if (!Files.isDirectory(workDir)) {
            throw new IllegalArgumentException(
                    "Property xmvn.it.workDir points to a non-existent directory " + workDir);
        }

        Path cwd = Path.of(".").toRealPath();
        if (!cwd.equals(workDir)) {
            throw new RuntimeException(
                    "XMvn integration tests must be ran from "
                            + workDir
                            + " directory, but CWD was "
                            + cwd);
        }

        saveDir = Path.of(getTestProperty("xmvn.it.saveDir")).resolve(testName);

        mavenHome = Path.of(getTestProperty("xmvn.it.mavenHome"));
        if (!Files.isDirectory(mavenHome)) {
            throw new IllegalStateException(
                    "Directory pointed to by xmvn.it.mavenHome property does not exist: "
                            + mavenHome);
        }

        resourcesDir = Path.of(getTestProperty("xmvn.it.resourcesDir"));

        dependencyDir = Path.of(getTestProperty("xmvn.it.dependencyDir"));
        if (!Files.isDirectory(dependencyDir)) {
            throw new IllegalStateException(
                    "Directory pointed to by xmvn.it.dependencyDir property does not exist: "
                            + dependencyDir);
        }

        printOutput = Boolean.valueOf(getTestProperty("xmvn.it.printOutput"));

        Files.createDirectories(saveDir);
        delete(saveDir);

        Files.createDirectories(workDir);
        delete(workDir);

        Path workDirTemplate = resourcesDir.resolve(testName);
        if (Files.isDirectory(workDirTemplate, LinkOption.NOFOLLOW_LINKS)) {
            copy(workDirTemplate, workDir);
        }

        expandBaseDir(resourcesDir + "/metadata.xml", "metadata.xml");
    }

    @AfterEach
    public void saveBaseDir() throws Exception {
        copy(workDir, saveDir);
        delete(workDir);
    }

    private void delete(Path path) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path child : ds) {
                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    delete(child);
                }

                Files.deleteIfExists(child);
            }
        }
    }

    private void copy(Path source, Path target) throws Exception {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(source)) {
            for (Path child : ds) {
                Path targetChild = target.resolve(child.getFileName());
                Files.copy(child, targetChild);

                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    copy(child, targetChild);
                }
            }
        }
    }

    public Stream<String> getStdout() throws Exception {
        return Files.lines(workDir.resolve(STDOUT));
    }

    public Stream<String> getStderr() throws Exception {
        return Files.lines(workDir.resolve(STDERR));
    }

    public void assumeJavaVersionAtLeast(int minVersion) {
        int version = Integer.parseInt(System.getProperty("java.version").replaceAll("\\..*", ""));
        assumeTrue(version >= minVersion, "Java major version is at least " + minVersion);
    }

    public String getTestProperty(String name) throws IOException {
        try (InputStream is =
                AbstractIntegrationTest.class.getResourceAsStream("/xmvn-it.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            String value = properties.getProperty(name);
            if (value == null) {
                throw new IllegalArgumentException("Required property " + name + " was not set");
            }
            return value;
        }
    }
}
