/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.Package;
import org.fedoraproject.xmvn.tools.install.RegularFile;
import org.fedoraproject.xmvn.tools.install.SymbolicLink;
import org.junit.jupiter.api.Test;

/**
 * @author msimacek
 */
public class PackageTest extends AbstractFileTest {
    private final Path jar = getResource("example.jar");

    @Test
    public void testSimplePackage() throws Exception {
        File jarfile = new RegularFile(Path.of("usr/share/java/foobar.jar"), jar);
        Package pkg = new Package("my-id");
        assertEquals("my-id", pkg.getId());
        pkg.addFile(jarfile);

        pkg.install(installRoot);
        assertDirectoryStructure(
                "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar");
        assertDescriptorEquals(pkg, "%attr(0644,root,root) /usr/share/java/foobar.jar");
    }

    @Test
    public void testMoreFiles() throws Exception {
        File dir = new Directory(Path.of("usr/share/java"));
        File jarfile = new RegularFile(Path.of("usr/share/java/foobar.jar"), jar, 0600);
        File link = new SymbolicLink(Path.of("usr/share/java/link.jar"), Path.of("foobar.jar"));
        Package pkg = new Package("my-id");
        assertEquals("my-id", pkg.getId());
        pkg.addFile(dir);
        pkg.addFile(jarfile);
        pkg.addFile(link);

        pkg.install(installRoot);
        assertDirectoryStructure(
                "D /usr",
                "D /usr/share",
                "D /usr/share/java",
                "F /usr/share/java/foobar.jar",
                "L /usr/share/java/link.jar");
        assertDescriptorEquals(
                pkg,
                "%attr(0755,root,root) %dir /usr/share/java",
                "%attr(0600,root,root) /usr/share/java/foobar.jar",
                "/usr/share/java/link.jar");
    }

    @Test
    public void testEmpty() throws Exception {
        Package pkg = new Package("my-id");

        pkg.install(installRoot);
        assertDirectoryStructure();
        assertDescriptorEquals(pkg);
    }

    @Test
    public void testSameFileTwice() throws Exception {
        File jarfile = new RegularFile(Path.of("usr/share/java/foobar.jar"), jar);
        Package pkg = new Package("my-id");
        assertEquals("my-id", pkg.getId());
        pkg.addFile(jarfile);
        assertThrows(IllegalArgumentException.class, () -> pkg.addFile(jarfile));
    }

    @Test
    public void testEquality() throws Exception {
        Package pkg = new Package("my-id");
        Package samePkg = new Package("my-id");
        Package anotherPkg = new Package("other-id");

        assertEquals(samePkg, pkg);
        assertNotEquals(pkg, anotherPkg);
        assertNotEquals(samePkg, anotherPkg);
    }
}
