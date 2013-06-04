/*-
 * Copyright (c) 2013 Red Hat, Inc.
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

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;

/**
 * @author Mikolaj Izdebski
 */
public class BasicResolverTest
    extends PlexusTestCase
{
    /**
     * Test if Plexus can load resolver component.
     * 
     * @throws Exception
     */
    public void testComponentLookup()
        throws Exception
    {
        Resolver resolver = lookup( Resolver.class );
        assertNotNull( resolver );
    }

    /**
     * Test if resolver configuration is present and sane.
     * 
     * @throws Exception
     */
    public void testConfigurationExistance()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        assertNotNull( configurator );

        Configuration configuration = configurator.getDefaultConfiguration();
        assertNotNull( configuration );

        ResolverSettings settings = configuration.getResolverSettings();
        assertNotNull( settings );
    }

    /**
     * Test if resolver correctly fails to resolve nonexistent artifact.
     * 
     * @throws Exception
     */
    public void testResolutionFailure()
        throws Exception
    {
        Resolver resolver = lookup( Resolver.class );
        ResolutionRequest request = new ResolutionRequest( "some", "nonexistent", "artifact", "pom" );
        ResolutionResult result = resolver.resolve( request );
        assertNotNull( result );
        assertNull( result.getArtifactFile() );
    }

    /**
     * Test if resolver correctly fails to resolve nonexistent artifact using deprecated API.
     * 
     * @throws Exception
     */
    @Deprecated
    public void testResolutionFailureDeprecatedAPI()
        throws Exception
    {
        Resolver resolver = lookup( Resolver.class );
        File artifactFile = resolver.resolve( "some", "nonexistent", "artifact", "pom" );
        assertNull( artifactFile );
    }
}
