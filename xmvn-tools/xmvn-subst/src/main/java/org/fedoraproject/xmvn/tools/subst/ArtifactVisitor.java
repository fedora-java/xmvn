/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.utils.ArtifactUtils;
import org.fedoraproject.xmvn.utils.FileUtils;

@Named
@Singleton
public class ArtifactVisitor
    implements FileVisitor<Path>
{
    private final Logger logger = LoggerFactory.getLogger( ArtifactVisitor.class );

    private final Set<String> types = new LinkedHashSet<>();

    private final Resolver resolver;

    private boolean followSymlinks;

    private boolean dryRun;

    private int failureCount;

    @Inject
    public ArtifactVisitor( Resolver resolver )
    {
        this.resolver = resolver;
    }

    public void setTypes( Collection<String> types )
    {
        this.types.addAll( types );
    }

    public void setFollowSymlinks( boolean followSymlinks )
    {
        this.followSymlinks = followSymlinks;
    }

    public void setDryRun( boolean dryRun )
    {
        this.dryRun = dryRun;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    @Override
    public FileVisitResult postVisitDirectory( Path path, IOException e )
        throws IOException
    {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory( Path path, BasicFileAttributes attrs )
        throws IOException
    {
        if ( Files.isSymbolicLink( path ) && !followSymlinks )
        {
            logger.debug( "Skipping symlink to directory: {}", path );
            return FileVisitResult.SKIP_SUBTREE;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile( Path path, BasicFileAttributes attrs )
        throws IOException
    {
        if ( !Files.isRegularFile( path ) )
        {
            logger.debug( "Skipping {}: not a regular file", path );
            return FileVisitResult.CONTINUE;
        }

        String fileName = path.getFileName().toString();

        for ( String type : types )
        {
            if ( fileName.endsWith( "." + type ) )
            {
                substituteArtifact( path, type );
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed( Path path, IOException e )
        throws IOException
    {
        logger.warn( "Failed to access file", e );
        return FileVisitResult.CONTINUE;
    }

    private void substituteArtifact( Path path, String type )
        throws IOException
    {
        Artifact artifact = ArtifactUtils.readArtifactDefinition( path, type );
        if ( artifact == null )
        {
            logger.info( "Skipping file {}: No artifact definition found", path );
            failureCount++;
            return;
        }

        Path artifactPath = resolver.resolve( new ResolutionRequest( artifact ) ).getArtifactPath();
        if ( artifactPath == null )
        {
            logger.warn( "Skipping file {}: Artifact {} not found in repository", path, artifact );
            failureCount++;
            return;
        }

        if ( !dryRun )
            FileUtils.replaceFileWithSymlink( path, artifactPath );
        logger.info( "Linked {} to {}", path, artifactPath );
    }
}
