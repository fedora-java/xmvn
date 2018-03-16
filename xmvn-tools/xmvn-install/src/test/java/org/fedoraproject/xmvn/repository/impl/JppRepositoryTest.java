/*-
 * Copyright (c) 2013-2018 Red Hat, Inc.
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

import java.nio.file.Paths;

import org.easymock.EasyMock;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;

/**
 * @author Mikolaj Izdebski
 */
public class JppRepositoryTest
{
    @Test
    public void testJppRepository()
        throws Exception
    {
        Configuration configuration = new Configuration();
        Repository repository = new Repository();
        repository.setId( "test123" );
        repository.setType( "jpp" );
        configuration.addRepository( repository );

        Configurator configurator = EasyMock.createMock( Configurator.class );
        EasyMock.expect( configurator.getConfiguration() ).andReturn( configuration ).atLeastOnce();
        EasyMock.replay( configurator );

        RepositoryConfigurator repoConfigurator = new DefaultRepositoryConfigurator( configurator );
        org.fedoraproject.xmvn.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        EasyMock.verify( configurator );
        assertNotNull( repo );

        Artifact artifact1 = new DefaultArtifact( "foo.bar", "the-artifact", "baz", "1.2.3" );
        ArtifactContext context1 = new ArtifactContext( artifact1 );
        assertEquals( Paths.get( "my-target/path/aid-1.2.3.baz" ),
                      repo.getPrimaryArtifactPath( artifact1, context1, "my-target/path/aid" ) );

        Artifact artifact2 = artifact1.setVersion( null );
        ArtifactContext context2 = new ArtifactContext( artifact2 );
        assertEquals( Paths.get( "my-target/path/aid.baz" ),
                      repo.getPrimaryArtifactPath( artifact2, context2, "my-target/path/aid" ) );
    }
}
