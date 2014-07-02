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
package org.fedoraproject.xmvn.osgi.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.fedoraproject.xmvn.osgi.OSGiConfigurator;
import org.fedoraproject.xmvn.osgi.OSGiFramework;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultOSGiFramework
    implements OSGiFramework
{
    private final Logger logger = LoggerFactory.getLogger( DefaultOSGiFramework.class );

    private final OSGiConfigurator equinoxLocator;

    private BundleContext bundleContext;

    @Inject
    public DefaultOSGiFramework( OSGiConfigurator equinoxLocator )
    {
        this.equinoxLocator = equinoxLocator;
    }

    private BundleContext launchEquinox()
        throws Exception
    {
        Map<String, String> properties = new LinkedHashMap<>();

        if ( logger.isDebugEnabled() )
        {
            properties.put( "osgi.debug", "true" );
            properties.put( "eclipse.consoleLog", "true" );
        }

        properties.put( "osgi.bundles", Joiner.on( ',' ).join( equinoxLocator.getBundles() ) );

        properties.put( "osgi.parentClassloader", "fwk" );
        properties.put( "org.osgi.framework.system.packages.extra",
                        Joiner.on( ',' ).join( equinoxLocator.getExportedPackages() ) );

        logger.info( "Launching Equinox..." );
        System.setProperty( "osgi.framework.useSystemProperties", "false" );
        EclipseStarter.setInitialProperties( properties );
        EclipseStarter.startup( new String[0], null );
        BundleContext context = EclipseStarter.getSystemBundleContext();

        if ( context == null )
        {
            logger.error( "Failed to launch Equinox" );
            if ( !logger.isDebugEnabled() )
                logger.info( "You can enable debugging output with -X to see more information." );
            throw new RuntimeException( "Failed to launch Equinox" );
        }

        tryActivateBundle( context, "org.eclipse.equinox.ds" );
        tryActivateBundle( context, "org.eclipse.equinox.registry" );
        tryActivateBundle( context, "org.eclipse.core.net" );

        logger.debug( "Equinox launched successfully" );
        return context;
    }

    private void tryActivateBundle( BundleContext bundleContext, String symbolicName )
    {
        logger.debug( "Trying to activate {}", symbolicName );

        for ( Bundle bundle : bundleContext.getBundles() )
        {
            if ( symbolicName.equals( bundle.getSymbolicName() ) )
            {
                try
                {
                    bundle.start( Bundle.START_TRANSIENT );
                }
                catch ( BundleException e )
                {
                    logger.warn( "Failed to activate bundle {}/{}", symbolicName, bundle.getVersion(), e );
                }
            }
        }
    }

    @Override
    public synchronized BundleContext getBundleContext()
    {
        if ( bundleContext != null )
            return bundleContext;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            bundleContext = launchEquinox();
            return bundleContext;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( classLoader );
        }
    }
}
