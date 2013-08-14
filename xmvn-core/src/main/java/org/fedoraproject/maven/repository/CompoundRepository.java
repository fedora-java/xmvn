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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.model.Artifact;

/**
 * Compound repository.
 * <p>
 * This repository aggregates zero or more other repositories. The repositories are ordered by preference.
 * <p>
 * All requests are forwarded to repositories backing this compound repository. If no repositories are aggregated then
 * this repository is equivalent to empty repository.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = CompoundRepository.ROLE_HINT )
public class CompoundRepository
    implements Repository
{
    public static final String ROLE_HINT = "compound";

    @Requirement
    private RepositoryConfigurator configurator;

    private final List<Repository> slaveRepositories = new ArrayList<>();

    @Override
    public void configure( Properties properties, Xpp3Dom configuration )
    {
        if ( configuration.getChildCount() != 1 || !configuration.getChild( 0 ).getName().equals( "repositories" ) )
            throw new RuntimeException( "compound repository expects configuration "
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
    public List<Path> getArtifactPaths( Artifact artifact )
    {
        List<Path> paths = new ArrayList<>();
        for ( Repository repository : slaveRepositories )
            paths.addAll( repository.getArtifactPaths( artifact ) );
        return Collections.unmodifiableList( paths );
    }

    @Override
    public Path getPrimaryArtifactPath( Artifact artifact )
    {
        Iterator<Path> it = getArtifactPaths( artifact ).iterator();
        return it.hasNext() ? it.next() : null;
    }
}
