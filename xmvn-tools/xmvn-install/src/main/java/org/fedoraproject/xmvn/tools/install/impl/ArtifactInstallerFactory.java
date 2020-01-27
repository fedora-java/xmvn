/*-
 * Copyright (c) 2014-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactInstallerFactory
{
    private final Logger logger = LoggerFactory.getLogger( ArtifactInstallerFactory.class );

    private final ArtifactInstaller defaultArtifactInstaller;

    private final IsolatedClassRealm pluginRealm;

    private final Map<String, ArtifactInstaller> cachedPluginsByType = new LinkedHashMap<>();

    private final Map<String, ArtifactInstaller> cachedPluginsByImplClass = new LinkedHashMap<>();

    private ArtifactInstaller tryLoadPlugin( String type )
    {
        if ( cachedPluginsByType.containsKey( type ) )
            return cachedPluginsByType.get( type );

        try
        {
            String resourceName = ArtifactInstaller.class.getCanonicalName() + "/" + type;
            InputStream resourceStream = pluginRealm != null ? pluginRealm.getResourceAsStream( resourceName ) : null;
            if ( resourceStream == null )
            {
                logger.debug( "No XMvn Installer plugin found for packaging type {}", type );
                cachedPluginsByType.put( type, null );
                return null;
            }

            String pluginImplClass;
            try ( BufferedReader resourceReader = new BufferedReader( new InputStreamReader( resourceStream ) ) )
            {
                pluginImplClass = resourceReader.readLine();
            }

            ArtifactInstaller pluggedInInstaller = cachedPluginsByImplClass.get( pluginImplClass );
            if ( pluggedInInstaller == null )
            {
                pluggedInInstaller =
                    (ArtifactInstaller) pluginRealm.loadClass( pluginImplClass ).getConstructor().newInstance();
                cachedPluginsByImplClass.put( pluginImplClass, pluggedInInstaller );
            }

            cachedPluginsByType.put( type, pluggedInInstaller );
            return pluggedInInstaller;
        }
        catch ( IOException | ReflectiveOperationException e )
        {
            throw new RuntimeException( "Unable to load XMvn Installer plugin for packaging type " + type, e );
        }
    }

    public ArtifactInstallerFactory( Configurator configurator )
    {
        this( configurator, Paths.get( "/usr/share/xmvn/lib/installer" ) );
    }

    ArtifactInstallerFactory( Configurator configurator, Path pluginDir )
    {
        defaultArtifactInstaller = new DefaultArtifactInstaller( configurator );

        if ( Files.isDirectory( pluginDir ) )
        {
            ClassLoader parentClassLoader = ArtifactInstallerFactory.class.getClassLoader();
            pluginRealm = new IsolatedClassRealm( parentClassLoader );
            pluginRealm.addJarDirectory( pluginDir );
            PLUGIN_IMPORTS.forEach( pluginRealm::importPackage );
        }
        else
        {
            pluginRealm = null;
        }
    }

    /**
     * List of packages imported from XMvn Installer class loader to plug-in realms.
     */
    private static final List<String> PLUGIN_IMPORTS = Arrays.asList( // XMvn API
                                                                      "org.fedoraproject.xmvn.artifact", //
                                                                      "org.fedoraproject.xmvn.config", //
                                                                      "org.fedoraproject.xmvn.deployer", //
                                                                      "org.fedoraproject.xmvn.locator", //
                                                                      "org.fedoraproject.xmvn.metadata", //
                                                                      "org.fedoraproject.xmvn.resolver", //
                                                                      // XMvn Installer SPI
                                                                      "org.fedoraproject.xmvn.tools.install", //
                                                                      // SLF4J API
                                                                      "org.slf4j" //
    );

    @SuppressWarnings( "unused" )
    public ArtifactInstaller getInstallerFor( Artifact artifact, Properties properties )
    {
        String type = properties.getProperty( "type" );
        if ( type != null )
        {
            ArtifactInstaller pluggedInInstaller = tryLoadPlugin( type );
            if ( pluggedInInstaller != null )
                return pluggedInInstaller;
        }

        return defaultArtifactInstaller;
    }
}
