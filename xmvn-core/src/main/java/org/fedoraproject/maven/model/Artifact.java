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

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Mikolaj Izdebski
 */
public class Artifact
    implements Comparable<Artifact>
{
    private static final String DEFAULT_VERSION = "SYSTEM";

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String extension;

    /**
     * Dummy artifact. Any dependencies on this artifact will be removed during model validation.
     */
    public static final Artifact DUMMY = new Artifact( "org.fedoraproject.xmvn", "xmvn-void" );

    /**
     * The same as {@code DUMMY}, but in JPP style. Any dependencies on this artifact will be removed during model
     * validation.
     */
    public static final Artifact DUMMY_JPP = new Artifact( "JPP/maven", "empty-dep" );

    public Artifact( String groupId, String artifactId )
    {
        this( groupId, artifactId, null );
    }

    public Artifact( String groupId, String artifactId, String version )
    {
        this( groupId, artifactId, version, null );
    }

    public Artifact( String groupId, String artifactId, String version, String extension )
    {
        if ( groupId == null )
            throw new IllegalArgumentException( "groupId may not be null" );
        if ( artifactId == null )
            throw new IllegalArgumentException( "artifactId may not be null" );

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.extension = extension;
    }

    @Override
    public int compareTo( Artifact rhs )
    {
        if ( !groupId.equals( rhs.groupId ) )
            return groupId.compareTo( rhs.groupId );
        if ( !artifactId.equals( rhs.artifactId ) )
            return artifactId.compareTo( rhs.artifactId );
        if ( !getVersion().equals( rhs.getVersion() ) )
            return getVersion().compareTo( rhs.getVersion() );
        return getExtension().compareTo( rhs.getExtension() );
    }

    @Override
    public boolean equals( Object rhs )
    {
        return rhs != null && rhs instanceof Artifact && compareTo( (Artifact) rhs ) == 0;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        if ( version == null )
            return DEFAULT_VERSION;
        return version;
    }

    public String getExtension()
    {
        if ( extension == null )
            return "pom";
        return extension;
    }

    public boolean isJppArtifact()
    {
        return groupId.equals( "JPP" ) || groupId.startsWith( "JPP/" );
    }

    public boolean isPom()
    {
        return getExtension().equals( "pom" );
    }

    public Artifact clearExtension()
    {
        return new Artifact( groupId, artifactId, version );
    }

    public Artifact clearVersionAndExtension()
    {
        return new Artifact( groupId, artifactId );
    }

    public Artifact copyMissing( Artifact rhs )
    {
        String version = this.version != null ? this.version : rhs.version;
        String extension = this.extension != null ? this.extension : rhs.extension;

        return new Artifact( groupId, artifactId, version, extension );
    }

    /**
     * Convert this artifact into human-readable string.
     * 
     * @return string representation this artifact
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append( '[' );

        result.append( groupId );
        result.append( ':' );
        result.append( artifactId );

        if ( version != null )
        {
            result.append( ':' );
            result.append( version );
        }

        if ( extension != null )
        {
            result.append( ':' );
            result.append( extension );
        }

        result.append( ']' );
        return result.toString();
    }

    @Override
    public int hashCode()
    {
        return 42;
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
