/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.maven.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.utils.AtomicFileCounter;

/**
 * @author Michael Simacek
 */
public class BisectResolverTest
    extends PlexusTestCase
{
    private AtomicFileCounter counter;

    @Override
    public void setUp()
        throws IOException
    {
        Path counterPath = Paths.get( "target/test-work/bisect-counter" );
        Files.createDirectories( counterPath.getParent() );
        System.setProperty( "xmvn.bisect.counter", counterPath.toString() );
        counter = new AtomicFileCounter( counterPath.toString(), 1000 );
    }

    @Override
    public void tearDown()
    {
        System.clearProperty( "xmvn.bisect.counter" );
    }

    /**
     * Test bisection resolution of artifact with no corresponding file.
     * 
     * @throws Exception
     */
    public void testBisectResolverNoFile()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        Configuration configuration = configurator.getConfiguration();

        Repository repository = new Repository();
        repository.setId( "bisect" );
        repository.setType( "flat" );
        configuration.addRepository( repository );

        Artifact artifact = new DefaultArtifact( "foo:bar:1.2" );

        Resolver resolver = lookup( Resolver.class );
        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolver.resolve( request );

        assertNotNull( result );
        assertNull( result.getArtifactFile() );
        assertEquals( 999, counter.getValue() );
    }
}
