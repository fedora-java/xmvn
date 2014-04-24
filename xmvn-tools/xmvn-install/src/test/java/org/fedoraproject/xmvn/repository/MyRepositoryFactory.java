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
package org.fedoraproject.xmvn.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author Mikolaj Izdebski
 */
@Named( "my-type" )
@Singleton
public class MyRepositoryFactory
    implements RepositoryFactory
{
    @Override
    public Repository getInstance( Xpp3Dom filter, Properties properties, Xpp3Dom configuration )
    {
        assertNotNull( properties );
        assertNotNull( configuration );
        assertEquals( "bar", properties.get( "foo" ) );
        assertNull( properties.get( "baz" ) );

        return new MyRepository();
    }

    @Override
    public Repository getInstance( Xpp3Dom filter, Properties properties, Xpp3Dom configuration, String namespace )
    {
        fail( "getInstance was not expected to be called" );
        return null;
    }
}
