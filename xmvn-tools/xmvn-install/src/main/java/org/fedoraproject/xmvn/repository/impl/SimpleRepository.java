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
package org.fedoraproject.xmvn.repository.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Stereotype;
import org.fedoraproject.xmvn.repository.RepositoryPath;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepository
    extends AbstractRepository
{
    private final Path root;

    private final List<Stereotype> stereotypes;

    public SimpleRepository( String namespace, Path root, List<Stereotype> stereotypes )
    {
        super( namespace );
        this.root = root;
        this.stereotypes = new ArrayList<>( stereotypes );
    }

    protected abstract Path getArtifactPath( String groupId, String artifactId, String extension, String classifier,
                                             String version );

    private boolean matchesStereotypes( Artifact artifact, boolean ignoreType )
    {
        for ( Stereotype stereotype : stereotypes )
        {
            if ( ( stereotype.getExtension() == null || stereotype.getExtension().equals( artifact.getExtension() ) )
                && ( stereotype.getClassifier() == null || stereotype.getClassifier().equals( artifact.getClassifier() ) ) )
            {
                return true;
            }
        }

        return stereotypes.isEmpty();
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType )
    {
        if ( !matchesStereotypes( artifact, ignoreType ) )
            return null;

        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        if ( version.equals( Artifact.DEFAULT_VERSION ) )
            version = null;

        Path path = getArtifactPath( groupId, artifactId, extension, classifier, version );
        if ( path == null )
            return null;

        if ( root != null )
            path = root.resolve( path );

        return new DefaultRepositoryPath( path, this );
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifacts, boolean ignoreType )
    {
        List<RepositoryPath> paths = new ArrayList<>();

        for ( Artifact artifact : artifacts )
        {
            RepositoryPath path = getPrimaryArtifactPath( artifact, ignoreType );
            if ( path != null )
                paths.add( path );
        }

        return Collections.unmodifiableList( paths );
    }
}
