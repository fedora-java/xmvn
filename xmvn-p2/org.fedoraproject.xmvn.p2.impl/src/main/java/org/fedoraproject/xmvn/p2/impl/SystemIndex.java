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
package org.fedoraproject.xmvn.p2.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.equinox.p2.metadata.VersionedId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
public class SystemIndex
{
    private final Logger logger = LoggerFactory.getLogger( SystemIndex.class );

    private final Set<IInstallableUnit> platformUnits = new LinkedHashSet<>();

    private final Set<IInstallableUnit> internalUnits = new LinkedHashSet<>();

    private final Set<IInstallableUnit> externalUnits = new LinkedHashSet<>();

    private final Map<IVersionedId, Path> index = new LinkedHashMap<>();

    private void discoverPluginsAndFeatures( Set<Path> plugins, Set<Path> features, Path eclipseDir )
        throws IOException
    {
        if ( Files.isDirectory( eclipseDir.resolve( "plugins" ) ) )
        {
            for ( Path plugin : Files.newDirectoryStream( eclipseDir.resolve( "plugins" ) ) )
            {
                if ( ( Files.isRegularFile( plugin ) && plugin.getFileName().toString().endsWith( ".jar" ) )
                    || Files.isDirectory( plugin ) )
                    plugins.add( plugin.toRealPath() );
            }
        }

        if ( Files.isDirectory( eclipseDir.resolve( "features" ) ) )
        {
            for ( Path feature : Files.newDirectoryStream( eclipseDir.resolve( "features" ) ) )
            {
                if ( ( Files.isRegularFile( feature ) && feature.getFileName().toString().endsWith( ".jar" ) )
                    || Files.isDirectory( feature ) )
                    features.add( feature.toRealPath() );
            }
        }
    }

    private void discoverBundles( Set<Path> bundles, Path repoDir )
        throws IOException
    {
        if ( Files.isDirectory( repoDir ) )
        {
            for ( Path path : Files.newDirectoryStream( repoDir ) )
            {
                if ( Files.isRegularFile( path ) && path.getFileName().toString().endsWith( ".jar" ) )
                    bundles.add( path.toRealPath() );
                else if ( Files.isDirectory( path ) )
                    discoverBundles( bundles, path );
            }
        }
    }

    private void filterPaths( Set<Path> prefixed, Set<Path> unpreifxed, Set<Path> prefixes, Set<Path> elements,
                              Set<Path> excludes )
    {
        elements.removeAll( excludes );

        for ( Path element : elements )
        {
            for ( Path prefix : prefixes )
            {
                if ( element.startsWith( prefix ) )
                {
                    prefixed.add( element );
                    break;
                }
            }
        }

        unpreifxed.addAll( elements );
        unpreifxed.removeAll( prefixed );
    }

    public void index()
        throws ProvisionException, IOException
    {
        Set<Path> javaDirs = new LinkedHashSet<>();
        Set<Path> eclipseDirs = new LinkedHashSet<>();
        Set<Path> platformBundles = new LinkedHashSet<>();
        Set<Path> platformFeatures = new LinkedHashSet<>();
        Set<Path> nonPlatformBundles = new LinkedHashSet<>();
        Set<Path> nonPlatformFeatures = new LinkedHashSet<>();
        Set<Path> internalBundles = new LinkedHashSet<>();
        Set<Path> internalFeatures = new LinkedHashSet<>();
        Set<Path> externalBundles = new LinkedHashSet<>();
        Set<Path> externalFeatures = new LinkedHashSet<>();

        for ( Path root : Collections.singleton( Paths.get( "/" ) ) )
        {
            Path prefix = root.resolve( "usr" );

            for ( String libDirArch : Arrays.asList( "share", "lib", "lib64" ) )
            {
                Path libDir = prefix.resolve( libDirArch );

                Path eclipseDir = libDir.resolve( "eclipse" );
                if ( Files.isDirectory( eclipseDir ) )
                {
                    eclipseDirs.add( eclipseDir );
                    discoverPluginsAndFeatures( platformBundles, platformFeatures, eclipseDir );
                    discoverPluginsAndFeatures( nonPlatformBundles, nonPlatformFeatures, eclipseDir.resolve( "dropins" ) );
                }

                for ( String javaVersion : Arrays.asList( null, "1.5.0", "1.6.0", "1.7.0", "1.8.0" ) )
                {
                    String versionSuffix = javaVersion != null ? "-" + javaVersion : "";
                    Path javaDir = libDir.resolve( "java" + versionSuffix );
                    if ( Files.isDirectory( javaDir ) )
                    {
                        javaDirs.add( javaDir );
                        discoverBundles( nonPlatformBundles, javaDir );
                    }
                }
            }
        }

        filterPaths( internalBundles, externalBundles, eclipseDirs, nonPlatformBundles, platformBundles );
        filterPaths( internalFeatures, externalFeatures, eclipseDirs, nonPlatformFeatures, platformFeatures );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Eclipse directories in use:" );
            for ( Path dir : eclipseDirs )
                logger.debug( "  * {}", dir );

            logger.debug( "Java directories in use:" );
            for ( Path dir : javaDirs )
                logger.debug( "  * {}", dir );
        }

