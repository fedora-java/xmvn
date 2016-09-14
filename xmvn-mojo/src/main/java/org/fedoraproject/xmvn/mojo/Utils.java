/*-
 * Copyright (c) 2013-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactType;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
final class Utils
{
    private Utils()
    {
        // Avoid generating default public constructor
    }

    public static Artifact aetherArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
    {
        String groupId = mavenArtifact.getGroupId();
        String artifactId = mavenArtifact.getArtifactId();
        String version = mavenArtifact.getVersion();

        ArtifactHandler handler = mavenArtifact.getArtifactHandler();
        String extension = handler.getExtension();
        String classifier = handler.getClassifier();
        if ( StringUtils.isNotEmpty( mavenArtifact.getClassifier() ) )
            classifier = mavenArtifact.getClassifier();

        File artifactFile = mavenArtifact.getFile();
        Path artifactPath = artifactFile != null ? artifactFile.toPath() : null;

        Artifact artifact = new DefaultArtifact( groupId, artifactId, extension, classifier, version );
        artifact = artifact.setPath( artifactPath );
        return artifact;
    }

    public static Artifact dependencyArtifact( RepositorySystemSession repoSession, Dependency dependency )
    {
        ArtifactTypeRegistry registry = ArtifactTypeRegistry.getDefaultRegistry();

        if ( repoSession != null && dependency.getType() != null )
        {
            ArtifactType type = repoSession.getArtifactTypeRegistry().get( dependency.getType() );

            if ( type != null )
            {
                registry =
                    registry.registerStereotype( dependency.getType(), type.getExtension(), type.getClassifier() );
            }
        }

        return registry.createTypedArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(),
                                             dependency.getClassifier(), dependency.getVersion() );
    }

    /**
     * Convert a collection of artifacts to a human-readable string.
     * 
     * @param collection collection of artifacts
     * @param multiLine if multi-line representation should be used instead of single-line
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> collection, boolean multiLine )
    {
        if ( collection.isEmpty() )
            return "[]";

        String separator = multiLine ? System.lineSeparator() : " ";
        String indent = multiLine ? "  " : "";

        String str = collection.stream().map( a -> indent + a ).collect( Collectors.joining( "," + separator ) );

        return "[" + separator + str + separator + "]";
    }
}
