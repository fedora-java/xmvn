/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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
import java.util.Properties;

import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepositoryFactory
    extends AbstractRepositoryFactory
{
    protected abstract Repository newInstance( String namespace, Path root, Element filter, String suffix );

    @Override
    public Repository getInstance( Element filter, Properties properties, Element configuration, String namespace )
    {
        String rootProperty = properties.getProperty( "root" );
        Path root = rootProperty != null ? Paths.get( rootProperty ) : null;

        if ( namespace == null || namespace.isEmpty() )
            namespace = properties.getProperty( "namespace", "" );

        String suffix = properties.getProperty( "suffix", "" );

        return newInstance( namespace, root, filter, suffix );
    }
}
