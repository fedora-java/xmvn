/*-
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
public class LayoutTest
{
    private Repository mavenRepository;

    private Repository jppRepository;

    @Before
    public void setUp()
        throws Exception
    {
        mavenRepository = new MavenRepositoryFactory().getInstance( null, new Properties(), null );
        jppRepository = new JppRepositoryFactory().getInstance( null, new Properties(), null );

        assertNotNull( mavenRepository );
        assertNotNull( jppRepository );
    }

    private void testPaths( Repository repository, Artifact artifact, String expected )
    {
        ArtifactContext context = new ArtifactContext( artifact );
        Path repoPath = repository.getPrimaryArtifactPath( artifact, context,
                                                           artifact.getGroupId() + "/" + artifact.getArtifactId() );

        if ( expected == null )
        {
            assertNull( repoPath );
        }
        else
        {
            assertNotNull( repoPath );
            assertNotNull( repoPath );
            assertEquals( expected, repoPath.toString() );
        }
    }

    /**
     * Test layout objects.
     * 
     * @throws Exception
     */
    @Test
    public void testLayouts()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "an-example.artifact:used-FOR42.testing:ext-ens.ion:blah-1.2.3-foo" );

        testPaths( mavenRepository, artifact,
                   "an-example/artifact/used-FOR42.testing/blah-1.2.3-foo/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion" );
        testPaths( mavenRepository, artifact.setVersion( "SYSTEM" ), null );
        testPaths( jppRepository, artifact, "an-example.artifact/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion" );
        testPaths( jppRepository, artifact.setVersion( "SYSTEM" ),
                   "an-example.artifact/used-FOR42.testing.ext-ens.ion" );
    }

    /**
     * Test is JPP prefixes in groupId are handled correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testJppPrefixes()
        throws Exception
    {
        Artifact artifact1 = new DefaultArtifact( "JPP:testing:abc:1.2.3" );
        Artifact artifact2 = new DefaultArtifact( "JPP/group:testing:abc:1.2.3" );
        Artifact artifact3 = new DefaultArtifact( "JPP-group:testing:abc:1.2.3" );

        testPaths( jppRepository, artifact1.setVersion( "SYSTEM" ), "testing.abc" );
        testPaths( jppRepository, artifact2.setVersion( "SYSTEM" ), "group/testing.abc" );
        testPaths( jppRepository, artifact3.setVersion( "SYSTEM" ), "JPP-group/testing.abc" );
    }
}
