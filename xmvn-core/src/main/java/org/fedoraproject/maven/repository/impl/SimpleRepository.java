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
package org.fedoraproject.maven.repository.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.Stereotype;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepository
    implements Repository
{
    private Path root;

    private final List<Stereotype> stereotypes = new ArrayList<>();

    protected abstract Path getArtifactPath( String groupId, String artifactId, String extension, String classifier,
                                             String version );

    private boolean matchesStereotypes( Artifact artifact )
    {
        for ( Stereotype stereotype : stereotypes )
        {
            String type = ArtifactUtils.getStereotype( artifact );

            if ( ( stereotype.getExtension() == null || stereotype.getExtension().equals( artifact.getExtension() ) )
                && ( stereotype.getClassifier() == null || stereotype.getClassifier().equals( artifact.getClassifier() ) )
                && ( StringUtils.isEmpty( type ) || stereotype.getType() == null || stereotype.getType().equals( type ) ) )
            {
                return true;
            }
        }

        return stereotypes.isEmpty();
    }

    @Override
    public Path getPrimaryArtifactPath( Artifact artifact )
    {
        if ( !matchesStereotypes( artifact ) )
            return null;

        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        if ( version.equals( ArtifactUtils.DEFAULT_VERSION ) )
            version = null;

        Path path = getArtifactPath( groupId, artifactId, extension, classifier, version );
        if ( path != null && root != null )
            path = root.resolve( path );

        return path;
    }

    @Override
    public List<Path> getArtifactPaths( Artifact artifact )
    {
        return getArtifactPaths( Collections.singletonList( artifact ) );
    }

    @Override
    public List<Path> getArtifactPaths( List<Artifact> artifacts )
    {
        List<Path> paths = new ArrayList<>();

        for ( Artifact artifact : artifacts )
        {
            Path path = getPrimaryArtifactPath( artifact );
            if ( path != null )
                paths.add( path );
        }

        return Collections.unmodifiableList( paths );
    }

    @Override
    public void configure( List<Stereotype> stereotypes, Properties properties, Xpp3Dom configuration )
    {
        String rootProperty = properties.getProperty( "root" );
        root = rootProperty != null ? Paths.get( rootProperty ) : null;
        this.stereotypes.clear();
        this.stereotypes.addAll( stereotypes );
    }
}