        Repository systemRepo = Repository.createTemp();

        logger.info( "Publishing platform bundles and features..." );
        Director.publish( systemRepo, platformBundles, platformFeatures );
        platformUnits.addAll( systemRepo.getAllUnits() );

        logger.info( "Publishing internal bundles and features..." );
        Director.publish( systemRepo, internalBundles, internalFeatures );
        internalUnits.addAll( systemRepo.getAllUnits() );

        logger.info( "Publishing external bundles and features..." );
        Director.publish( systemRepo, externalBundles, externalFeatures );
        externalUnits.addAll( systemRepo.getAllUnits() );

        externalUnits.removeAll( internalUnits );
        internalUnits.removeAll( platformUnits );

        if ( externalFeatures.size() > 0 )
            throw new RuntimeException( "External features are not supported: "
                + Arrays.toString( externalFeatures.toArray() ) );

        logger.info( "Creating external bundle index..." );
        for ( Path path : externalBundles )
        {
            try (JarInputStream jar = new JarInputStream( Files.newInputStream( path ) ))
            {
                Manifest manifest = jar.getManifest();
                if ( manifest == null )
                    continue;

                Attributes mainAttributes = manifest.getMainAttributes();
                String id = mainAttributes.getValue( "Bundle-SymbolicName" ).replaceAll( " *;.*$", "" );
                String version = mainAttributes.getValue( "Bundle-Version" ).replaceAll( " *;.*$", "" );
                IVersionedId key = new VersionedId( id, version );
                index.put( key, path );
                logger.debug( "Indexed {} => {}", key, path );
            }
            catch ( Exception e )
            {
                // Ignore
            }
        }
    }

    /**
     * Enumerate all platform installable units available in the system.
     * <p>
     * Platform units are installable units included as part of Eclipse Platform. They are assumed to always be
     * available.
     * 
     * @return set of platform installable units available in the system
     */
    public Set<IInstallableUnit> getPlatformUnits()
    {
        return Collections.unmodifiableSet( platformUnits );
    }

    /**
     * Enumerate all internal installable units available in the system.
     * <p>
     * Internal units are all installable units which are not part of Eclipse Platform, but are located in locations
     * from which they will be automatically discovered by Eclipse during startup.
     * 
     * @return set of internal installable units available in the system
     */
    public Set<IInstallableUnit> gitInternalUnits()
    {
        return Collections.unmodifiableSet( internalUnits );
    }

    /**
     * Enumerate all external installable units available in the system.
     * <p>
     * External units are all installable units which are not discovered nor loaded by Eclipse during startup - to be
     * used they need to be symlinked from one of locations searched by Eclipse.
     * 
     * @return set of external installable units available in the system
     */
    public Set<IInstallableUnit> getExternalUnits()
    {
        return Collections.unmodifiableSet( externalUnits );
    }

    /**
     * Locate external OSGi bundle in the system.
     * 
     * @return absolute path to the bundle (can be {@code null} if no bundle with requested coordinates is available)
     */
    public Path lookupBundle( IVersionedId key )
    {
        return index.get( new VersionedId( key.getId(), key.getVersion() ) );
    }
}
