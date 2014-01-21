/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import javax.inject.Named;

import org.fedoraproject.xmvn.repository.Repository;

/**
 * Maven repository layout, as used by upstream Maven.
 * <p>
 * Example: {@code g/r/o/u/p/artifact/ver/artifact-ver.ext}
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "maven" )
public class MavenRepository
    extends SimpleRepository
{
    @Override
    protected Path getArtifactPath( String groupId, String artifactId, String extension, String classifier,
                                    String version )
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

    @Override
    public Repository clone()
    {
        return new MavenRepository();
    }
}
