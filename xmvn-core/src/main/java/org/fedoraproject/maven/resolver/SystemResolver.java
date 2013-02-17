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

import static org.fedoraproject.maven.utils.Logger.debug;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fedoraproject.maven.config.ConfigurationXXX;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.JppRepository;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.rpmquery.RpmDb;

class SystemResolver
    extends AbstractResolver
{
    private final Repository systemRepo;

    private final DependencyMap depmap;

    private static final RpmDb rpmdb = new RpmDb();

    private static final Set<String> usedRpmPackages = new TreeSet<>();

    public SystemResolver( File root )
    {
        systemRepo = new JppRepository( root );
        depmap = DepmapReader.readArtifactMap( root );
    }

    @Override
    public File resolve( Artifact artifact )
    {
        debug( "Resolving ", artifact );
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
            debug( "Failed to resolve artifact ", artifact );
            return null;
        }

        debug( "Artifact ", artifact, " was resolved to ", file );

        if ( ConfigurationXXX.getConfiguration().getBuildSettings().isDebug() )
        {
            String rpmPackage = rpmdb.lookupFile( file );
            if ( rpmPackage != null )
            {
                usedRpmPackages.add( rpmPackage );
                debug( "Artifact ", artifact, " is provided by ", rpmPackage );
            }
            else
            {
                debug( "Artifact ", artifact, " is not provided by any package" );
            }
        }

        return file;
    }

    public static void printInvolvedPackages()
    {
        if ( !usedRpmPackages.isEmpty() )
        {
            debug( "Packages involved in artifact resolution:" );
            for ( String pkg : usedRpmPackages )
                debug( "  * ", pkg );
        }
    }
}
