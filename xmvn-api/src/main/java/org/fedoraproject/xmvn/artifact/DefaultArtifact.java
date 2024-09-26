/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.artifact;

import java.nio.file.Path;

/**
 * @author Mikolaj Izdebski
 */
public final class DefaultArtifact
    implements Artifact
{
    private final String groupId;

    private final String artifactId;

    private final String extension;

    private final String classifier;

    private final String version;

    private final Path path;

    public DefaultArtifact( String coords )
    {
        String s = coords;
        int n = s.length() - s.replace( ":", "" ).length();
        if ( n < 1 || n > 4 )
        {
            throw new IllegalArgumentException( "Illegal artifact coordinates " + coords
                + ", expected coordinates in format <groupId>:<artifactId>[:<extension>[:<classifier>]]:[<version>]" );
        }
        s += "::::";
        String[] a = new String[5];
        for ( int j = 0; j < 5; j++ )
        {
            int i = s.indexOf( ':' );
            a[j] = s.substring( 0, i );
            s = s.substring( i + 1 );
        }

        groupId = a[0];
        artifactId = a[1];
        extension = n < 3 || a[2].isEmpty() ? DEFAULT_EXTENSION : a[2];
        classifier = n < 4 ? "" : a[3];
        version = n < 2 || a[n].isEmpty() ? DEFAULT_VERSION : a[n];
        path = null;
    }

    public DefaultArtifact( String groupId, String artifactId )
    {
        this( groupId, artifactId, null );
    }

    public DefaultArtifact( String groupId, String artifactId, String version )
    {
        this( groupId, artifactId, null, version );
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String version )
    {
        this( groupId, artifactId, extension, null, version );
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String classifier, String version )
    {
        this( groupId, artifactId, extension, classifier, version, null );
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String classifier, String version,
                            Path path )
    {
        if ( groupId == null || groupId.isEmpty() )
        {
            throw new IllegalArgumentException( "groupId must be specified" );
        }
        if ( artifactId == null || artifactId.isEmpty() )
        {
            throw new IllegalArgumentException( "artifactId must be specified" );
        }

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension == null || extension.isEmpty() ? DEFAULT_EXTENSION : extension;
        this.classifier = classifier == null ? "" : classifier;
        this.version = version == null || version.isEmpty() ? DEFAULT_VERSION : version;
        this.path = path;
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
    public Path getPath()
    {
        return path;
    }

    @Override
    public Artifact setVersion( String version )
    {
        return new DefaultArtifact( groupId, artifactId, extension, classifier, version, path );
    }

    @Override
    public Artifact setPath( Path path )
    {
        return new DefaultArtifact( groupId, artifactId, extension, classifier, version, path );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( groupId );
        sb.append( ':' ).append( artifactId );
        sb.append( ':' ).append( extension );
        if ( !classifier.isEmpty() )
        {
            sb.append( ':' ).append( classifier );
        }
        sb.append( ':' ).append( getVersion() );
        return sb.toString();
    }

    @Override
    public boolean equals( Object rhs )
    {
        if ( !( rhs instanceof Artifact ) )
        {
            return false;
        }

        Artifact x = (Artifact) rhs;

        return groupId.equals( x.getGroupId() ) && artifactId.equals( x.getArtifactId() )
            && extension.equals( x.getExtension() ) && classifier.equals( x.getClassifier() )
            && version.equals( x.getVersion() ) && ( path == null ? x.getPath() == null : path.equals( x.getPath() ) );
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
}
