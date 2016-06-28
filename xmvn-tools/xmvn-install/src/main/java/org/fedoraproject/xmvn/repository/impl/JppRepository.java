/*-
 * Copyright (c) 2012-2015 Red Hat, Inc.
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

import org.w3c.dom.Node;

/**
 * JPP-style repository JPP layout, either versioned or versionless, depending on properties.
 * <p>
 * Example: {@code g/r/o/u/p/artifact-ver.ext} or {@code g/r/o/u/p/artifact.ext}
 * 
 * @author Mikolaj Izdebski
 */
class JppRepository
    extends SimpleRepository
{
    public JppRepository( String namespace, Path root, Node filter )
    {
        super( namespace, root, filter );
    }

    @Override
    protected Path getArtifactPath( String pattern, String groupId, String artifactId, String extension,
                                    String classifier, String version )
    {
        StringBuilder path = new StringBuilder();

        path.append( pattern.replaceFirst( "^JPP/", "" ) );

        if ( version != null )
            path.append( '-' ).append( version );

        if ( !classifier.isEmpty() )
            path.append( '-' ).append( classifier );

        if ( !extension.isEmpty() )
            path.append( '.' ).append( extension );

        return Paths.get( path.toString() );
    }
}
