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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.JppRepository;
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

    private static final Set<String> usedRpmPackages = new TreeSet<>();

    public SystemResolver( File root, ResolverSettings settings, Logger logger )
    {
        this.settings = settings;
        this.logger = logger;
        systemRepo = new JppRepository( root, settings );
        depmap = DepmapReader.readArtifactMap( root, settings, logger );
    }

    @Override
    public File resolve( Artifact artifact )
    {
        logger.debug( "Resolving " + artifact );
        List<Artifact> jppList = depmap.translate( artifact.clearVersionAndExtension() );

        File file = null;
        outer: for ( boolean versioned : new Boolean[] { true, false } )
        {
            for ( Artifact jppArtifact : jppList )
            {
                jppArtifact = jppArtifact.clearVersionAndExtension().copyMissing( artifact );
                file = systemRepo.findArtifact( jppArtifact, versioned );
                if ( file != null )
                    break outer;
            }
        }

        if ( file == null )
        {
            logger.debug( "Failed to resolve artifact " + artifact );
            return null;
        }

        logger.debug( "Artifact " + artifact + " was resolved to " + file );

        if ( settings.isDebug() )
        {
            String rpmPackage = rpmdb.lookupFile( file );
            if ( rpmPackage != null )
            {
                usedRpmPackages.add( rpmPackage );
                logger.debug( "Artifact " + artifact + " is provided by " + rpmPackage );
            }
            else
            {
                logger.debug( "Artifact " + artifact + " is not provided by any package" );
            }
        }

        return file;
    }

    public static void printInvolvedPackages( Logger logger )
    {
        if ( !usedRpmPackages.isEmpty() )
        {
            logger.debug( "Packages involved in artifact resolution:" );
            for ( String pkg : usedRpmPackages )
                logger.debug( "  * " + pkg );
        }
    }
}
