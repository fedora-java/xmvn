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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.osgi.OSGiConfigurator;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultOSGiConfigurator
    implements OSGiConfigurator
{
    private final Logger logger = LoggerFactory.getLogger( DefaultOSGiConfigurator.class );

    private static final Artifact BUNDLES_EXTERNAL = new DefaultArtifact( "org.eclipse.tycho",
                                                                          "tycho-bundles-external", "zip", "SYSTEM" );

    private static final Artifact XMVN_P2_BUNDLE = new DefaultArtifact( "org.fedoraproject.xmvn",
                                                                        "org.fedoraproject.xmvn.p2.impl", "SYSTEM" );

    private final Resolver resolver;

    @Inject
    public DefaultOSGiConfigurator( Resolver resolver )
    {
        this.resolver = resolver;
    }

    @Override
    public Collection<Path> getBundles()
    {
        try
        {
            Set<Path> bundles = new LinkedHashSet<>();

            Path xmvnP2Bundle = resolver.resolve( new ResolutionRequest( XMVN_P2_BUNDLE ) ).getArtifactPath();
            if ( xmvnP2Bundle == null )
                throw new RuntimeException( "Unable to locate " + XMVN_P2_BUNDLE );

            logger.debug( "Using XMvn OSGi bundle: {}", xmvnP2Bundle );
            bundles.add( xmvnP2Bundle );

            Path bundlesExternal = resolver.resolve( new ResolutionRequest( BUNDLES_EXTERNAL ) ).getArtifactPath();
            if ( bundlesExternal == null )
                throw new RuntimeException( "Unable to locate " + BUNDLES_EXTERNAL );
            logger.debug( "Using bundles from: {}", bundlesExternal );

            Path tempDir = Files.createTempDirectory( "xmvn-p2-equinox-" );
            tempDir.toFile().deleteOnExit();
            unzip( bundlesExternal, tempDir );
            Path installationPath = tempDir.resolve( "eclipse" ).resolve( "plugins" );

            for ( Path file : Files.newDirectoryStream( installationPath ) )
            {
                if ( file.getFileName().toString().startsWith( "org.eclipse.osgi_" ) )
                    continue;

                logger.debug( "Using external OSGi bundle: {}", file );
                bundles.add( file );
            }

            return Collections.unmodifiableCollection( bundles );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to extract Equinox runtime", e );
        }
    }

    @Override
    public Collection<String> getExportedPackages()
    {
        return Arrays.asList( "org.fedoraproject.xmvn.p2", "org.slf4j" );
    }

    private void unzip( Path zip, Path dest )
        throws IOException
    {
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream( Files.newInputStream( zip ) ))
        {

            ZipEntry ze;
            while ( ( ze = zis.getNextEntry() ) != null )
            {
                Path newFile = dest.resolve( Paths.get( ze.getName() ) );

                if ( ze.isDirectory() )
                {
                    Files.createDirectories( newFile );
                }
                else
                {
                    Files.createDirectories( newFile.getParent() );

                    try (OutputStream fos = Files.newOutputStream( newFile ))
                    {
                        int len;
                        while ( ( len = zis.read( buffer ) ) > 0 )
                            fos.write( buffer, 0, len );
                    }
                }
            }
        }
    }
}
