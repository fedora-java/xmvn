/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
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
import java.nio.file.Paths;

import org.w3c.dom.Element;

/**
 * Maven repository layout, as used by upstream Maven.
 * <p>
 * Example: {@code g/r/o/u/p/artifact/ver/artifact-ver.ext}
 * 
 * @author Mikolaj Izdebski
 */
class MavenRepository
    extends SimpleRepository
{
    public MavenRepository( String namespace, Path root, Element filter )
    {
        super( namespace, root, filter );
    }

    @Override
    protected Path getArtifactPath( String pattern, String groupId, String artifactId, String extension,
                                    String classifier, String version )
    {
        if ( version == null )
            return null;

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
}
