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
package org.fedoraproject.xmvn.metadata.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.SkippedArtifactMetadata;
import org.fedoraproject.xmvn.test.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class MetadataReaderTest extends AbstractTest {
    private DefaultMetadataResolver reader;

    @BeforeEach
    void setUp() {
        reader = new DefaultMetadataResolver(locator);
    }

    /**
     * Test if trying to read metadata from empty list of directories returns empty result.
     *
     * @throws Exception
     */
    @Test
    void readingEmptyList() throws Exception {
        List<String> pathList = List.of();
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertThat(map).isEmpty();
    }

    /**
     * Test if trying to read metadata from empty directory returns empty result.
     *
     * @throws Exception
     */
    @Test
    void readingEmptyDirectory() throws Exception {
        Path dir = Files.createTempDirectory("xmvn-test");
        List<String> pathList = List.of(dir.toString());
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertThat(map).isEmpty();
    }

    /**
     * Test reading metadata from a file.
     *
     * @throws Exception
     */
    @Test
    void metadata1() throws Exception {
        testMetadata1("metadata1");
    }

    /**
     * Test reading metadata from a file (with explicit namespace).
     *
     * @throws Exception
     */
    @Test
    void metadata1WithNamespace() throws Exception {
        testMetadata1("metadata1-ns");
    }

    private void testMetadata1(String fileName) throws Exception {
        List<String> pathList = List.of("src/test/resources/" + fileName + ".xml");
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertThat(map).hasSize(1);

        PackageMetadata pm = map.values().iterator().next();

        assertThat(pm.getProperties()).hasSize(1);
        assertThat(pm.getProperties().keySet().iterator().next()).isEqualTo("key");
        assertThat(pm.getProperties().values().iterator().next()).isEqualTo("value");

        assertThat(pm.getArtifacts()).hasSize(1);
        ArtifactMetadata am = pm.getArtifacts().iterator().next();

        assertThat(am.getGroupId()).isEqualTo("gid");
        assertThat(am.getArtifactId()).isEqualTo("aid");
        assertThat(am.getExtension()).isEqualTo("ext");
        assertThat(am.getClassifier()).isEqualTo("cla");
        assertThat(am.getVersion()).isEqualTo("ver");
        assertThat(am.getPath()).isEqualTo("/foo/bar");
        assertThat(am.getNamespace()).isEqualTo("myscl10");

        assertThat(am.getProperties()).hasSize(1);
        assertThat(am.getProperties().keySet().iterator().next()).isEqualTo("key1");
        assertThat(am.getProperties().values().iterator().next()).isEqualTo("value1");

        assertThat(am.getCompatVersions()).hasSize(1);
        assertThat(am.getCompatVersions().iterator().next()).isEqualTo("1.2-beta3");

        assertThat(am.getAliases()).hasSize(1);
        ArtifactAlias alias = am.getAliases().iterator().next();

        assertThat(alias.getGroupId()).isEqualTo("a-gid");
        assertThat(alias.getArtifactId()).isEqualTo("a-aid");
        assertThat(alias.getExtension()).isEqualTo("a-ext");
        assertThat(alias.getClassifier()).isEqualTo("a-cla");

        assertThat(am.getDependencies()).hasSize(1);
        Dependency dep = am.getDependencies().iterator().next();

        assertThat(dep.getGroupId()).isEqualTo("d-gid");
        assertThat(dep.getArtifactId()).isEqualTo("d-aid");
        assertThat(dep.getExtension()).isEqualTo("d-ext");
        assertThat(dep.getClassifier()).isEqualTo("d-cla");
        assertThat(dep.getRequestedVersion()).isEqualTo("1.2.3");
        assertThat(dep.getResolvedVersion()).isEqualTo("4.5.6");
        assertThat(dep.getNamespace()).isEqualTo("xyzzy");

        assertThat(dep.getExclusions()).hasSize(1);
        DependencyExclusion exc = dep.getExclusions().iterator().next();

        assertThat(exc.getGroupId()).isEqualTo("e-gid");
        assertThat(exc.getArtifactId()).isEqualTo("e-aid");

        assertThat(pm.getSkippedArtifacts()).hasSize(1);
        SkippedArtifactMetadata skip = pm.getSkippedArtifacts().iterator().next();

        assertThat(skip.getGroupId()).isEqualTo("s-gid");
        assertThat(skip.getArtifactId()).isEqualTo("s-aid");
        assertThat(skip.getExtension()).isEqualTo("s-ext");
        assertThat(skip.getClassifier()).isEqualTo("s-cla");
    }

    @Test
    void simpleMetadata() {
        List<String> pathList = List.of("src/test/resources/simple.xml");
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertThat(map).hasSize(1);

        PackageMetadata pm = map.values().iterator().next();
        Iterator<ArtifactMetadata> iter = pm.getArtifacts().iterator();

        assertThat(iter).hasNext();
        ArtifactMetadata am1 = iter.next();
        assertThat(am1).isNotNull();

        assertThat(am1.getGroupId()).isEqualTo("org.codehaus.plexus");
        assertThat(am1.getArtifactId()).isEqualTo("plexus-ant-factory");
        assertThat(am1.getExtension()).isEqualTo("jar");
        assertThat(am1.getClassifier()).isEqualTo("");
        assertThat(am1.getVersion()).isEqualTo("1.0");
        assertThat(am1.getNamespace()).isEqualTo("ns");
        assertThat(am1.getPath()).isEqualTo("/usr/share/java/plexus/ant-factory-1.0.jar");

        List<String> compatVersions1 = am1.getCompatVersions();
        assertThat(compatVersions1).hasSize(1);
        assertThat(compatVersions1.iterator().next()).isEqualTo("1.0");

        assertThat(iter).hasNext();
        ArtifactMetadata am2 = iter.next();
        assertThat(am2).isNotNull();

        assertThat(am2.getGroupId()).isEqualTo("org.codehaus.plexus");
        assertThat(am2.getArtifactId()).isEqualTo("plexus-ant-factory");
        assertThat(am2.getExtension()).isEqualTo("pom");
        assertThat(am2.getClassifier()).isEqualTo("");
        assertThat(am2.getVersion()).isEqualTo("1.0");
        assertThat(am2.getNamespace()).isEqualTo("ns");
        assertThat(am2.getPath()).isEqualTo("/usr/share/maven-poms/JPP.plexus-ant-factory-1.0.pom");

        List<String> compatVersions2 = am2.getCompatVersions();
        assertThat(compatVersions2).hasSize(1);
        assertThat(compatVersions2.iterator().next()).isEqualTo("1.0");
    }
}
