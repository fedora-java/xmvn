/*-
 * Copyright (c) 2013-2018 Red Hat, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.Repository;

/**
 * @author Mikolaj Izdebski
 */
public class CustomRepositoryTest
{
    /**
     * Test if simple (non-composite) repository configuration works as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testCustomRepository()
        throws Exception
    {
        Configuration configuration = new Configuration();
        Repository repository = new Repository();
        repository.setId( "test123" );
        repository.setType( "my-type" );
        repository.addProperty( "foo", "bar" );
        configuration.addRepository( repository );

        Configurator configurator = EasyMock.createMock( Configurator.class );
        EasyMock.expect( configurator.getConfiguration() ).andReturn( configuration ).atLeastOnce();
        EasyMock.replay( configurator );

        DefaultRepositoryConfigurator repoConfigurator = new DefaultRepositoryConfigurator( configurator );
        repoConfigurator.addRepositoryFactory( "my-type", new MyRepositoryFactory() );
        org.fedoraproject.xmvn.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        EasyMock.verify( configurator );
        assertNotNull( repo );
        assertTrue( repo instanceof MyRepository );
    }
}
