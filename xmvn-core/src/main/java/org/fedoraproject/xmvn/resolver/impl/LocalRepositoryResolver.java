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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * Resolver that resolves artifacts from local and bisect repositories.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "local-repo" )
@Singleton
public class LocalRepositoryResolver
    implements Resolver
{
    private Path getMavenRepositoryPath( Artifact artifact )
    {
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();

        StringBuilder path = new StringBuilder();

        path.append( groupId.replace( '.', '/' ) ).append( '/' );

        path.append( artifactId );

        path.append( '/' ).append( version );

        path.append( '/' ).append( artifactId );

        path.append( '-' ).append( version );

        if ( !classifier.isEmpty() )
            path.append( '-' ).append( classifier );

        if ( !extension.isEmpty() )
            path.append( '.' ).append( extension );

        return Paths.get( path.toString() );
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        Artifact artifact = request.getArtifact();
        Path repoPath = getMavenRepositoryPath( artifact );

        // TODO: bisect
        Path repoRoot = Paths.get( ".m2" ).toAbsolutePath();

        Path artifactPath = repoRoot.resolve( repoPath );
        if ( Files.isRegularFile( artifactPath ) )
        {
            DefaultResolutionResult result = new DefaultResolutionResult( artifactPath );
            result.setCompatVersion( artifact.getVersion() );
            return result;
        }

        return new DefaultResolutionResult();
    }
}
