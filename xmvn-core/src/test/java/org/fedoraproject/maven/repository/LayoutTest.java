/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.repository;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class LayoutTest
    extends PlexusTestCase
{
    /**
     * Make sure there is no default repository.
     * 
     * @throws Exception
     */
    public void defaultRepositoryTest()
        throws Exception
    {
        Repository defaultRepository = lookup( Repository.class );

        assertNull( defaultRepository );
    }

    private void testPaths( Repository repository, Artifact artifact, String... result )
    {
        Set<String> expected = new TreeSet<>();
        Set<String> actual = new TreeSet<>();

        expected.addAll( Arrays.asList( result ) );
        for ( RepositoryPath path : repository.getArtifactPaths( artifact ) )
        {
            assertNotNull( path );
            actual.add( path.getPath().toString() );
        }

        assertEquals( expected, actual );
    }

    /**
     * Test layout objects.
     * 
     * @throws Exception
     */
    public void testLayouts()
        throws Exception
    {
        Repository mavenRepository = lookup( Repository.class, "maven" );
        Repository jppRepository = lookup( Repository.class, "jpp" );
        Repository flatRepository = lookup( Repository.class, "flat" );
        assertNotNull( mavenRepository );
        assertNotNull( jppRepository );
        assertNotNull( flatRepository );

        Artifact artifact = new DefaultArtifact( "an-example.artifact:used-FOR42.testing:ext-ens.ion:blah-1.2.3-foo" );

        testPaths( mavenRepository, artifact,
                   "an-example/artifact/used-FOR42.testing/blah-1.2.3-foo/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion" );
        testPaths( mavenRepository, artifact.setVersion( "SYSTEM" ) );
        testPaths( jppRepository, artifact, "an-example.artifact/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion" );
        testPaths( jppRepository, artifact.setVersion( "SYSTEM" ), "an-example.artifact/used-FOR42.testing.ext-ens.ion" );
        testPaths( flatRepository, artifact, "an-example.artifact-used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion" );
        testPaths( flatRepository, artifact.setVersion( "SYSTEM" ),
                   "an-example.artifact-used-FOR42.testing.ext-ens.ion" );
    }

    /**
     * Test is JPP prefixes in groupId are handled correctly.
     * 
     * @throws Exception
     */
    public void testJppPrefixes()
        throws Exception
    {
        Repository jppRepository = lookup( Repository.class, "jpp" );
        assertNotNull( jppRepository );

        Artifact artifact1 = new DefaultArtifact( "JPP:testing:abc:1.2.3" );
        Artifact artifact2 = new DefaultArtifact( "JPP/group:testing:abc:1.2.3" );
        Artifact artifact3 = new DefaultArtifact( "JPP-group:testing:abc:1.2.3" );

        testPaths( jppRepository, artifact1.setVersion( "SYSTEM" ), "testing.abc" );
        testPaths( jppRepository, artifact2.setVersion( "SYSTEM" ), "group/testing.abc" );
        testPaths( jppRepository, artifact3.setVersion( "SYSTEM" ), "JPP-group/testing.abc" );
    }
}
