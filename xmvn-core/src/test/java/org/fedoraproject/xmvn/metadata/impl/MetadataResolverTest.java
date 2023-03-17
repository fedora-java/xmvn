/*-
 * Copyright (c) 2014-2023 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.test.AbstractTest;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataResolverTest
    extends AbstractTest
{
    private MetadataResolver metadataResolver;

    @BeforeEach
    public void setUp()
    {
        metadataResolver = new DefaultMetadataResolver( locator );
    }

    /**
     * Test if metadata resolution works for exact version.
     * 
     * @throws Exception
     */
    @Test
    public void testCompatExactVersion()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/metadata1.xml" );
        MetadataResult result = metadataResolver.resolveMetadata( new MetadataRequest( pathList ) );

        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", "1.2-beta3" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNotNull( am );
        assertEquals( "/foo/bar", am.getPath() );
    }

    /**
     * Test if metadata resolution does not work for inexact versions.
     * 
     * @throws Exception
     */
    @Test
    public void testCompatNonExactVersion()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/metadata1.xml" );
        MetadataResult result = metadataResolver.resolveMetadata( new MetadataRequest( pathList ) );

        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", "1.1" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNull( am );
    }

    /**
     * Test if metadata resolution works for exact version.
     * 
     * @throws Exception
     */
    @Test
    public void testNonCompatExactVersion()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/metadata1-non-compat.xml" );
        MetadataResult result = metadataResolver.resolveMetadata( new MetadataRequest( pathList ) );

        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", Artifact.DEFAULT_VERSION );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNotNull( am );
        assertEquals( "/foo/bar", am.getPath() );
    }

    /**
     * Test if metadata resolution does not work for inexact versions.
     * 
     * @throws Exception
     */
    @Test
    public void testNonCompatNonExactVersion()
        throws Exception
    {
        List<String> pathList = Collections.singletonList( "src/test/resources/metadata1-non-compat.xml" );
        MetadataResult result = metadataResolver.resolveMetadata( new MetadataRequest( pathList ) );

        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", "1.1" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNull( am );
    }

    @Test
    public void testRepositoryListedTwice()
        throws Exception
    {
        String path = "src/test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest( Arrays.asList( path, path ) );
        MetadataResult result = metadataResolver.resolveMetadata( request );

        Artifact artifact = new DefaultArtifact( "org.codehaus.plexus", "plexus-ant-factory", "1.0" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNotNull( am );
        assertEquals( "/usr/share/java/plexus/ant-factory-1.0.jar", am.getPath() );
    }

    @Test
    public void testRepositoryListedTwiceDifferentPaths()
        throws Exception
    {
        String path1 = "src/test/resources/simple.xml";
        String path2 = "src/test/../test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest( Arrays.asList( path1, path2 ) );
        MetadataResult result = metadataResolver.resolveMetadata( request );

        Artifact artifact = new DefaultArtifact( "org.codehaus.plexus", "plexus-ant-factory", "1.0" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNull( am );
    }

    @Test
    public void testAllowDuplicates()
        throws Exception
    {
        String path1 = "src/test/resources/simple.xml";
        String path2 = "src/test/../test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest( Arrays.asList( path1, path2 ) );
        assertTrue( request.isIgnoreDuplicates() );
        request.setIgnoreDuplicates( false );
        MetadataResult result = metadataResolver.resolveMetadata( request );

        Artifact artifact = new DefaultArtifact( "org.codehaus.plexus", "plexus-ant-factory", "1.0" );
        ArtifactMetadata am = result.getMetadataFor( artifact );

        assertNotNull( am );
        assertEquals( "/usr/share/java/plexus/ant-factory-1.0.jar", am.getPath() );
    }
}
