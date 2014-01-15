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
package org.fedoraproject.xmvn.config.impl;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.RepositoryConfigurator;
import org.fedoraproject.xmvn.config.Stereotype;
import org.fedoraproject.xmvn.repository.Repository;

import com.google.inject.Key;
import com.google.inject.name.Names;

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

    private final MutableBeanLocator locator;

    @Inject
    public DefaultRepositoryConfigurator( Configurator configurator, MutableBeanLocator locator )
    {
        this.configurator = configurator;
        this.locator = locator;
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

        List<Stereotype> stereotypes = desc.getStereotypes();

        Key<Repository> key = Key.get( Repository.class, Names.named( type ) );
        Repository repository = locator.locate( key ).iterator().next().getValue();
        repository.configure( stereotypes, properties, configurationXml );
        return repository;
    }
}
