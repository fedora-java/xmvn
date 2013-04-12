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
package org.fedoraproject.maven.repository;

import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public enum Layout
{
    /**
     * Maven repository layout, as used by upstream Maven.
     * <p>
     * Example: {@code g/r/o/u/p/artifact/ver/artifact-ver.ext}
     */
    MAVEN( "Maven", false, true, true, false ),

    /**
     * Version-aware repository JPP layout.
     * <p>
     * Example: {@code g/r/o/u/p/artifact-ver.ext}
     */
    JPP( "version-aware JPP", false, false, true, true ),

    /**
     * Version-unaware JPP repository layout.
     * <p>
     * Example: {@code g/r/o/u/p/artifact.ext}
     */
    JPP_VERSIONLESS( "versionless JPP", false, false, false, true ),

    /**
     * Version-aware flat repository layout.
     * <p>
     * Example: {@code g.r.o.u.p-artifact-ver.ext}
     */
    FLAT( "version-aware flat", true, false, true, false ),

    /**
     * Version-unaware flat repository layout.
     * <p>
     * Example: {@code g.r.o.u.p-artifact.ext}
     */
    FLAT_VERSIONLESS( "versionless flat", true, false, false, false );

    private final String name;

    private final boolean flat;

    private final boolean deep;

    private final boolean versioned;

    private final boolean skipJpp;

    private Layout( String name, boolean flat, boolean deep, boolean versioned, boolean skipJpp )
    {
        this.name = name;
        this.flat = flat;
        this.deep = deep;
        this.versioned = versioned;
        this.skipJpp = skipJpp;
    }

    public boolean isVersioned()
    {
        return versioned;
    }

    public String getArtifactPath( Artifact artifact )
    {
        StringBuilder path = new StringBuilder();

        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        String extension = artifact.getExtension();

        if ( skipJpp )
        {
            if ( groupId.startsWith( "JPP/" ) )
                groupId = groupId.substring( 4 );
            else if ( groupId.equals( "JPP" ) )
                groupId = null;
        }

        if ( groupId != null )
        {
            if ( flat )
            {
                path.append( groupId.replace( '/', '.' ) );
                path.append( '-' );
            }
            else
            {
                path.append( groupId.replace( '.', '/' ) );
                path.append( '/' );
            }
        }

        path.append( artifactId );

        if ( deep )
        {
            path.append( '/' );
            path.append( version );
            path.append( '/' );
            path.append( artifactId );
        }

        if ( versioned )
        {
            path.append( '-' );
            path.append( version );
        }

        path.append( '.' );
        path.append( extension );

        return path.toString();
    }

    @Override
    public String toString()
    {
        return name + " layout";
    }
}
