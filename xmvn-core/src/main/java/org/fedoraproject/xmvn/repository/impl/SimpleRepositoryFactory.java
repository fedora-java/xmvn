/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.xmvn.config.Stereotype;
import org.fedoraproject.xmvn.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepositoryFactory
    extends AbstractRepositoryFactory
{
    protected abstract Repository newInstance( String namespace, Path root, List<Stereotype> stereotypes );

    @Override
    public Repository getInstance( List<Stereotype> stereotypes, Properties properties, Xpp3Dom configuration,
                                   String namespace )
    {
        String rootProperty = properties.getProperty( "root" );
        Path root = rootProperty != null ? Paths.get( rootProperty ) : null;

        if ( StringUtils.isEmpty( namespace ) )
            namespace = properties.getProperty( "namespace", "" );

        return newInstance( namespace, root, stereotypes );
    }
}
