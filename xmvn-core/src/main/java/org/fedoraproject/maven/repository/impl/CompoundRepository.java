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
package org.fedoraproject.maven.repository.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.config.Stereotype;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryPath;

/**
 * Compound repository.
 * <p>
 * This repository aggregates zero or more other repositories. The repositories are ordered by preference.
 * <p>
 * All requests are forwarded to repositories backing this compound repository. If no repositories are aggregated then
 * this repository is equivalent to empty repository.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = "compound", instantiationStrategy = "per-lookup" )
public class CompoundRepository
    extends AbstractRepository
{
    @Requirement
    private RepositoryConfigurator configurator;

    private Path prefix;

    private final List<Repository> slaveRepositories = new ArrayList<>();

    @Override
    public void configure( List<Stereotype> stereotypes, Properties properties, Xpp3Dom configuration )
    {
        if ( properties.containsKey( "prefix" ) )
            prefix = Paths.get( properties.getProperty( "prefix" ) );

        if ( configuration.getChildCount() != 1 || !configuration.getChild( 0 ).getName().equals( "repositories" ) )
            throw new RuntimeException( "compound repository expects configuration "
                + "with exactly one child element: <repositories>" );
        configuration = configuration.getChild( 0 );

        for ( Xpp3Dom child : configuration.getChildren() )
        {
            if ( !child.getName().equals( "repository" ) || child.getChildCount() > 0 )
                throw new RuntimeException( "All childreen of <repositories> must be <repository> text nodes" );

            Repository slaveRepository = configurator.configureRepository( child.getValue().trim() );
            slaveRepositories.add( slaveRepository );
        }

        setNamespace( properties.getProperty( "namespace", "" ) );
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifacts, boolean ignoreType )
    {
        List<RepositoryPath> paths = new ArrayList<>();
        for ( Repository repository : slaveRepositories )
        {
            for ( RepositoryPath path : repository.getArtifactPaths( artifacts, ignoreType ) )
            {
                DefaultRepositoryPath newPath = new DefaultRepositoryPath( path );
                if ( prefix != null )
                    newPath.setPath( prefix.resolve( path.getPath() ) );
                paths.add( newPath );
            }
        }
        return Collections.unmodifiableList( paths );
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType )
    {
        Iterator<RepositoryPath> it = getArtifactPaths( artifact, ignoreType ).iterator();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public void setNamespace( String namespace )
    {
        super.setNamespace( namespace );

        if ( StringUtils.isNotEmpty( namespace ) )
        {
            for ( Repository slave : slaveRepositories )
                slave.setNamespace( namespace );
        }
    }
}
