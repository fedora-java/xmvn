/*-
 * Copyright (c) 2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo.install;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.junit.jupiter.api.Test;

class InstallMojoIntegrationTest extends AbstractMojoIntegrationTest {
    private static String id(ArtifactMetadata amd) {
        return amd.getArtifactId() + ":" + amd.getClassifier() + ":" + amd.getExtension();
    }

    @Test
    void testInstallMojo() throws Exception {
        performMojoTest("package", "install");
        Path reactorPath = Path.of(".xmvn-reactor");
        assertThat(reactorPath).isRegularFile();
        PackageMetadata pmd = PackageMetadata.readFromXML(reactorPath);
        assertThat(pmd.getArtifacts()).hasSize(6);

        var map = pmd.getArtifacts().stream().collect(Collectors.toMap(amd -> id(amd), amd -> amd));
        List<ArtifactMetadata> sortedList = new ArrayList<>(new TreeMap<>(map).values());
        Iterator<ArtifactMetadata> it = sortedList.iterator();

        assertThat(it).hasNext();
        ArtifactMetadata aj = it.next();
        assertThat(aj.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(aj.getArtifactId()).isEqualTo("install-attached");
        assertThat(aj.getExtension()).isEqualTo("jar");
        assertThat(aj.getClassifier()).isEqualTo("");
        assertThat(aj.getVersion()).isEqualTo("42");
        assertThat(Path.of(aj.getPath())).isRegularFile();
        assertThat(aj.getDependencies()).hasSize(1);

        assertThat(it).hasNext();
        ArtifactMetadata ap = it.next();
        assertThat(ap.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(ap.getArtifactId()).isEqualTo("install-attached");
        assertThat(ap.getExtension()).isEqualTo("pom");
        assertThat(ap.getClassifier()).isEqualTo("");
        assertThat(ap.getVersion()).isEqualTo("42");
        assertThat(Path.of(ap.getPath())).isRegularFile();
        assertThat(ap.getDependencies()).hasSize(1);

        assertThat(it).hasNext();
        ArtifactMetadata at = it.next();
        assertThat(at.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(at.getArtifactId()).isEqualTo("install-attached");
        assertThat(at.getExtension()).isEqualTo("jar");
        assertThat(at.getClassifier()).isEqualTo("tests");
        assertThat(at.getVersion()).isEqualTo("42");
        assertThat(Path.of(at.getPath())).isRegularFile();
        assertThat(at.getDependencies()).hasSize(1);

        assertThat(it).hasNext();
        ArtifactMetadata pp = it.next();
        assertThat(pp.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(pp.getArtifactId()).isEqualTo("install-parent");
        assertThat(pp.getExtension()).isEqualTo("pom");
        assertThat(pp.getClassifier()).isEqualTo("");
        assertThat(pp.getVersion()).isEqualTo("42");
        assertThat(Path.of(pp.getPath())).isRegularFile();
        assertThat(pp.getDependencies()).isEmpty();

        assertThat(it).hasNext();
        ArtifactMetadata sj = it.next();
        assertThat(sj.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(sj.getArtifactId()).isEqualTo("install-simple");
        assertThat(sj.getExtension()).isEqualTo("jar");
        assertThat(sj.getClassifier()).isEqualTo("");
        assertThat(sj.getVersion()).isEqualTo("42");
        assertThat(Path.of(sj.getPath())).isRegularFile();
        assertThat(sj.getDependencies()).isEmpty();

        assertThat(it).hasNext();
        ArtifactMetadata sp = it.next();
        assertThat(sp.getGroupId()).isEqualTo("xmvn.it.install.mojo");
        assertThat(sp.getArtifactId()).isEqualTo("install-simple");
        assertThat(sp.getExtension()).isEqualTo("pom");
        assertThat(sp.getClassifier()).isEqualTo("");
        assertThat(sp.getVersion()).isEqualTo("42");
        assertThat(Path.of(sp.getPath())).isRegularFile();
        assertThat(sp.getDependencies()).isEmpty();
    }
}
