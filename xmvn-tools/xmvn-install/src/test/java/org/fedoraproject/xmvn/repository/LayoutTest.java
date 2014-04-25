/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class LayoutTest
    extends InjectedTest
{
    private Repository mavenRepository;

    private Repository jppRepository;

    private Repository flatRepository;

    private Repository getRepositoryInstance( String type )
    {
        RepositoryFactory factory = lookup( RepositoryFactory.class, type );
        assertNotNull( factory );

        Xpp3Dom filter = new Xpp3Dom( "filter" );
        Properties properties = new Properties();
        Xpp3Dom configuration = new Xpp3Dom( "configuration" );

        Repository repository = factory.getInstance( filter, properties, configuration );
        assertNotNull( repository );

        return repository;
    }

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        mavenRepository = getRepositoryInstance( "maven" );
        jppRepository = getRepositoryInstance( "jpp" );
        flatRepository = getRepositoryInstance( "flat" );
    }

    /**
     * Make sure there is no default repository factory.
     * 
     * @throws Exception
     */
    public void defaultRepositoryFactoryTest()
        throws Exception
    {
        RepositoryFactory defaultRepositoryfFactory = lookup( RepositoryFactory.class );

        assertNull( defaultRepositoryfFactory );
    }

    private void testPaths( Repository repository, Artifact artifact, String expected )
    {
        ArtifactContext context = new ArtifactContext( artifact );
        String actual = repository.getPrimaryArtifactPath( artifact, context ).toString();
        assertEquals( expected, actual );
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
