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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
import org.fedoraproject.maven.utils.FileUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * Default implementation of XMvn {@code Resolver} interface.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
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

    private DefaultResolutionResult tryResolveFromBisectRepo( Artifact artifact )
    {
        try
        {
            if ( bisectCounter == null || bisectRepo == null || bisectCounter.tryDecrement() == 0 )
                return null;

            File artifactFile = bisectRepo.getPrimaryArtifactPath( artifact ).getPath().toFile();
            if ( artifactFile.exists() )
                return new DefaultResolutionResult( artifactFile, bisectRepo );

            return new DefaultResolutionResult();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Translate artifact to list of JPP artifacts (there may be more than one JPP artifacts if there are multiple
     * depmap entries for the same artifact, like when two packages install the same artifact, but different version).
     * Depmaps are always versionless, so use versionless artifact. Returned JPP artifacts are also versionless.
     * 
     * @param artifact Maven artifact to translate
     * @return list of versionless JPP artifacts corresponding to given artifact
     */
    private List<Artifact> getJppArtifactList( Artifact artifact )
    {
        Artifact versionlessArtifact = artifact.setVersion( ArtifactUtils.DEFAULT_VERSION );
        Set<Artifact> jppArtifacts = new LinkedHashSet<>( depmap.translate( versionlessArtifact ) );

        // For POM artifacts besides standard mapping we need to use backwards-compatible mappings too. We set extension
        // to "jar", translate the artifact using depmaps and set extensions back to "pom".
        if ( artifact.getExtension().equals( "pom" ) )
        {
            Artifact jarArtifact =
                new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), "jar",
                                     ArtifactUtils.DEFAULT_VERSION );
            for ( Artifact jppJarArtifact : depmap.translate( jarArtifact ) )
            {
                Artifact jppPomArtifact =
                    new DefaultArtifact( jppJarArtifact.getGroupId(), jppJarArtifact.getArtifactId(),
                                         jppJarArtifact.getClassifier(), "pom", ArtifactUtils.DEFAULT_VERSION );
                jppArtifacts.add( jppPomArtifact );
            }
        }

        return new ArrayList<>( jppArtifacts );
    }

    private DefaultResolutionResult tryResolveFromJavaHome( List<Artifact> jppArtifacts )
    {
        String javaHome = System.getProperty( "java.home" );
        if ( javaHome == null )
            return null;

        Path javaHomeDir = followSymlink( new File( javaHome ) ).toPath();

        for ( Artifact jppArtifact : jppArtifacts )
        {
            if ( jppArtifact.getGroupId().equals( "JAVA_HOME" ) )
            {
                Path artifactPath = Paths.get( jppArtifact.getArtifactId() + "." + jppArtifact.getExtension() );
                File artifactFile = javaHomeDir.resolve( artifactPath ).toFile();
                artifactFile = followSymlink( artifactFile );
                if ( artifactFile.exists() )
                    return new DefaultResolutionResult( artifactFile );
            }
        }

        return null;
    }

    private DefaultResolutionResult tryResolveFromConfiguredRepos( List<Artifact> jppArtifacts, String requestedVersion )
    {
        List<String> versionList = Arrays.asList( requestedVersion, ArtifactUtils.DEFAULT_VERSION );
        Set<String> orderedVersionSet = new LinkedHashSet<>( versionList );

        for ( String version : orderedVersionSet )
        {
            for ( ListIterator<Artifact> it = jppArtifacts.listIterator(); it.hasNext(); )
                it.set( it.next().setVersion( version ) );

            for ( RepositoryPath repoPath : systemRepo.getArtifactPaths( jppArtifacts, true ) )
            {
                File artifactFile = repoPath.getPath().toFile();
                logger.debug( "Checking artifact path: " + artifactFile );
                if ( artifactFile.exists() )
                {
                    DefaultResolutionResult result = new DefaultResolutionResult( artifactFile );
                    result.setCompatVersion( version );
                    result.setRepository( repoPath.getRepository() );
                    return result;
                }
            }
        }

        return null;
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        Artifact artifact = request.getArtifact();
        logger.debug( "Trying to resolve artifact " + artifact );

        List<Artifact> jppArtifacts = getJppArtifactList( artifact );
        logger.debug( "JPP artifacts considered during resolution: " + ArtifactUtils.collectionToString( jppArtifacts ) );
        jppArtifacts.add( artifact );

        DefaultResolutionResult result = tryResolveFromBisectRepo( artifact );
        if ( result == null )
            result = tryResolveFromJavaHome( jppArtifacts );
        if ( result == null )
            result = tryResolveFromConfiguredRepos( jppArtifacts, artifact.getVersion() );

        if ( result == null )
        {
            logger.warn( "Failed to resolve artifact: " + artifact );
            return new DefaultResolutionResult();
        }

        File artifactFile = result.getArtifactFile();
        artifactFile = FileUtils.followSymlink( artifactFile );
        logger.debug( "Artifact " + artifact + " was resolved to " + artifactFile );
        if ( request.isProviderNeeded() )
            result.setProvider( rpmdb.lookupFile( artifactFile ) );

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
