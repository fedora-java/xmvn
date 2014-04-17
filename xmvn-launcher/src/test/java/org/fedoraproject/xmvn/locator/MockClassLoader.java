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

package org.fedoraproject.xmvn.locator;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Michael Simacek
 */
class MockClassLoader extends ClassLoader {

    @Override
    public Class<?> loadClass( String string )
            throws ClassNotFoundException
    {
        return MockClassLoader.class;
    }

    @Override
    public URL getResource( String string )
    {
        try
        {
            return new URL( "http://example.com" );
        }
        catch ( MalformedURLException ex )
        {
            throw new RuntimeException();
        }
    }

}
