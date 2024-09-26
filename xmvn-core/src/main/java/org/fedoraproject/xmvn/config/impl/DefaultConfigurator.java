/*-
 * Copyright (c) 2013-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxReader;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.logging.Logger;

/**
 * Default implementation of XMvn configurator.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultConfigurator
    implements Configurator
{
    private final Logger logger;

    private final ConfigurationMerger merger = new ConfigurationMerger();

    private Configuration cachedConfiguration;

    private Configuration cachedDefaultConfiguration;

    private List<Path> configFiles;

    @Inject
    public DefaultConfigurator( Logger logger )
    {
        this.logger = logger;
    }

    public DefaultConfigurator( ServiceLocator locator )
    {
        this( locator.getService( Logger.class ) );
    }

    private Configuration loadConfigurationFromStream( InputStream stream )
        throws IOException
    {
        try
        {
            ConfigurationStaxReader reader = new ConfigurationStaxReader();
            return reader.read( stream );
        }
        catch ( XMLStreamException e )
        {
            throw new IOException( "Failed to parse configuration", e );
        }
    }

    private Configuration loadDefaultConfiguration()
    {
        ClassLoader loader = getClass().getClassLoader();
        try ( InputStream stream = loader.getResourceAsStream( "default-configuration.xml" ) )
        {
            return loadConfigurationFromStream( stream );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to load default XMvn configuration", e );
        }
    }

    private Configuration loadConfiguration( Path path )
        throws IOException
    {
        try ( InputStream childStream = Files.newInputStream( path ) )
        {
            return loadConfigurationFromStream( childStream );
        }
    }

    private String getEnvDefault( String key, Object defaultValue )
    {
        String value = System.getenv( key );
        if ( value == null || value.isEmpty() )
        {
            logger.debug( "Environmental variable ${} is unset or empty, using default value: {}", key, defaultValue );
            return defaultValue.toString();
        }

        return value;
    }

    private void addConfigFile( Path file )
    {
        if ( !Files.isRegularFile( file ) )
        {
            String reason = "not a regular file";
            if ( !Files.exists( file ) )
            {
                reason = "no such file";
            }

            logger.debug( "Skipping configuration file {}: {}", file, reason );
            return;
        }

        configFiles.add( file );
    }

    private void addConfigDir( Path directory )
        throws IOException
    {
        if ( !Files.isDirectory( directory ) )
        {
            String reason = "not a directory";
            if ( !Files.exists( directory ) )
            {
                reason = "no such directory";
            }

            logger.debug( "Skipping configuration directory {}: {}", directory, reason );
            return;
        }

        try ( DirectoryStream<Path> directoryStream = Files.newDirectoryStream( directory ) )
        {
            Set<Path> children = new TreeSet<>();
            for ( Path file : directoryStream )
                children.add( file );

            for ( Path file : children )
                addConfigFile( file );
        }
    }

    private void addXdgBasePath( String location )
        throws IOException
    {
        Path base = Paths.get( location );
        if ( !base.isAbsolute() )
        {
            logger.debug( "Skipping XDG configuration directory {}: path is not absolute", base );
            return;
        }
        if ( System.getProperty( "xmvn.config.sandbox" ) != null )
        {
            logger.debug( "Skipping XDG configuration directory {}: running in sandbox environment", base );
            return;
        }

        base = base.resolve( "xmvn" );
        addConfigDir( base.resolve( "config.d" ) );
        addConfigFile( base.resolve( "configuration.xml" ) );
    }

    private Configuration loadConfiguration()
    {
        try
        {
            Path reactorConfDir = Paths.get( ".xmvn" ).toAbsolutePath();
            Path xdgHome = Paths.get( getEnvDefault( "HOME", System.getProperty( "user.home" ) ) );

            // 1. artifact configuration: pom.xml
            configFiles = new ArrayList<>();

            // 2. reactor configuration directory: $PWD/.xmvn/config.d/
            addConfigDir( reactorConfDir.resolve( "config.d" ) );
            // 3. reactor configuration file: $PWD/.xmvn/configuration.xml
            addConfigFile( reactorConfDir.resolve( "configuration.xml" ) );

            // 4. user configuration directory: $XDG_CONFIG_HOME/xmvn/config.d/
            // 5. user configuration file: $XDG_CONFIG_HOME/xmvn/configuration.xml
            addXdgBasePath( getEnvDefault( "XDG_CONFIG_HOME", xdgHome.resolve( ".config" ) ) );

            // 6. user data directory: $XDG_DATA_HOME/xmvn/config.d/
            // 7. user data file: $XDG_DATA_HOME/xmvn/configuration.xml
            addXdgBasePath( getEnvDefault( "XDG_DATA_HOME", xdgHome.resolve( ".local" ).resolve( "share" ) ) );

            // 8. system configuration directories: $XDG_CONFIG_DIRS/xmvn/config.d/
            // 9. system configuration files: $XDG_CONFIG_DIRS/xmvn/configuration.xml
            for ( String part : getEnvDefault( "XDG_CONFIG_DIRS", "/etc/xdg" ).split( ":+" ) )
                addXdgBasePath( part );

            // 10. system data directories: $XDG_DATA_DIRS/xmvn/config.d/
            // 11. system data files: $XDG_DATA_DIRS/xmvn/configuration.xml
            for ( String part : getEnvDefault( "XDG_DATA_DIRS", "/usr/local/share:/usr/share" ).split( ":+" ) )
                addXdgBasePath( part );

            // 12. built-in xmvn-core.jar
            Configuration conf = getDefaultConfiguration();

            if ( configFiles.isEmpty() )
            {
                logger.warn( "No XMvn configuration files were found. Using default embedded configuration." );
            }
            else
            {
                logger.debug( "XMvn configuration files used:" );
                for ( Path file : configFiles )
                    logger.debug( "  * {}", file.toString() );
            }

            List<Path> reversedConfigFiles = new ArrayList<>( configFiles );
            Collections.reverse( reversedConfigFiles );
            for ( Path file : reversedConfigFiles )
                conf = merger.merge( loadConfiguration( file ), conf );

            return conf;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to load XMvn configuration", e );
        }
    }

    @Override
    public synchronized Configuration getDefaultConfiguration()
    {
        if ( cachedDefaultConfiguration == null )
        {
            cachedDefaultConfiguration = loadDefaultConfiguration();
        }

        return cachedDefaultConfiguration;
    }

    @Override
    public synchronized Configuration getConfiguration()
    {
        if ( cachedConfiguration == null )
        {
            cachedConfiguration = loadConfiguration();
        }

        return cachedConfiguration;
    }

    public void dumpConfiguration()
    {
        Configuration configuration = getConfiguration();

        try ( StringWriter writer = new StringWriter() )
        {
            ConfigurationStaxWriter dumper = new ConfigurationStaxWriter();
            dumper.write( writer, configuration );
            logger.debug( "XMvn configuration:\n{}", writer.toString() );
        }
        catch ( IOException | XMLStreamException e )
        {
            throw new RuntimeException( e );
        }
    }
}
