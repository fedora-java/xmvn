/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.SkippedArtifactMetadata;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataReaderTest
{
    private MetadataReader reader;

    @Before
    public void setUp()
    {
        reader = new MetadataReader();
    }

    /**
     * Test if trying to read metadata from empty list of directories returns empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingEmptyList()
        throws Exception
    {
        List<String> pathList = Collections.<String> emptyList();
        List<PackageMetadata> list = reader.readMetadata( pathList );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }

    /**
     * Test if trying to read metadata from empty directory returns empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingEmptyDirectory()
        throws Exception
    {
        Path dir = Files.createTempDirectory( "xmvn-test" );
        List<String> pathList = Collections.singletonList( dir.toString() );
        List<PackageMetadata> list = reader.readMetadata( pathList );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }

    /**
     * Test if trying to read metadata from depmap file empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingDepmap()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/xml-depmap.xml" );
        List<PackageMetadata> list = reader.readMetadata( pathList );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }

    /**
     * Test read metadata from a file.
     * 
     * @throws Exception
     */
    @Test
    public void testMetadata1()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/metadata1.xml" );
        List<PackageMetadata> list = reader.readMetadata( pathList );
        assertNotNull( list );
        assertEquals( 1, list.size() );

        PackageMetadata pm = list.iterator().next();

        assertEquals( "p-uuid", pm.getUuid() );

        assertNotNull( pm.getProperties() );
        assertEquals( 1, pm.getProperties().size() );
        assertEquals( "key", pm.getProperties().keySet().iterator().next() );
        assertEquals( "value", pm.getProperties().values().iterator().next() );

        assertNotNull( pm.getArtifacts() );
        assertEquals( 1, pm.getArtifacts().size() );
        ArtifactMetadata am = pm.getArtifacts().iterator().next();

        assertEquals( "gid", am.getGroupId() );
        assertEquals( "aid", am.getArtifactId() );
        assertEquals( "ext", am.getExtension() );
        assertEquals( "cla", am.getClassifier() );
        assertEquals( "ver", am.getVersion() );
        assertEquals( "/foo/bar", am.getPath() );
        assertEquals( "myscl10", am.getNamespace() );
        assertEquals( "a-uuid", am.getUuid() );

        assertNotNull( am.getProperties() );
        assertEquals( 1, am.getProperties().size() );
        assertEquals( "key1", am.getProperties().keySet().iterator().next() );
        assertEquals( "value1", am.getProperties().values().iterator().next() );

        assertNotNull( am.getCompatVersions() );
        assertEquals( 1, am.getCompatVersions().size() );
        assertEquals( "1.2-beta3", am.getCompatVersions().iterator().next() );

        assertNotNull( am.getAliases() );
        assertEquals( 1, am.getAliases().size() );
        ArtifactAlias alias = am.getAliases().iterator().next();

        assertEquals( "a-gid", alias.getGroupId() );
        assertEquals( "a-aid", alias.getArtifactId() );
        assertEquals( "a-ext", alias.getExtension() );
        assertEquals( "a-cla", alias.getClassifier() );

        assertNotNull( am.getDependencies() );
        assertEquals( 1, am.getDependencies().size() );
        Dependency dep = am.getDependencies().iterator().next();

        assertEquals( "d-gid", dep.getGroupId() );
        assertEquals( "d-aid", dep.getArtifactId() );
        assertEquals( "d-ext", dep.getExtension() );
        assertEquals( "d-cla", dep.getClassifier() );
        assertEquals( "1.2.3", dep.getRequestedVersion() );
        assertEquals( "4.5.6", dep.getResolvedVersion() );
        assertEquals( "xyzzy", dep.getNamespace() );

        assertNotNull( dep.getExclusions() );
        assertEquals( 1, dep.getExclusions().size() );
        DependencyExclusion exc = dep.getExclusions().iterator().next();

        assertEquals( "e-gid", exc.getGroupId() );
        assertEquals( "e-aid", exc.getArtifactId() );

        assertNotNull( pm.getSkippedArtifacts() );
        assertEquals( 1, pm.getSkippedArtifacts().size() );
        SkippedArtifactMetadata skip = pm.getSkippedArtifacts().iterator().next();

        assertEquals( "s-gid", skip.getGroupId() );
        assertEquals( "s-aid", skip.getArtifactId() );
        assertEquals( "s-ext", skip.getExtension() );
        assertEquals( "s-cla", skip.getClassifier() );
    }
}
