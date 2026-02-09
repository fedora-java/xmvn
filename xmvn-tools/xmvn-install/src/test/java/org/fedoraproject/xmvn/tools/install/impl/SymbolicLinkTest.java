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

import java.nio.file.Files;
import java.nio.file.Path;
import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.RegularFile;
import org.fedoraproject.xmvn.tools.install.SymbolicLink;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Simacek
 */
class SymbolicLinkTest extends AbstractFileTest {
    @Test
    void symlink() throws Exception {
        Path jar = getResource("example.jar");
        add(new Directory(Path.of("usr/share/java")));
        add(new RegularFile(Path.of("usr/share/java/foobar.jar"), jar));
        add(new SymbolicLink(Path.of("usr/share/java/link.jar"), Path.of("foobar.jar")));
        Path root = performInstallation();
        assertDirectoryStructure(
                "D /usr",
                "D /usr/share",
                "D /usr/share/java",
                "F /usr/share/java/foobar.jar",
                "L /usr/share/java/link.jar");
        Path link = root.resolve(Path.of("usr/share/java/link.jar"));
        assertThat(link).isSymbolicLink();
        assertThat(Files.readSymbolicLink(link)).isEqualTo(Path.of("foobar.jar"));

        assertDescriptorEquals(
                "%attr(0755,root,root) %dir /usr/share/java",
                "%attr(0644,root,root) /usr/share/java/foobar.jar", "/usr/share/java/link.jar");
    }
}
