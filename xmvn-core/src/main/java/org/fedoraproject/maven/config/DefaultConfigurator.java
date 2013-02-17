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
package org.fedoraproject.maven.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Reader;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Writer;

@Component( role = Configurator.class )
public class DefaultConfigurator
    implements Configurator
{
    @Requirement
    private Logger logger;

    @Requirement
    private ConfigurationMerger merger;

    private Configuration loadConfiguration( InputStream stream )
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

    private Configuration loadRawConfiguration( String name, Path path )
    {
        try (InputStream childStream = new FileInputStream( path.toFile() ))
        {
            return loadConfiguration( childStream );
        }
        catch ( FileNotFoundException e )
        {
            logger.warn( "Failed to load " + name + " configuration from " + path + ": " + e );
            return null;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to load " + name + " configuration from " + path, e );
        }
    }

    @Override
    public Configuration getDefaultConfiguration()
    {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream stream = loader.getResourceAsStream( "default-configuration.xml" ))
        {
            return loadConfiguration( stream );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to load default XMvn configuration", e );
        }
    }

    @Override
    public Configuration getRawSystemConfiguration()
    {
        Path systemConfig = Paths.get( "/etc/xmvn/system-configuration.xml" );
        return loadRawConfiguration( "system", systemConfig );
    }

    @Override
    public Configuration getSystemConfiguration()
    {
        return merger.merge( getRawSystemConfiguration(), getDefaultConfiguration() );
    }

    @Override
    public Configuration getRawUserConfiguration()
    {
        Path userHome = Paths.get( System.getProperty( "user.home" ) );
        Path userConfig = userHome.resolve( ".xmvn" ).resolve( "user-configuration.xml" );
        return loadRawConfiguration( "user", userConfig );
    }

    @Override
    public Configuration getUserConfiguration()
    {
        return merger.merge( getRawUserConfiguration(), getSystemConfiguration() );
    }

    @Override
    public Configuration getRawReactorConfiguration()
    {
        Path reactorPath = Paths.get( "." ).toAbsolutePath();
        Path reactorConfig = reactorPath.resolve( ".xmvn" ).resolve( "reactor-configuration.xml" );
        return loadRawConfiguration( "reactor", reactorConfig );
    }

    @Override
    public Configuration getReactorConfiguration()
    {
        return merger.merge( getRawReactorConfiguration(), getUserConfiguration() );
    }

    @Override
    public Configuration getConfiguration()
    {
        return getReactorConfiguration();
    }

    private void dump( String name, Configuration configuration )
    {
        try (StringWriter writer = new StringWriter())
        {
            if ( configuration != null )
            {
                ConfigurationXpp3Writer dumper = new ConfigurationXpp3Writer();
                dumper.write( writer, configuration );
            }
            else
            {
                writer.write( " (non-existent)\n" );
            }
            logger.debug( "XMvn " + name + " configuration:\n" + writer.toString() );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void dumpConfiguration()
    {
        dump( "default", getDefaultConfiguration() );
        dump( "raw system", getRawSystemConfiguration() );
        dump( "effective system", getSystemConfiguration() );
        dump( "raw user", getRawUserConfiguration() );
        dump( "effective user", getUserConfiguration() );
        dump( "raw reactor", getRawReactorConfiguration() );
        dump( "effective reactor", getReactorConfiguration() );
    }
}
