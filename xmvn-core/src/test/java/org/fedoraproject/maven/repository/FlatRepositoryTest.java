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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.RepositoryConfigurator;

/**
 * @author Mikolaj Izdebski
 */
public class FlatRepositoryTest
    extends PlexusTestCase
{
    public void testJppRepository()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        Configuration configuration = configurator.getConfiguration();

        Repository repository = new Repository();
        repository.setId( "test123" );
        repository.setType( "flat" );
        configuration.addRepository( repository );

        RepositoryConfigurator repoConfigurator = lookup( RepositoryConfigurator.class );
        org.fedoraproject.maven.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        assertNotNull( repo );

        Artifact artifact = new DefaultArtifact( "JPP/foo.bar-bazz:the-artifact:baz:1.2.3" );
        assertEquals( Paths.get( "JPP.foo.bar-bazz-the-artifact-1.2.3.baz" ),
                      repo.getPrimaryArtifactPath( artifact ).getPath() );
        assertEquals( Paths.get( "JPP.foo.bar-bazz-the-artifact.baz" ),
                      repo.getPrimaryArtifactPath( artifact.setVersion( "SYSTEM" ) ).getPath() );
    }
}
