/*-
 * Copyright (c) 2013-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.sisu.launch.InjectedTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.utils.AtomicFileCounter;

/**
 * @author Michael Simacek
 */
public class BisectResolverTest
    extends InjectedTest
{
    private AtomicFileCounter counter;

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        Path counterPath = Paths.get( "target/test-work/bisect-counter" );
        Files.createDirectories( counterPath.getParent() );
        System.setProperty( "xmvn.bisect.counter", counterPath.toString() );
        counter = new AtomicFileCounter( counterPath.toString(), 1000 );
    }

    @After
    @Override
    public void tearDown()
        throws Exception
    {
        System.clearProperty( "xmvn.bisect.counter" );

        super.tearDown();
    }

    /**
     * Test bisection resolution of artifact with no corresponding file.
     * 
     * @throws Exception
     */
    @Test
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
        assertNull( result.getArtifactPath() );
        assertEquals( 999, counter.getValue() );
    }
}
