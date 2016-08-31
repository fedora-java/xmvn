/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.utils.DomUtils;

/**
 * Factory creating compound repositories.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "compound" )
@Singleton
public class CompoundRepositoryFactory
    extends AbstractRepositoryFactory
{
    private final RepositoryConfigurator configurator;

    @Inject
    public CompoundRepositoryFactory( RepositoryConfigurator configurator )
    {
        this.configurator = configurator;
    }

    @Override
    public Repository getInstance( Element filter, Properties properties, Element configuration, String namespace )
    {
        Path prefix = null;
        if ( properties.containsKey( "prefix" ) )
            prefix = Paths.get( properties.getProperty( "prefix" ) );

        Element repositories = DomUtils.parseAsWrapper( configuration );
        if ( !repositories.getNodeName().equals( "repositories" ) )
        {
            throw new RuntimeException( "compound repository expects configuration "
                + "with exactly one child element: <repositories>" );
        }

        List<Repository> slaveRepositories = new ArrayList<>();
        for ( Element child : DomUtils.parseAsParent( repositories ) )
        {
            String text = DomUtils.parseAsText( child );
            if ( !child.getNodeName().equals( "repository" ) )
                throw new RuntimeException( "All children of <repositories> must be <repository> text nodes" );

            Repository slaveRepository = configurator.configureRepository( text, namespace );
            slaveRepositories.add( slaveRepository );
        }

        if ( Strings.isNullOrEmpty( namespace ) )
            namespace = properties.getProperty( "namespace", "" );

        return new CompoundRepository( namespace, prefix, slaveRepositories );
    }
}
