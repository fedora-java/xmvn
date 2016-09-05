/*-
 * Copyright (c) 2016 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class IsolatedXMvnServiceLocatorTest
{
    /**
     * @author Mikolaj Izdebski
     */
    public interface MockService
    {
        String sayHello();
    }

    private static class MockServiceLocator
    {
        private static volatile int callCount;

        @SuppressWarnings( "unused" )
        public static MockService getService( Class<MockService> role )
        {
            assertEquals( MockService.class, role );
            ++callCount;

            return new MockService()
            {
                @Override
                public String sayHello()
                {
                    return "hello";
                }
            };
        }

        public static int getCallCount()
        {
            return callCount;
        }

        public static void resetCallCount()
        {
            MockServiceLocator.callCount = 0;
        }
    }

    private static class MockClassLoader
        extends URLClassLoader
    {
        private static volatile int callCount;

        private final boolean found;

        public MockClassLoader( boolean found )
        {
            super( new URL[0] );
            this.found = found;
        }

        @Override
        public Class<?> loadClass( String name )
            throws ClassNotFoundException
        {
            if ( name.equals( "org.fedoraproject.xmvn.locator.XMvnServiceLocator" ) )
            {
                ++callCount;
                if ( !found )
                    throw new ClassNotFoundException( "foobar" );
                return MockServiceLocator.class;
            }

            return super.loadClass( name );
        }

        public static int getCallCount()
        {
            return callCount;
        }

        public static void resetCallCount()
        {
            MockClassLoader.callCount = 0;
        }

    }

    @Before
    public void setUp()
    {
        MockClassLoader.resetCallCount();
        MockServiceLocator.resetCallCount();
    }

    @Test
    public void testGetService()
        throws Exception
    {
        try ( URLClassLoader classLoader = new MockClassLoader( true ) )
        {
            IsolatedXMvnServiceLocator locator = new IsolatedXMvnServiceLocator( classLoader );
            MockService service = locator.getService( MockService.class );
            assertTrue( service instanceof MockService );
            assertEquals( 1, MockClassLoader.getCallCount() );
            assertEquals( 1, MockServiceLocator.getCallCount() );

            String response = service.sayHello();
            assertEquals( "hello", response );
        }
    }

    @Test
    public void testCNFE()
        throws Exception
    {
        try ( URLClassLoader classLoader = new MockClassLoader( false ) )
        {
            IsolatedXMvnServiceLocator locator = new IsolatedXMvnServiceLocator( classLoader );
            locator.getService( MockService.class );
            Assert.fail();
        }
        catch ( RuntimeException e )
        {
            assertNotNull( e.getCause() );
            assertEquals( "foobar", e.getCause().getMessage() );
            assertEquals( 1, MockClassLoader.getCallCount() );
        }
    }
}
