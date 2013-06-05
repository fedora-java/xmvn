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
package org.fedoraproject.maven.resolver;

import static org.fedoraproject.maven.utils.FileUtils.followSymlink;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
class JavaHomeResolver
    extends AbstractResolver
{
    private final DependencyMap depmap;

    public JavaHomeResolver( File root, ResolverSettings settings, Logger logger )
    {
        depmap = DepmapReader.readArtifactMap( root, settings, logger );
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        for ( Artifact artifact : depmap.translate( request.getArtifact().clearVersionAndExtension() ) )
        {
            artifact = artifact.copyMissing( request.getArtifact() );
            String javaHome = System.getProperty( "java.home" );

            if ( artifact.getGroupId().equals( "JAVA_HOME" ) && javaHome != null )
            {
                Path javaHomeDir = followSymlink( new File( javaHome ) ).toPath();
                Path artifactPath = Paths.get( artifact.getArtifactId() + "." + artifact.getExtension() );
                File artifactFile = javaHomeDir.resolve( artifactPath ).toFile();
                artifactFile = followSymlink( artifactFile );
                if ( artifactFile.exists() )
                    return new DefaultResolutionResult( artifactFile );
            }
        }

        return new DefaultResolutionResult();
    }
}
