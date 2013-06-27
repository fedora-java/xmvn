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
import java.nio.file.Path;
import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.rpmquery.RpmDb;

/**
 * @author Mikolaj Izdebski
 */
class SystemResolver
    extends AbstractResolver
{
    private final Logger logger;

    private final ResolverSettings settings;

    private final Repository systemRepo;

    private final DependencyMap depmap;

    private static final RpmDb rpmdb = new RpmDb();

    public SystemResolver( File root, ResolverSettings settings, Logger logger )
    {
        this.settings = settings;
        this.logger = logger;
        systemRepo = null; // FIXME
        depmap = DepmapReader.readArtifactMap( root, settings, logger );
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        Artifact artifact = request.getArtifact();
        logger.debug( "Resolving " + artifact );
        List<Artifact> jppList = depmap.translate( artifact.clearVersionAndExtension() );

        Path path = null;
        String compatVersion = null;

        // TODO: this loop needs to be simplified not to use goto...
        notFound: for ( ;; )
        {
            for ( Artifact jppArtifact : jppList )
            {
                compatVersion = artifact.getVersion();
                jppArtifact = jppArtifact.clearVersionAndExtension().copyMissing( artifact );
                path = systemRepo.getArtifactPaths( jppArtifact ).iterator().next();
                if ( path != null )
                    break notFound;
            }

            compatVersion = null;
            for ( Artifact jppArtifact : jppList )
            {
                jppArtifact = jppArtifact.clearVersionAndExtension().copyMissing( artifact );
                jppArtifact = jppArtifact.clearVersion();
                path = systemRepo.getArtifactPaths( jppArtifact ).iterator().next();
                if ( path != null )
                    break notFound;
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
