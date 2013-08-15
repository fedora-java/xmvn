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
package org.fedoraproject.maven.config;

import java.util.Properties;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = RepositoryConfigurator.class )
public class DefaultRepositoryConfigurator
    implements RepositoryConfigurator
{
    @Requirement
    private Configurator configurator;

    @Requirement
    private PlexusContainer container;

    @Requirement
    private Logger logger;

    @Override
    public Repository configureRepository( String repoId )
    {
        Properties properties = null;
        Xpp3Dom configurationXml = null;
        String type = null;

        for ( org.fedoraproject.maven.config.Repository repository : configurator.getConfiguration().getRepositories() )
        {
            if ( repository.getId() != null && repository.getId().equals( repoId ) )
            {
                properties = repository.getProperties();
                configurationXml = (Xpp3Dom) repository.getConfiguration();
                type = repository.getType();
                break;
            }
        }

        if ( properties == null )
            properties = new Properties();
        if ( configurationXml == null )
            configurationXml = new Xpp3Dom( "configuration" );
        if ( type == null )
            type = repoId;

        try
        {
            Repository repository = container.lookup( Repository.class, type );
            repository.configure( properties, configurationXml );
            return repository;
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( "Unable to lookup implementation for repository type '" + type + "'", e );
        }
    }
}
