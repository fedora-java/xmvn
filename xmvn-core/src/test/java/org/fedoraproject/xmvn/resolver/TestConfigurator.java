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
package org.fedoraproject.xmvn.resolver;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.ConfigurationMerger;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.config.impl.DefaultConfigurator;

/**
 * @author Mikolaj Izdebski
 */
@Named( "default" )
@Singleton
public class TestConfigurator
    extends DefaultConfigurator
{
    @Inject
    public TestConfigurator( ConfigurationMerger merger )
    {
        super( merger );
    }

    @Override
    public synchronized Configuration getConfiguration()
    {
        Configuration configuration = getDefaultConfiguration();

        Repository repository = new Repository();
        repository.setId( "resolve" );
        repository.setType( "maven" );
        repository.addProperty( "root", "/some/nonexistent/path" );
        configuration.addRepository( repository );

        return configuration;
    }
}