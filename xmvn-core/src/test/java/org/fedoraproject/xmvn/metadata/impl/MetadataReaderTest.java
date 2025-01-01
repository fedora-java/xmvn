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
package org.fedoraproject.xmvn.metadata.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class MetadataReaderTest extends AbstractTest {
    private DefaultMetadataResolver reader;

    @BeforeEach
    public void setUp() {
        reader = new DefaultMetadataResolver(locator);
    }

    /**
     * Test if trying to read metadata from empty list of directories returns empty result.
     *
     * @throws Exception
     */
    @Test
    public void testReadingEmptyList() throws Exception {
        List<String> pathList = List.of();
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    /**
     * Test if trying to read metadata from empty directory returns empty result.
     *
     * @throws Exception
     */
    @Test
    public void testReadingEmptyDirectory() throws Exception {
        Path dir = Files.createTempDirectory("xmvn-test");
        List<String> pathList = List.of(dir.toString());
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    /**
     * Test reading metadata from a file.
     *
     * @throws Exception
     */
    @Test
    public void testMetadata1() throws Exception {
        testMetadata1("metadata1");
    }

    /**
     * Test reading metadata from a file (with explicit namespace).
     *
     * @throws Exception
     */
    @Test
    public void testMetadata1WithNamespace() throws Exception {
        testMetadata1("metadata1-ns");
    }

    private void testMetadata1(String fileName) throws Exception {
        List<String> pathList = List.of("src/test/resources/" + fileName + ".xml");
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertNotNull(map);
        assertEquals(1, map.size());

        PackageMetadata pm = map.values().iterator().next();

        assertNotNull(pm.getProperties());
        assertEquals(1, pm.getProperties().size());
        assertEquals("key", pm.getProperties().keySet().iterator().next());
        assertEquals("value", pm.getProperties().values().iterator().next());

        assertNotNull(pm.getArtifacts());
        assertEquals(1, pm.getArtifacts().size());
        ArtifactMetadata am = pm.getArtifacts().iterator().next();

        assertEquals("gid", am.getGroupId());
        assertEquals("aid", am.getArtifactId());
        assertEquals("ext", am.getExtension());
        assertEquals("cla", am.getClassifier());
        assertEquals("ver", am.getVersion());
        assertEquals("/foo/bar", am.getPath());
        assertEquals("myscl10", am.getNamespace());

        assertNotNull(am.getProperties());
        assertEquals(1, am.getProperties().size());
        assertEquals("key1", am.getProperties().keySet().iterator().next());
        assertEquals("value1", am.getProperties().values().iterator().next());

        assertNotNull(am.getCompatVersions());
        assertEquals(1, am.getCompatVersions().size());
        assertEquals("1.2-beta3", am.getCompatVersions().iterator().next());

        assertNotNull(am.getAliases());
        assertEquals(1, am.getAliases().size());
        ArtifactAlias alias = am.getAliases().iterator().next();

        assertEquals("a-gid", alias.getGroupId());
        assertEquals("a-aid", alias.getArtifactId());
        assertEquals("a-ext", alias.getExtension());
        assertEquals("a-cla", alias.getClassifier());

        assertNotNull(am.getDependencies());
        assertEquals(1, am.getDependencies().size());
        Dependency dep = am.getDependencies().iterator().next();

        assertEquals("d-gid", dep.getGroupId());
        assertEquals("d-aid", dep.getArtifactId());
        assertEquals("d-ext", dep.getExtension());
        assertEquals("d-cla", dep.getClassifier());
        assertEquals("1.2.3", dep.getRequestedVersion());
        assertEquals("4.5.6", dep.getResolvedVersion());
        assertEquals("xyzzy", dep.getNamespace());

        assertNotNull(dep.getExclusions());
        assertEquals(1, dep.getExclusions().size());
        DependencyExclusion exc = dep.getExclusions().iterator().next();

        assertEquals("e-gid", exc.getGroupId());
        assertEquals("e-aid", exc.getArtifactId());

        assertNotNull(pm.getSkippedArtifacts());
        assertEquals(1, pm.getSkippedArtifacts().size());
        SkippedArtifactMetadata skip = pm.getSkippedArtifacts().iterator().next();

        assertEquals("s-gid", skip.getGroupId());
        assertEquals("s-aid", skip.getArtifactId());
        assertEquals("s-ext", skip.getExtension());
        assertEquals("s-cla", skip.getClassifier());
    }

    @Test
    public void testSimpleMetadata() {
        List<String> pathList = List.of("src/test/resources/simple.xml");
        Map<Path, PackageMetadata> map = reader.readMetadata(pathList);
        assertNotNull(map);
        assertEquals(1, map.size());

        PackageMetadata pm = map.values().iterator().next();
        Iterator<ArtifactMetadata> iter = pm.getArtifacts().iterator();

        assertTrue(iter.hasNext());
        ArtifactMetadata am1 = iter.next();
        assertNotNull(am1);

        assertEquals("org.codehaus.plexus", am1.getGroupId());
        assertEquals("plexus-ant-factory", am1.getArtifactId());
        assertEquals("jar", am1.getExtension());
        assertEquals("", am1.getClassifier());
        assertEquals("1.0", am1.getVersion());
        assertEquals("ns", am1.getNamespace());
        assertEquals("/usr/share/java/plexus/ant-factory-1.0.jar", am1.getPath());

        List<String> compatVersions1 = am1.getCompatVersions();
        assertEquals(1, compatVersions1.size());
        assertEquals("1.0", compatVersions1.iterator().next());

        assertTrue(iter.hasNext());
        ArtifactMetadata am2 = iter.next();
        assertNotNull(am2);

        assertEquals("org.codehaus.plexus", am2.getGroupId());
        assertEquals("plexus-ant-factory", am2.getArtifactId());
        assertEquals("pom", am2.getExtension());
        assertEquals("", am2.getClassifier());
        assertEquals("1.0", am2.getVersion());
        assertEquals("ns", am2.getNamespace());
        assertEquals("/usr/share/maven-poms/JPP.plexus-ant-factory-1.0.pom", am2.getPath());

        List<String> compatVersions2 = am2.getCompatVersions();
        assertEquals(1, compatVersions2.size());
        assertEquals("1.0", compatVersions2.iterator().next());
    }
}
