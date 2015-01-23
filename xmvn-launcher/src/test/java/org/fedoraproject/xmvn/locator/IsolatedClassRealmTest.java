/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class IsolatedClassRealmTest
{
    protected final Path resourceDir = Paths.get( "src/test/resources" ).toAbsolutePath();

    protected Path jarPath = resourceDir.resolve( "example.jar" );

    protected Path secondJarPath = resourceDir.resolve( "example2.jar" );

    @Test
    public void testImports()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( null ))
        {
            realm.importPackage( "java.lang" );
            realm.importPackage( "junit" );
            realm.importPackage( "org.eclipse.sisu.space" );

            assertTrue( realm.isImported( "java.lang.Object" ) );
            assertTrue( realm.isImported( "java/lang/Object" ) );
            assertTrue( realm.isImported( "java/lang/Object.class" ) );

            assertFalse( realm.isImported( "java.math.Random" ) );
            assertFalse( realm.isImported( "java/math/Random" ) );
            assertFalse( realm.isImported( "java/math/Random.class" ) );

            assertTrue( realm.isImported( "junit.Assert" ) );
            assertFalse( realm.isImported( "junit" ) );
            assertFalse( realm.isImported( "org.junit.Assert" ) );

            assertTrue( realm.isImported( "org.eclipse.sisu.space.ClassSpace" ) );
            assertFalse( realm.isImported( "org.eclipse.sisu.space.asm.ClassVisitor" ) );
        }
    }

    @Test
    public void testImportAll()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( null ))
        {
            realm.importAllPackages( "org.fedoraproject.xmvn" );

            assertTrue( realm.isImported( "org.fedoraproject.xmvn.Artifact" ) );
            assertTrue( realm.isImported( "org.fedoraproject.xmvn.install.Installer" ) );
            assertTrue( realm.isImported( "org.fedoraproject.xmvn.install.impl.DefaultInstaller" ) );

            assertFalse( realm.isImported( "org.fedoraproject.Example" ) );
        }
    }

    @Test
    public void testLoadJar()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( IsolatedClassRealmTest.class.getClassLoader() ))
        {
            realm.addJar( jarPath );
            Class<?> clazz = realm.loadClass( "com.example.Example" );
            String data = (String) clazz.getMethod( "getTestData" ).invoke( null, new Object[0] );
            assertEquals( "test", data );
        }
    }

    @Test
    public void testLoadJarDirectory()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( IsolatedClassRealmTest.class.getClassLoader() ))
        {
            realm.addJarDirectory( resourceDir );
            Class<?> clazz = realm.loadClass( "com.example.Example" );
            String data = (String) clazz.getMethod( "getTestData" ).invoke( null, new Object[0] );
            assertEquals( "test", data );
            Class<?> clazz2 = realm.loadClass( "com.example.SecondExample" );
            String data2 = (String) clazz2.getMethod( "getTestData" ).invoke( null, new Object[0] );
            assertEquals( "test-second", data2 );
        }
    }

    @Test
    public void testGetResource()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( IsolatedClassRealmTest.class.getClassLoader() ))
        {
            realm.addJar( jarPath );
            try (InputStream resourceStream = realm.getResourceAsStream( "secret-file" ))
            {
                assertNotNull( resourceStream );
                int read = resourceStream.read();
                assertEquals( '#', read );
            }
        }
    }

    @Test
    public void testGetResources()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( IsolatedClassRealmTest.class.getClassLoader() ))
        {
            realm.addJarDirectory( resourceDir );
            realm.getResource( "secret-file" );
            Enumeration<URL> resources = realm.getResources( "secret-file" );
            assertTrue( resources.hasMoreElements() );
            resources.nextElement();
            assertTrue( resources.hasMoreElements() );
        }
    }

    @Test
    public void testParentClassloader()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( new MockClassLoader() ))
        {
            realm.addJar( jarPath );
            realm.importPackage( "com.example" );
            Class<?> clazz = realm.loadClass( "com.example.Example" );
            assertEquals( MockClassLoader.class, clazz );
        }
    }

    @Test
    public void testParentClassloaderResource()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( new MockClassLoader() ))
        {
            realm.addJar( jarPath );
            realm.importPackage( "resources" );
            URL url = realm.getResource( "resources/secret-file" );
            assertEquals( "http://example.com", url.toString() );
        }
    }
}
