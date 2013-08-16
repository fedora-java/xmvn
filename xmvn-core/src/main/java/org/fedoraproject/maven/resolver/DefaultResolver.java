/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.resolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.utils.AtomicFileCounter;
import org.fedoraproject.maven.utils.LoggingUtils;
import org.fedoraproject.rpmquery.RpmDb;

/**
 * Default implementation of XMvn {@code Resolver} interface.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Resolver.class )
public class DefaultResolver
    extends AbstractResolver
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private RepositoryConfigurator repositoryConfigurator;

    private Repository bisectRepo;

    private Repository systemRepo;

    private DependencyMap depmap;

    private AtomicFileCounter bisectCounter;

    private boolean initialized;

    private ResolverSettings settings;

    private static final RpmDb rpmdb = new RpmDb();

    private void initializeBisect()
    {
        try
        {
            String bisectCounterPath = System.getProperty( "xmvn.bisect.counter" );
            if ( StringUtils.isEmpty( bisectCounterPath ) )
            {
                logger.debug( "Bisection build is not enabled" );
                return;
            }
            bisectCounter = new AtomicFileCounter( bisectCounterPath );

            bisectRepo = repositoryConfigurator.configureRepository( "bisect" );

            logger.info( "Enabled XMvn bisection build" );
        }
        catch ( IOException e )
        {
            logger.fatalError( "Unable to initialize XMvn bisection build", e );
            throw new RuntimeException( e );
        }
    }

    private void initialize()
    {
        initializeBisect();

        settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        systemRepo = repositoryConfigurator.configureRepository( "resolve" );

        List<String> metadataDirs = new ArrayList<>();
        for ( String prefix : settings.getPrefixes() )
        {
            File root = new File( prefix );
            if ( root.isDirectory() )
            {
                for ( String dir : settings.getMetadataRepositories() )
                    metadataDirs.add( new File( root, dir ).toString() );
            }
        }

        depmap = new DependencyMap( logger );
        DepmapReader reader = new DepmapReader();
        reader.readMappings( depmap, metadataDirs );

        initialized = true;
    }

    private boolean resolveFromBisectRepo()
    {
        if ( bisectCounter == null || bisectRepo == null )
            return false;

        try
        {
            return bisectCounter.tryDecrement() > 0;
        }
        catch ( IOException e )
        {
            logger.fatalError( "Failed to decrement bisection counter", e );
            throw new RuntimeException( e );
        }
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        if ( !initialized )
            initialize();

        Artifact artifact = request.getArtifact();

        if ( resolveFromBisectRepo() )
        {
            logger.debug( "Resolving artifact " + artifact + " from bisection repository." );
            File artifactFile = bisectRepo.getPrimaryArtifactPath( artifact ).toFile();
            return new DefaultResolutionResult( artifactFile, bisectRepo );
        }

        logger.debug( "Trying to resolve artifact " + artifact );

        List<Artifact> jppList = depmap.translate( artifact.clearVersionAndExtension() );

        Path path = null;
        String compatVersion = null;

        // TODO: this loop needs to be simplified not to use goto...
        notFound: for ( ;; )
        {
            if ( !artifact.isVersionless() )
            {
                List<Artifact> tempList = new ArrayList<>();
                compatVersion = artifact.getVersion();
                for ( Artifact jppArtifact : jppList )
                    tempList.add( jppArtifact.clearVersionAndExtension().copyMissing( artifact ) );
                for ( Path pp : systemRepo.getArtifactPaths( tempList ) )
                {
                    logger.debug( "Checking artifact path: " + pp );
                    if ( Files.exists( pp ) )
                    {
                        path = pp;
                        break notFound;
                    }
                }
            }

            {
                List<Artifact> tempList = new ArrayList<>();
                compatVersion = null;
                for ( Artifact jppArtifact : jppList )
                    tempList.add( jppArtifact.clearVersionAndExtension().copyMissing( artifact ).clearVersion() );
                for ( Path pp : systemRepo.getArtifactPaths( tempList ) )
                {
                    logger.debug( "Checking artifact path: " + pp );
                    if ( Files.exists( pp ) )
                    {
                        path = pp;
                        break notFound;
                    }
                }
            }

            logger.debug( "Failed to resolve artifact " + artifact );
            return new DefaultResolutionResult();
        }

        logger.debug( "Artifact " + artifact + " was resolved to " + path );
        DefaultResolutionResult result = new DefaultResolutionResult( path.toFile() );
        result.setCompatVersion( compatVersion );
        result.setRepository( systemRepo );

        if ( request.isProviderNeeded() || settings.isDebug() )
        {
            String rpmPackage = rpmdb.lookupFile( path.toFile() );
            if ( rpmPackage != null )
            {
                result.setProvider( rpmPackage );
                logger.debug( "Artifact " + artifact + " is provided by " + rpmPackage );
            }
            else
            {
                logger.debug( "Artifact " + artifact + " is not provided by any package" );
            }
        }

        return result;
    }
}
