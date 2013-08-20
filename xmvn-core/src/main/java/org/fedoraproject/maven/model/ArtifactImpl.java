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
package org.fedoraproject.maven.model;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.aether.artifact.AbstractArtifact;
import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactImpl
    extends AbstractArtifact
    implements Comparable<Artifact>
{
    private static final String DEFAULT_VERSION = "SYSTEM";

    private final String groupId;

    private final String artifactId;

    private final String extension;

    private final String classifier;

    private final String version;

    private final File file;

    private final Map<String, String> properties;

    private final String scope;

    /**
     * Dummy artifact. Any dependencies on this artifact will be removed during model validation.
     */
    public static final ArtifactImpl DUMMY = new ArtifactImpl( "org.fedoraproject.xmvn", "xmvn-void" );

    /**
     * The same as {@code DUMMY}, but in JPP style. Any dependencies on this artifact will be removed during model
     * validation.
     */
    public static final ArtifactImpl DUMMY_JPP = new ArtifactImpl( "JPP/maven", "empty-dep" );

    public ArtifactImpl( String groupId, String artifactId )
    {
        this( groupId, artifactId, null );
    }

    public ArtifactImpl( String groupId, String artifactId, String version )
    {
        this( groupId, artifactId, version, null );
    }

    public ArtifactImpl( String groupId, String artifactId, String version, String extension )
    {
        if ( groupId == null )
            throw new IllegalArgumentException( "groupId may not be null" );
        if ( artifactId == null )
            throw new IllegalArgumentException( "artifactId may not be null" );

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.classifier = null;
        this.version = version;
        this.file = null;
        this.properties = Collections.emptyMap();
        this.scope = null;
    }

    public ArtifactImpl( ArtifactImpl artifact, String scope )
    {
        this.groupId = artifact.groupId;
        this.artifactId = artifact.artifactId;
        this.extension = artifact.extension;
        this.classifier = null;
        this.version = artifact.version;
        this.file = null;
        this.properties = Collections.emptyMap();
        this.scope = scope;
    }

    @Override
    public int compareTo( Artifact rhs )
    {
        if ( !getGroupId().equals( rhs.getGroupId() ) )
            return getGroupId().compareTo( rhs.getGroupId() );
        if ( !getArtifactId().equals( rhs.getArtifactId() ) )
            return getArtifactId().compareTo( rhs.getArtifactId() );
        if ( !getVersion().equals( rhs.getVersion() ) )
            return getVersion().compareTo( rhs.getVersion() );
        return getExtension().compareTo( rhs.getExtension() );
    }

    @Override
    public String getGroupId()
    {
        return groupId;
    }

    @Override
    public String getArtifactId()
    {
        return artifactId;
    }

    @Override
    public String getExtension()
    {
        if ( extension == null )
            return "pom";
        return extension;
    }

    @Override
    public String getClassifier()
    {
        return classifier;
    }

    @Override
    public String getVersion()
    {
        if ( version == null )
            return DEFAULT_VERSION;
        return version;
    }

    @Override
    public File getFile()
    {
        return file;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public String getScope()
    {
        return scope;
    }

    public boolean isJppArtifact()
    {
        return groupId.equals( "JPP" ) || groupId.startsWith( "JPP/" );
    }

    public boolean isPom()
    {
        return getExtension().equals( "pom" );
    }

    public boolean isVersionless()
    {
        return version == null;
    }

    public ArtifactImpl clearVersion()
    {
        return new ArtifactImpl( groupId, artifactId, null, extension );
    }

    public ArtifactImpl clearExtension()
    {
        return new ArtifactImpl( groupId, artifactId, version );
    }

    public ArtifactImpl clearVersionAndExtension()
    {
        return new ArtifactImpl( groupId, artifactId );
    }

    public ArtifactImpl copyMissing( ArtifactImpl rhs )
    {
        String version = this.version != null ? this.version : rhs.version;
        String extension = this.extension != null ? this.extension : rhs.extension;

        return new ArtifactImpl( groupId, artifactId, version, extension );
    }

    /**
     * Convert a collection of artifacts to a human-readable string. This function uses single-line representation.
     * 
     * @param collection collection of artifacts
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<ArtifactImpl> set )
    {
        return collectionToString( set, false );
    }

    /**
     * Convert a collection of artifacts to a human-readable string.
     * 
     * @param collection collection of artifacts
     * @param multiLine if multi-line representation should be used instead of single-line
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<ArtifactImpl> collection, boolean multiLine )
    {
        if ( collection.isEmpty() )
            return "[]";

        String separator = multiLine ? System.lineSeparator() : " ";
        String indent = multiLine ? "  " : "";

        StringBuilder sb = new StringBuilder();
        sb.append( "[" + separator );

        Iterator<ArtifactImpl> iter = collection.iterator();
        sb.append( indent + iter.next() );

        while ( iter.hasNext() )
        {
            sb.append( "," + separator );
            sb.append( indent + iter.next() );
        }

        sb.append( separator + "]" );
        return sb.toString();
    }
}
