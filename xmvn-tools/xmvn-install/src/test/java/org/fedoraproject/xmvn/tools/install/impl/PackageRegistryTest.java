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

import java.nio.file.Path;
import java.util.Set;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.JavaPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Simacek
 */
class PackageRegistryTest {
    private InstallerSettings settings;

    private PackageRegistry registry;

    @BeforeEach
    void setUpRegistry() {
        settings = new InstallerSettings();
        settings.setMetadataDir("usr/share/maven-metadata");
        registry = new PackageRegistry(settings, "test-package");
    }

    @Test
    void defaultPackage() {
        JavaPackage pkg1 = registry.getPackageById(null);
        JavaPackage pkg2 = registry.getPackageById(null);
        assertThat(pkg2).isSameAs(pkg1);
        JavaPackage pkg3 = registry.getPackageById("");
        assertThat(pkg3).isSameAs(pkg2);
        assertThat(registry.getPackages()).hasSize(1);
    }

    @Test
    void metadata() throws Exception {
        JavaPackage pkg = registry.getPackageById(null);
        Set<File> files = pkg.getFiles();
        assertThat(files).hasSize(1);
        File metadataFile = files.iterator().next();
        assertThat(metadataFile.getTargetPath())
                .isEqualTo(Path.of("usr/share/maven-metadata/test-package.xml"));
        assertThat(pkg.getMetadata()).isNotNull();
    }

    @Test
    void nonDefault() throws Exception {
        JavaPackage pkg = registry.getPackageById("subpackage");
        Set<File> files = pkg.getFiles();
        assertThat(files).hasSize(1);
        File metadataFile = files.iterator().next();
        assertThat(metadataFile.getTargetPath())
                .isEqualTo(Path.of("usr/share/maven-metadata/test-package-subpackage.xml"));
    }

    @Test
    void multiple() throws Exception {
        registry.getPackageById(null);
        registry.getPackageById("subpackage");
        assertThat(registry.getPackages()).hasSize(2);
        assertThat(registry.getPackageById("__noinstall")).isNull();
        assertThat(registry.getPackages()).hasSize(2);
    }
}
