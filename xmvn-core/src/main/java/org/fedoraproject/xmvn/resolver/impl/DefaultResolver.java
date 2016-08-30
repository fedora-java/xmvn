/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Default implementation of XMvn {@code Resolver} interface.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultResolver
    implements Resolver
{
    private final Logger logger = LoggerFactory.getLogger( DefaultResolver.class );

    private final MetadataResolver metadataResolver;

    private static final RpmDb rpmdb = new RpmDb();

    private final Resolver localRepoResolver;

    private final EffectivePomGenerator pomGenerator;

    private final CacheManager cacheManager;

    private final MockAgent mockAgent;

    private final Bisect bisect;

    @Inject
    public DefaultResolver( @Named( "local-repo" ) Resolver localRepoResolver, Configurator configurator )
    {
        this.localRepoResolver = localRepoResolver;

        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        metadataResolver = new MetadataResolver( settings.getMetadataRepositories() );
        pomGenerator = new EffectivePomGenerator();
        cacheManager = new CacheManager();
        mockAgent = new MockAgent();
        bisect = new Bisect();
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        if ( bisect.tryBisect() )
            return new DefaultResolutionResult();

        Properties properties = new Properties();
        properties.putAll( System.getProperties() );

        ResolutionResult localRepoResult = localRepoResolver.resolve( request );
        if ( localRepoResult.getArtifactPath() != null )
            return localRepoResult;

        Artifact artifact = request.getArtifact();
        logger.debug( "Trying to resolve artifact {}", artifact );

        String compatVersion;
        ArtifactMetadata metadata = metadataResolver.resolveArtifactMetadata( artifact );

        if ( metadata == null )
        {
            metadata = metadataResolver.resolveArtifactMetadata( artifact.setVersion( Artifact.DEFAULT_VERSION ) );
            compatVersion = null;
        }
        else
        {
            compatVersion = artifact.getVersion();
        }

        if ( metadata == null && mockAgent.tryInstallArtifact( artifact ) )
        {
            metadataResolver.invalidateMappings();
            metadata = metadataResolver.resolveArtifactMetadata( artifact );

            if ( metadata == null )
            {
                metadata = metadataResolver.resolveArtifactMetadata( artifact.setVersion( Artifact.DEFAULT_VERSION ) );
                compatVersion = null;
            }
            else
            {
                compatVersion = artifact.getVersion();
            }
        }

        if ( metadata == null )
        {
            logger.debug( "Failed to resolve artifact: {}", artifact );
            return new DefaultResolutionResult();
        }

        properties.putAll( metadata.getProperties() );

        if ( !Objects.equal( properties.getProperty( "xmvn.resolver.disableEffectivePom" ), "true" )
            && Objects.equal( metadata.getExtension(), "pom" )
            && ( !Objects.equal( properties.getProperty( "type" ), "pom" ) || metadata.getPath() == null ) )
        {
            try
            {
                Path pomPath = pomGenerator.generateEffectivePom( metadata, artifact );

                if ( request.isPersistentFileNeeded() )
                {
                    pomPath = cacheManager.cacheFile( pomPath );
                }

                metadata.setPath( pomPath.toString() );
            }
            catch ( IOException e )
            {
                logger.warn( "Failed to generate effective POM", e );
                return new DefaultResolutionResult();
            }
        }

        Path artifactPath = Paths.get( metadata.getPath() );
        try
        {
            artifactPath = artifactPath.toRealPath();
        }
        catch ( IOException e )
        {
            // Ignore
        }

        DefaultResolutionResult result = new DefaultResolutionResult( artifactPath );
        result.setNamespace( metadata.getNamespace() );
        result.setCompatVersion( compatVersion );
        if ( request.isProviderNeeded() )
            result.setProvider( rpmdb.lookupPath( artifactPath ) );

        logger.debug( "Artifact {} was resolved to {}", artifact, artifactPath );
        return result;
    }
}
