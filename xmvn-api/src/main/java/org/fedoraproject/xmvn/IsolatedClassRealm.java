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
package org.fedoraproject.xmvn;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Mikolaj Izdebski
 */
class IsolatedClassRealm
    extends URLClassLoader
{
    static
    {
        registerAsParallelCapable();
    }

    private final ClassLoader parent;

    private final Set<String> imports = new HashSet<>();

    public IsolatedClassRealm( ClassLoader parent )
    {
        super( new URL[0], null );
        this.parent = parent;
    }

    public void addJar( Path jar )
        throws IOException
    {
        addURL( jar.toUri().toURL() );
    }

    public void addJarDirectory( Path dir )
        throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream( dir, "*.jar" ))
        {
            for ( Path path : stream )
            {
                addURL( path.toUri().toURL() );
            }
        }
    }

    public void importPackage( String packageName )
    {
        imports.add( packageName );
    }

    boolean isImported( String name )
    {
        int index = name.lastIndexOf( '/' );

        if ( index >= 0 )
        {
            name = name.replace( '/', '.' );
        }
        else
        {
            index = Math.max( name.lastIndexOf( '.' ), 0 );
        }

        return imports.contains( name.substring( 0, index ) );
    }

    @Override
    public Class<?> loadClass( String name )
        throws ClassNotFoundException
    {
        return loadClass( name, false );
    }

    @Override
    protected Class<?> loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        if ( isImported( name ) )
        {
            try
            {
                return parent.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
            }
        }

        try
        {
            return super.loadClass( name, resolve );
        }
        catch ( ClassNotFoundException e )
        {
        }

        synchronized ( getClassLoadingLock( name ) )
        {
            Class<?> clazz = findLoadedClass( name );
            if ( clazz != null )
            {
                return clazz;
            }

            try
            {
                return super.findClass( name );
            }
            catch ( ClassNotFoundException e )
            {
            }
        }

        throw new ClassNotFoundException( name );
    }

    @Override
    protected Class<?> findClass( String name )
        throws ClassNotFoundException
    {
        throw new ClassNotFoundException( name );
    }

    @Override
    public URL getResource( String name )
    {
        if ( isImported( name ) )
        {
            URL resource = parent.getResource( name );
            if ( resource != null )
            {
                return resource;
            }
        }

        URL resource = super.getResource( name );
        if ( resource != null )
        {
            return resource;
        }

        resource = super.findResource( name );
        if ( resource != null )
        {
            return resource;
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources( String name )
        throws IOException
    {
        Collection<URL> resources = new LinkedHashSet<>();

        if ( isImported( name ) )
        {
            try
            {
                resources.addAll( Collections.list( parent.getResources( name ) ) );
            }
            catch ( IOException e )
            {
            }
        }

        try
        {
            resources.addAll( Collections.list( super.getResources( name ) ) );
        }
        catch ( IOException e )
        {
        }

        try
        {
            resources.addAll( Collections.list( super.findResources( name ) ) );
        }
        catch ( IOException e )
        {
        }

        return Collections.enumeration( resources );
    }

    public <T> T execute( Callable<T> routine )
        throws Exception
    {
        ClassLoader savedThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( this );
            return routine.call();
        }
        catch ( ReflectiveOperationException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( savedThreadContextClassLoader );
        }
    }
}
