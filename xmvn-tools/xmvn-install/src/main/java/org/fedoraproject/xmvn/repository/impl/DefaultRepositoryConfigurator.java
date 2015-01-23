/*-
 * Copyright (c) 2013-2015 Red Hat, Inc.
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

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.repository.RepositoryFactory;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultRepositoryConfigurator
    implements RepositoryConfigurator
{
    private final Configurator configurator;

    private final Map<String, RepositoryFactory> repositoryFactories;

    @Inject
    public DefaultRepositoryConfigurator( Configurator configurator, Map<String, RepositoryFactory> repositoryFactories )
    {
        this.configurator = configurator;
        this.repositoryFactories = repositoryFactories;
    }

    private org.fedoraproject.xmvn.config.Repository findDescriptor( String repoId )
    {
        for ( org.fedoraproject.xmvn.config.Repository repository : configurator.getConfiguration().getRepositories() )
            if ( repository.getId() != null && repository.getId().equals( repoId ) )
                return repository;

        return null;
    }

    @Override
    public Repository configureRepository( String repoId )
    {
        return configureRepository( repoId, "" );
    }

    @Override
    public Repository configureRepository( String repoId, String namespace )
    {
        org.fedoraproject.xmvn.config.Repository desc = findDescriptor( repoId );
        if ( desc == null )
            throw new RuntimeException( "Repository '" + repoId + "' is not configured." );

        Properties properties = desc.getProperties();

        Xpp3Dom configurationXml = (Xpp3Dom) desc.getConfiguration();
        if ( configurationXml == null )
            configurationXml = new Xpp3Dom( "configuration" );

        String type = desc.getType();
        if ( type == null )
            throw new RuntimeException( "Repository '" + repoId + "' has missing type." );

        Xpp3Dom filter = (Xpp3Dom) desc.getFilter();

        RepositoryFactory factory = repositoryFactories.get( type );
        if ( factory == null )
            throw new RuntimeException( "Unable to create repository of type '" + type + "': no suitable factory found" );

        return factory.getInstance( filter, properties, configurationXml );
    }
}
