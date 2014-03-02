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
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * @author Mikolaj Izdebski
 */
class IsolatedXMvnServiceLocator
{
    static
    {
        INSTANCE = new IsolatedXMvnServiceLocator();
    }

    static IsolatedXMvnServiceLocator INSTANCE;

    private static Path getHome()
    {
        String home = System.getProperty( "xmvn.home" );
        if ( home == null )
            home = System.getenv( "XMVN_HOME" );
        if ( home == null )
            home = System.getenv( "M2_HOME" );
        if ( home == null )
            home = "/usr/share/xmvn";
        return Paths.get( home );
    }

    final IsolatedClassRealm realm;

    public static IsolatedClassRealm getRealm()
    {
        return INSTANCE.realm;
    }

    final Method getServiceMethod;

    private IsolatedXMvnServiceLocator()
    {
        try
        {
            realm = new IsolatedClassRealm( XMvn.class.getClassLoader() );

            realm.addJarDirectory( getHome().resolve( "isolated" ) );

            realm.importPackage( "org.fedoraproject.xmvn" );
            realm.importPackage( "org.fedoraproject.xmvn.deployer" );
            realm.importPackage( "org.fedoraproject.xmvn.resolver" );
            realm.importPackage( "org.eclipse.aether.artifact" );
            realm.importPackage( "org.slf4j" );
            realm.importPackage( "org.slf4j.impl" );
            realm.importPackage( "org.slf4j.spi" );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to initialize XMvn class realm", e );
        }

        try
        {
            getServiceMethod = realm.execute( new Callable<Method>()
            {
                @Override
                public Method call()
                    throws ReflectiveOperationException
                {
                    Class<?> locatorClass = realm.loadClass( "org.fedoraproject.xmvn.locator.XMvnServiceLocator" );
                    return locatorClass.getDeclaredMethod( "getService", Class.class );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Unable to initialize XMvn class space", e );
        }
    }

    public static <T> T getService( final Class<T> role )
    {
        try
        {
            return INSTANCE.realm.execute( new Callable<T>()
            {
                @Override
                public T call()
                    throws ReflectiveOperationException
                {
                    return (T) INSTANCE.getServiceMethod.invoke( null, role );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
