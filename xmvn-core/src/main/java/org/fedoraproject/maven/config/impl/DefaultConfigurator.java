/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.config.impl;

import java.io.FileInputStream;
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.ConfigurationMerger;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Reader;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Writer;

/**
 * Default implementation of XMvn configurator.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Configurator.class, instantiationStrategy = "singleton" )
public class DefaultConfigurator
    implements Configurator
{
    @Requirement
    private Logger logger;

    @Requirement
    private ConfigurationMerger merger;

    private Configuration cachedConfiguration;

    private Configuration cachedDefaultConfiguration;

    private List<Path> configFiles;

    private Configuration loadConfigurationFromStream( InputStream stream )
        throws IOException
    {
        try
        {
            ConfigurationXpp3Reader reader = new ConfigurationXpp3Reader();
            return reader.read( stream );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "Failed to parse configuration", e );
        }
    }

    private Configuration loadDefaultConfiguration()
    {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream stream = loader.getResourceAsStream( "default-configuration.xml" ))
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
        try (InputStream childStream = new FileInputStream( path.toFile() ))
        {
            return loadConfigurationFromStream( childStream );
        }
    }

    private String getCompatLevelSuffix()
    {
        String compatLevel = System.getProperty( "xmvn.compat" );
        if ( compatLevel == null )
            compatLevel = System.getenv( "XMVN_COMPAT" );
        return compatLevel;
    }

    private String getEvnDefault( String key, Object defaultValue )
    {
        String value = System.getenv( key );
        if ( !StringUtils.isNotEmpty( value ) )
        {
            logger.debug( "Environmental variable $" + key + " is unset or empty, using default value: " + defaultValue );
            return defaultValue.toString();
        }

        return value;
    }

    private void addConfigFile( Path file, boolean useCompat )
    {
        String suffix = getCompatLevelSuffix();
        if ( suffix != null && useCompat )
        {
            Path compatFile = Paths.get( file + "-" + suffix );
            if ( Files.isRegularFile( compatFile ) )
                file = compatFile;
        }

        if ( !Files.isRegularFile( file ) )
        {
            String reason = "not a regular file";
            if ( !Files.exists( file ) )
                reason = "no such file";

            logger.debug( "Skipping configuration file " + file + ": " + reason );
            return;
        }

        configFiles.add( file );
    }

    private void addConfigDir( Path directory, boolean useCompat )
        throws IOException
    {
        String suffix = getCompatLevelSuffix();
        if ( suffix != null && useCompat )
        {
            Path compatDirectory = Paths.get( directory + "-" + suffix );
            if ( Files.isDirectory( compatDirectory ) )
                directory = compatDirectory;
        }

        if ( !Files.isDirectory( directory ) )
        {
            String reason = "not a directory";
            if ( !Files.exists( directory ) )
                reason = "no such directory";

            logger.debug( "Skipping configuration directory " + directory + ": " + reason );
            return;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream( directory ))
        {
            Set<Path> childreen = new TreeSet<>();
            for ( Path file : directoryStream )
                childreen.add( file );

            for ( Path file : childreen )
                addConfigFile( file, false );
        }
    }

    private void addXdgBasePath( String location )
        throws IOException
    {
        Path base = Paths.get( location );
        if ( !base.isAbsolute() )
        {
            logger.debug( "Skipping XDG configuration directory " + base + ": path is not absolute" );
            return;
        }

        base = base.resolve( "xmvn" );
        addConfigDir( base.resolve( "config.d" ), true );
        addConfigFile( base.resolve( "configuration.xml" ), true );
    }

    private Configuration loadConfiguration()
    {
        try
        {
            Path reactorConfDir = Paths.get( ".xmvn" ).toAbsolutePath();
            Path xdgHome = Paths.get( getEvnDefault( "HOME", System.getProperty( "user.home" ) ) );

            // 1. artifact configuration: pom.xml
            configFiles = new ArrayList<>();

            // 2. reactor configuration directory: $PWD/.xmvn/conf.d/
            addConfigDir( reactorConfDir.resolve( "config.d" ), true );
            // 3. reactor configuration file: $PWD/.xmvn/configuration.xml
            addConfigFile( reactorConfDir.resolve( "configuration.xml" ), true );

            // 4. user configuration directory: $XDG_CONFIG_HOME/xmvn/conf.d/
            // 5. user configuration file: $XDG_CONFIG_HOME/xmvn/configuration.xml
            addXdgBasePath( getEvnDefault( "XDG_CONFIG_HOME", xdgHome.resolve( ".config" ) ) );

            // 6. user data directory: $XDG_DATA_HOME/xmvn/conf.d/
            // 7. user data file: $XDG_DATA_HOME/xmvn/configuration.xml
            addXdgBasePath( getEvnDefault( "XDG_DATA_HOME", xdgHome.resolve( ".local" ).resolve( "share" ) ) );

            // 8. system configuration directories: $XDG_CONFIG_DIRS/xmvn/conf.d/
            // 9. system configuration files: $XDG_CONFIG_DIRS/xmvn/configuration.xml
            for ( String part : StringUtils.split( getEvnDefault( "XDG_CONFIG_DIRS", "/etc/xdg" ), ":" ) )
                addXdgBasePath( part );

            // 10. system data directories: $XDG_DATA_DIRS/xmvn/conf.d/
            // 11. system data files: $XDG_DATA_DIRS/xmvn/configuration.xml
            for ( String part : StringUtils.split( getEvnDefault( "XDG_DATA_DIRS", "/usr/local/share:/usr/share" ), ":" ) )
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
                    logger.debug( "  * " + file.toString() );
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
            cachedDefaultConfiguration = loadDefaultConfiguration();

        return cachedDefaultConfiguration;
    }

    @Override
    public synchronized Configuration getConfiguration()
    {
        if ( cachedConfiguration == null )
            cachedConfiguration = loadConfiguration();

        return cachedConfiguration;
    }

    @Override
    public void dumpConfiguration()
    {
        Configuration configuration = getConfiguration();

        try (StringWriter writer = new StringWriter())
        {
            ConfigurationXpp3Writer dumper = new ConfigurationXpp3Writer();
            dumper.write( writer, configuration );
            logger.debug( "XMvn configuration:\n" + writer.toString() );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }
}
