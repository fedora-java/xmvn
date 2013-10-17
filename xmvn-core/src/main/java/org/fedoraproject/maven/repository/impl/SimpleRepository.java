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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.Stereotype;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryPath;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepository
    implements Repository
{
    private Path root;

    private String namespace;

    private final List<Stereotype> stereotypes = new ArrayList<>();

    protected abstract Path getArtifactPath( String groupId, String artifactId, String extension, String classifier,
                                             String version );

    private boolean matchesStereotypes( Artifact artifact )
    {
        String type = ArtifactUtils.getStereotype( artifact );

        for ( Stereotype stereotype : stereotypes )
        {
            if ( ( stereotype.getExtension() == null || stereotype.getExtension().equals( artifact.getExtension() ) )
                && ( stereotype.getClassifier() == null || stereotype.getClassifier().equals( artifact.getClassifier() ) )
                && ( stereotype.getType() == null || ( stereotype.getType().equals( type ) ) ) )
            {
                return true;
            }
        }

        return stereotypes.isEmpty();
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact )
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
        if ( path == null )
            return null;

        if ( root != null )
            path = root.resolve( path );

        return new DefaultRepositoryPath( path, this );
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( Artifact artifact )
    {
        return getArtifactPaths( Collections.singletonList( artifact ) );
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifacts )
    {
        List<RepositoryPath> paths = new ArrayList<>();

        for ( Artifact artifact : artifacts )
        {
            RepositoryPath path = getPrimaryArtifactPath( artifact );
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

        namespace = properties.getProperty( "namespace", "" );

        this.stereotypes.clear();
        this.stereotypes.addAll( stereotypes );
    }

    @Override
    public String getNamespace()
    {
        return namespace;
    }

    @Override
    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }
}
