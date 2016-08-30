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
package org.fedoraproject.xmvn.locator;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Michael Simacek
 */
public class XMvnHomeClassLoaderTest
{
    protected final Path resourceDir = Paths.get( "src/test/resources" ).toAbsolutePath();

    protected Path jarPath = resourceDir.resolve( "example.jar" );

    @Test
    public void testHome()
        throws Exception
    {
        try (XMvnHomeClassLoader loader = new XMvnHomeClassLoader( resourceDir, null ))
        {
            assertEquals( resourceDir, loader.getHome() );
        }
    }

    @Test
    public void testHomeProperty()
        throws Exception
    {
        String saved = System.getProperty( "xmvn.home" );
        try
        {
            System.setProperty( "xmvn.home", resourceDir.toString() );
            try (XMvnHomeClassLoader loader = new XMvnHomeClassLoader( null ))
            {
                assertEquals( resourceDir, loader.getHome() );
            }
        }
        finally
        {
            if ( saved != null )
                System.setProperty( "xmvn.home", saved );
        }
    }

    @Test
    public void testLoadJar()
        throws Exception
    {
        try (XMvnHomeClassLoader loader =
            new XMvnHomeClassLoader( resourceDir, XMvnHomeClassLoaderTest.class.getClassLoader() ))
        {
            loader.addJar( jarPath );
            Class<?> clazz = loader.loadClass( "com.example.Example" );
            String data = (String) clazz.getMethod( "getTestData" ).invoke( null, new Object[0] );
            assertEquals( "test", data );
        }
    }

    @Test
    public void testImports()
        throws Exception
    {
        try (XMvnHomeClassLoader loader = new XMvnHomeClassLoader( resourceDir, new MockClassLoader() ))
        {
            String[] imports =
                { "org.fedoraproject.xmvn", "org.fedoraproject.xmvn.artifact", "org.fedoraproject.xmvn.deployer",
                    "org.fedoraproject.xmvn.resolver" };
            for ( String imp : imports )
            {
                Class<?> clazz = loader.loadClass( imp + ".Example" );
                assertEquals( MockClassLoader.class, clazz );
            }
        }
    }
}
