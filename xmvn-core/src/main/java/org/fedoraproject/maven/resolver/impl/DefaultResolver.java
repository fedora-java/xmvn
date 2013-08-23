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
package org.fedoraproject.maven.resolver.impl;

import static org.fedoraproject.maven.utils.FileUtils.followSymlink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryPath;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.resolver.ResolutionRequest;
import org.fedoraproject.maven.resolver.ResolutionResult;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.AtomicFileCounter;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * Default implementation of XMvn {@code Resolver} interface.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Resolver.class )
public class DefaultResolver
    implements Resolver, Initializable
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private RepositoryConfigurator repositoryConfigurator;

    private Repository bisectRepo;

    private Repository systemRepo;

    @Requirement
    private DependencyMap depmap;

    private AtomicFileCounter bisectCounter;

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

    @Override
    public void initialize()
    {
        initializeBisect();

        settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        systemRepo = repositoryConfigurator.configureRepository( "resolve" );
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
        Artifact artifact = request.getArtifact();

        if ( resolveFromBisectRepo() )
        {
            logger.debug( "Resolving artifact " + artifact + " from bisection repository." );
            File artifactFile = bisectRepo.getPrimaryArtifactPath( artifact ).getPath().toFile();
            return new DefaultResolutionResult( artifactFile, bisectRepo );
        }

        logger.debug( "Trying to resolve artifact " + artifact );

        List<Artifact> jppList =
            depmap.translate( new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                                   ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION ) );

        String javaHome = System.getProperty( "java.home" );
        Path javaHomeDir = followSymlink( new File( javaHome != null ? javaHome : "." ) ).toPath();

        for ( Artifact aa : jppList )
        {
            if ( aa.getGroupId().equals( "JAVA_HOME" ) && javaHome != null )
            {
                Path artifactPath = Paths.get( aa.getArtifactId() + "." + artifact.getExtension() );
                File artifactFile = javaHomeDir.resolve( artifactPath ).toFile();
                artifactFile = followSymlink( artifactFile );
                if ( artifactFile.exists() )
                    return new DefaultResolutionResult( artifactFile );
            }
        }

        RepositoryPath path = null;
        String compatVersion = null;

        // TODO: this loop needs to be simplified not to use goto...
        notFound: for ( ;; )
        {
            if ( !artifact.getVersion().equals( ArtifactUtils.DEFAULT_VERSION ) )
            {
                List<Artifact> tempList = new ArrayList<>();
                compatVersion = artifact.getVersion();
                for ( Artifact jppArtifact : jppList )
                    tempList.add( new DefaultArtifact( jppArtifact.getGroupId(), jppArtifact.getArtifactId(),
                                                       jppArtifact.getClassifier(), artifact.getExtension(),
                                                       artifact.getVersion() ) );
                for ( RepositoryPath rp : systemRepo.getArtifactPaths( tempList ) )
                {
                    logger.debug( "Checking artifact path: " + rp.getPath() );
                    if ( Files.exists( rp.getPath() ) )
                    {
                        path = rp;
                        break notFound;
                    }
                }
            }

            {
                List<Artifact> tempList = new ArrayList<>();
                compatVersion = null;
                for ( Artifact jppArtifact : jppList )
                    tempList.add( new DefaultArtifact( jppArtifact.getGroupId(), jppArtifact.getArtifactId(),
                                                       jppArtifact.getClassifier(), artifact.getExtension(),
                                                       ArtifactUtils.DEFAULT_VERSION ) );
                for ( RepositoryPath rp : systemRepo.getArtifactPaths( tempList ) )
                {
                    logger.debug( "Checking artifact path: " + rp );
                    if ( Files.exists( rp.getPath() ) )
                    {
                        path = rp;
                        break notFound;
                    }
                }
            }

            logger.debug( "Failed to resolve artifact " + artifact );
            return new DefaultResolutionResult();
        }

        logger.debug( "Artifact " + artifact + " was resolved to " + path );
        DefaultResolutionResult result = new DefaultResolutionResult( path.getPath().toFile() );
        result.setCompatVersion( compatVersion );
        result.setRepository( path.getRepository() );

        if ( request.isProviderNeeded() || settings.isDebug() )
        {
            String rpmPackage = rpmdb.lookupFile( path.getPath().toFile() );
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

    @Deprecated
    @Override
    public File resolve( String groupId, String artifactId, String version, String extension )
    {
        Artifact artifact = new DefaultArtifact( groupId, artifactId, extension, version );
        return resolve( artifact );
    }

    @Deprecated
    @Override
    public File resolve( Artifact artifact )
    {
        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolve( request );
        return result.getArtifactFile();
    }
}
