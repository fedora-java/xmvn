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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = AggregatorRepository.ROLE_HINT )
public class AggregatorRepository
    implements Repository
{
    static final String ROLE_HINT = "aggregator";

    @Requirement
    private RepositoryConfigurator configurator;

    private final List<Repository> slaveRepositories = new ArrayList<>();

    @Override
    public void configure( Properties properties, Xpp3Dom configuration )
    {
        if ( configuration.getChildCount() != 1 || !configuration.getChild( 0 ).getName().equals( "repositories" ) )
            throw new RuntimeException( "aggregator repository expects configuration "
                + "with exactly one child element: <repositories>" );
        configuration = configuration.getChild( 0 );

        for ( Xpp3Dom child : configuration.getChildren() )
        {
            if ( child.getName().equals( "repository" ) || child.getChildCount() > 0 )
                throw new RuntimeException( "All childreen of <repositories> must be <repository> text nodes" );

            Repository slaveRepository = configurator.configureRepository( child.getValue().trim() );
            slaveRepositories.add( slaveRepository );
        }
    }

    @Override
    public Path getArtifactPath( Artifact artifact )
    {
        // FIXME
        return null;
    }
}
