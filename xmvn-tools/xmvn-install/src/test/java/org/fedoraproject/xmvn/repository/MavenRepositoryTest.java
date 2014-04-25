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
package org.fedoraproject.xmvn.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;

import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.Repository;

/**
 * @author Mikolaj Izdebski
 */
public class MavenRepositoryTest
    extends InjectedTest
{
    @Test
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
        org.fedoraproject.xmvn.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        assertNotNull( repo );

        Artifact artifact = new DefaultArtifact( "foo.bar:the-artifact:baz:1.2.3" );
        ArtifactContext context = new ArtifactContext();
        assertEquals( Paths.get( "foo/bar/the-artifact/1.2.3/the-artifact-1.2.3.baz" ),
                      repo.getPrimaryArtifactPath( artifact, context ).getPath() );
        assertNull( repo.getPrimaryArtifactPath( artifact.setVersion( "SYSTEM" ), context ) );
    }
}
