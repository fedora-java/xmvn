/*-
 * Copyright (c) 2013-2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.test;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.config.impl.DefaultConfigurator;

/**
 * @author Mikolaj Izdebski
 */
public class TestConfigurator
    extends DefaultConfigurator
{
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
