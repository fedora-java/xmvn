/*-
 * Copyright (c) 2014-2026 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.RegularFile;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Simacek
 */
class RegularFileTest extends AbstractFileTest {
    @Test
    void fileInstallationFromArray() throws Exception {
        Path jar = getResource("example.jar");
        byte[] content = Files.readAllBytes(jar);
        add(new Directory(Path.of("usr/share/java")));
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), content));
        Path root = performInstallation();
        assertDirectoryStructure(
                "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar");
        assertThat(root.resolve("usr/share/java/foobar.jar")).hasSameBinaryContentAs(jar);

        assertDescriptorEquals(
                "%attr(0755,root,root) %dir /usr/share/java",
                "%attr(0644,root,root) /usr/share/java/foobar.jar");
    }

    @Test
    void fileInstallationFromFile() throws Exception {
        Path jar = getResource("example.jar");
        add(new Directory(Path.of("usr/share/java")));
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), jar));
        Path root = performInstallation();
        assertDirectoryStructure(
                "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar");
        assertThat(root.resolve("usr/share/java/foobar.jar")).hasSameBinaryContentAs(jar);
        assertDescriptorEquals(
                "%attr(0755,root,root) %dir /usr/share/java",
                "%attr(0644,root,root) /usr/share/java/foobar.jar");
    }

    @Test
    void createParentDirectory() throws Exception {
        Path jar = getResource("example.jar");
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), jar));
        Path root = performInstallation();
        assertDirectoryStructure(
                "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar");
        assertThat(root.resolve("usr/share/java/foobar.jar")).hasSameBinaryContentAs(jar);
        assertDescriptorEquals("%attr(0644,root,root) /usr/share/java/foobar.jar");
    }

    @Test
    void nonexistentFile() throws Exception {
        add(new Directory(Path.of("usr/share/java")));
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), Path.of("not-here")));
        assertThatExceptionOfType(IOException.class).isThrownBy(this::performInstallation);
    }

    @Test
    void accessMode() throws Exception {
        Path jar = getResource("example.jar");
        add(new Directory(Path.of("usr/share/java")));
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), jar, 0666));
        Path root = performInstallation();
        assertDirectoryStructure(
                "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar");
        assertThat(root.resolve("usr/share/java/foobar.jar")).hasSameBinaryContentAs(jar);

        assertDescriptorEquals(
                "%attr(0755,root,root) %dir /usr/share/java",
                "%attr(0666,root,root) /usr/share/java/foobar.jar");
    }

    @Test
    void incorrectMode() throws Exception {
        Path jar = getResource("example.jar");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () ->
                                add(
                                        new RegularFile(
                                                Path.of("usr/share/java/foobar.jar"), jar, 01000)));
    }

    @Test
    void negativeMode() throws Exception {
        Path jar = getResource("example.jar");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () ->
                                add(
                                        new RegularFile(
                                                Path.of("usr/share/java/foobar.jar"), jar, -0644)));
    }

    @Test
    void absoluteTarget() throws Exception {
        Path jar = getResource("example.jar");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () ->
                                add(
                                        new RegularFile(
                                                Path.of("/usr/share/java/foobar.jar"),
                                                jar,
                                                01000)));
    }
}
