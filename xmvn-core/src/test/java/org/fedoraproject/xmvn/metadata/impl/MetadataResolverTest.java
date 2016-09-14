/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataResolverTest
    extends InjectedTest
{
    @Inject
    private MetadataResolver metadataResolver;

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
}
