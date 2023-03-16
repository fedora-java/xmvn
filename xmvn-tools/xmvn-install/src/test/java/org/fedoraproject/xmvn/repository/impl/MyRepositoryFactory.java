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
package org.fedoraproject.xmvn.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
public class MyRepositoryFactory
    implements RepositoryFactory
{
    @Override
    public Repository getInstance( Element filter, Properties properties, Element configuration )
    {
        assertNotNull( properties );
        assertEquals( "bar", properties.get( "foo" ) );
        assertNull( properties.get( "baz" ) );

        return new MyRepository();
    }

    @Override
    public Repository getInstance( Element filter, Properties properties, Element configuration, String namespace )
    {
        fail( "getInstance was not expected to be called" );
        return null;
    }
}
