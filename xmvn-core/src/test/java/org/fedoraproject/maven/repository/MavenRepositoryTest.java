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
package org.fedoraproject.maven.repository;

import java.nio.file.Paths;

import org.codehaus.plexus.PlexusTestCase;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.model.ArtifactImpl;

/**
 * @author Mikolaj Izdebski
 */
public class MavenRepositoryTest
    extends PlexusTestCase
{
    public void testMavenRepository()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        Configuration configuration = configurator.getConfiguration();

        Repository repository = new Repository();
        repository.setId( "test123" );
        repository.setType( "maven" );
        configuration.addRepository( repository );

        RepositoryConfigurator repoConfigurator = lookup( RepositoryConfigurator.class );
        org.fedoraproject.maven.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        assertNotNull( repo );

        ArtifactImpl artifact = new ArtifactImpl( "foo.bar", "the-artifact", "1.2.3", "baz" );
        assertEquals( Paths.get( "foo/bar/the-artifact/1.2.3/the-artifact-1.2.3.baz" ),
                      repo.getPrimaryArtifactPath( artifact ) );
        assertNull( repo.getPrimaryArtifactPath( artifact.clearVersion() ) );
    }
}
