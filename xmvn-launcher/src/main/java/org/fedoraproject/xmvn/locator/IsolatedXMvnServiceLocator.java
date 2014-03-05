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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Mikolaj Izdebski
 */
public class IsolatedXMvnServiceLocator
{
    final ClassLoader classLoader;

    public IsolatedXMvnServiceLocator( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    public <T> T call( Callable<T> routine )
        throws Exception
    {
        ClassLoader savedThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( classLoader );
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

    public <T> T get( final Class<T> role )
    {
        try
        {
            return call( new Callable<T>()
            {
                @Override
                public T call()
                    throws ReflectiveOperationException
                {
                    Class<?> locatorClass = classLoader.loadClass( "org.fedoraproject.xmvn.locator.XMvnServiceLocator" );
                    Method getServiceMethod = locatorClass.getDeclaredMethod( "getService", Class.class );
                    return (T) getServiceMethod.invoke( null, role );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
