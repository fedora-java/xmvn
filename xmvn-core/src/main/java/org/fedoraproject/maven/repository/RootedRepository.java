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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = RootedRepository.ROLE_HINT )
public class RootedRepository
    implements Repository
{
    public static final String ROLE_HINT = "rooted";

    @Requirement
    private PlexusContainer container;

    private Path root;

    private Repository slave;

    @Override
    public Path getArtifactPath( Artifact artifact )
    {
        Path path = slave.getArtifactPath( artifact );
        return root.resolve( path );
    }

    @Override
    public void configure( Properties properties, Xpp3Dom configuration )
    {
        try
        {
            String slaveId = properties.getProperty( "slave" );
            slave = container.lookup( Repository.class, slaveId != null ? slaveId : "default" );
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }

        String rootPath = properties.getProperty( "root" );
        root = Paths.get( rootPath != null ? rootPath : "" );
    }
}
