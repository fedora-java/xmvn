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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
public class XMvn
{
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

    private static URL[] getLibraryJars()
    {
        List<URL> urls = new ArrayList<>();

        for ( String subdir : Arrays.asList( "boot", "lib" ) )
        {
            Path dir = getHome().resolve( subdir );
            try (DirectoryStream<Path> stream = Files.newDirectoryStream( dir ))
            {
                for ( Path file : stream )
                {
                    if ( file.toString().endsWith( ".jar" ) )
                        urls.add( file.toUri().toURL() );
                }
            }
            catch ( IOException | DirectoryIteratorException e )
            {
                throw new RuntimeException( e );
            }
        }

        return urls.toArray( new URL[0] );
    }

    private static ClassLoader savedClassLoader;

    private static ClassLoader sisuRealm;

    private static void enterSisuRealm()
    {
        Thread currentThread = Thread.currentThread();
        savedClassLoader = currentThread.getContextClassLoader();

        if ( sisuRealm == null )
            sisuRealm = new URLClassLoader( getLibraryJars(), XMvn.class.getClassLoader() );
        currentThread.setContextClassLoader( sisuRealm );
    }

    private static void leaveSisuRealm()
    {
        Thread.currentThread().setContextClassLoader( savedClassLoader );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private static <T> T lookup( Class<T> role )
    {
        try
        {
            enterSisuRealm();

            Class urlClassSpaceClass = sisuRealm.loadClass( "org.eclipse.sisu.space.URLClassSpace" );
            Class spaceModuleClass = sisuRealm.loadClass( "org.eclipse.sisu.space.SpaceModule" );
            Class classSpaceClass = sisuRealm.loadClass( "org.eclipse.sisu.space.ClassSpace" );
            Class wireModuleClass = sisuRealm.loadClass( "org.eclipse.sisu.wire.WireModule" );
            Class guiceClass = sisuRealm.loadClass( "com.google.inject.Guice" );
            Class injectorClass = sisuRealm.loadClass( "com.google.inject.Injector" );

            Constructor urlClassSpaceConstructor = urlClassSpaceClass.getConstructor( ClassLoader.class );
            Constructor spaceModuleConstructor = spaceModuleClass.getConstructor( classSpaceClass );
            Constructor wireModuleConstructor = wireModuleClass.getConstructor( Iterable.class );
            Method createInjectorMethod = guiceClass.getMethod( "createInjector", Iterable.class );
            Method getInstanceMethod = injectorClass.getMethod( "getInstance", Class.class );

            Object urlClassSpace = urlClassSpaceConstructor.newInstance( sisuRealm );
            Object spaceModule = spaceModuleConstructor.newInstance( urlClassSpace );
            Object wireModule = wireModuleConstructor.newInstance( Collections.singleton( spaceModule ) );
            Object injector = createInjectorMethod.invoke( null, Collections.singleton( wireModule ) );
            Object component = getInstanceMethod.invoke( injector, role );

            return (T) component;
        }
        catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | InstantiationException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            leaveSisuRealm();
        }
    }

    private static class IsolatedResolver
        implements Resolver
    {
        private static final Resolver instance = new IsolatedResolver();

        private final Resolver delegate;

        public IsolatedResolver()
        {
            delegate = lookup( Resolver.class );
        }

        @Override
        public ResolutionResult resolve( ResolutionRequest request )
        {
            try
            {
                enterSisuRealm();
                return delegate.resolve( request );
            }
            finally
            {
                leaveSisuRealm();
            }
        }
    }

    private static class IsolatedDeployer
        implements Deployer
    {
        private static final Deployer instance = new IsolatedDeployer();

        private final Deployer delegate;

        public IsolatedDeployer()
        {
            delegate = lookup( Deployer.class );
        }

        @Override
        public DeploymentResult deploy( DeploymentRequest request )
        {
            try
            {
                enterSisuRealm();
                return delegate.deploy( request );
            }
            finally
            {
                leaveSisuRealm();
            }
        }
    }

    public static Resolver getResolver()
    {
        return IsolatedResolver.instance;
    }

    public static Deployer getDeployer()
    {
        return IsolatedDeployer.instance;
    }

    public static ResolutionResult resolve( ResolutionRequest request )
    {
        return getResolver().resolve( request );
    }

    public static DeploymentResult deploy( DeploymentRequest request )
    {
        return getDeployer().deploy( request );
    }

    public static void main( String[] args )
    {
        Artifact artifact = new DefaultArtifact( args[0] );
        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolve( request );
        System.err.println( result.getArtifactFile() );
    }
}
