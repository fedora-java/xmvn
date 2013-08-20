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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.aether.artifact.AbstractArtifact;
import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactImpl
    extends AbstractArtifact
{
    public static final String DEFAULT_VERSION = "SYSTEM";

    private final String groupId;

    private final String artifactId;

    private final String extension;

    private final String classifier;

    private final String version;

    private final File file;

    private final Map<String, String> properties;

    /**
     * Dummy artifact. Any dependencies on this artifact will be removed during model validation.
     */
    public static final ArtifactImpl DUMMY = new ArtifactImpl( "org.fedoraproject.xmvn", "xmvn-void" );

    /**
     * The same as {@code DUMMY}, but in JPP style. Any dependencies on this artifact will be removed during model
     * validation.
     */
    public static final ArtifactImpl DUMMY_JPP = new ArtifactImpl( "JPP/maven", "empty-dep" );

    private static final String KEY_SCOPE = "xmvn.artifact.scope";

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
        if ( extension == null )
            extension = "jar";
        if ( version == null )
            version = DEFAULT_VERSION;

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.classifier = "";
        this.version = version;
        this.file = null;
        this.properties = Collections.emptyMap();
    }

    public ArtifactImpl( Artifact artifact )
    {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.extension = artifact.getExtension();
        this.classifier = artifact.getClassifier();
        this.version = artifact.getVersion();
        this.file = artifact.getFile();
        this.properties = copyProperties( artifact.getProperties() );
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
        return getProperty( KEY_SCOPE, "" );
    }

    public Artifact setScope( String scope )
    {
        Map<String, String> properties = new HashMap<>( getProperties() );
        properties.put( KEY_SCOPE, scope );
        return setProperties( properties );
    }

    public boolean isJppArtifact()
    {
        return groupId.equals( "JPP" ) || groupId.startsWith( "JPP/" );
    }

    public boolean isPom()
    {
        return getExtension().equals( "pom" );
    }

    public ArtifactImpl clearVersion()
    {
        return new ArtifactImpl( groupId, artifactId, null, extension );
    }

    public ArtifactImpl clearVersionAndExtension()
    {
        return new ArtifactImpl( groupId, artifactId );
    }

    /**
     * Convert a collection of artifacts to a human-readable string. This function uses single-line representation.
     * 
     * @param collection collection of artifacts
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> set )
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
    public static String collectionToString( Collection<Artifact> collection, boolean multiLine )
    {
        if ( collection.isEmpty() )
            return "[]";

        String separator = multiLine ? System.lineSeparator() : " ";
        String indent = multiLine ? "  " : "";

        StringBuilder sb = new StringBuilder();
        sb.append( "[" + separator );

        Iterator<Artifact> iter = collection.iterator();
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
