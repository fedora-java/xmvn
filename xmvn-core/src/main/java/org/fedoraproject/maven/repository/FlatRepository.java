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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.component.annotations.Component;
import org.fedoraproject.maven.model.Artifact;

/**
 * Flat repository layout, either versioned or versionless, depending on properties.
 * <p>
 * Example: {@code g.r.o.u.p-artifact-ver.ext} or {@code g.r.o.u.p-artifact.ext}
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = FlatRepository.ROLE_HINT )
public class FlatRepository
    implements Repository
{
    static final String ROLE_HINT = "flat";

    @Override
    public Path getArtifactPath( Artifact artifact )
    {
        StringBuilder path = new StringBuilder();

        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        String extension = artifact.getExtension();

        if ( groupId != null )
        {
            path.append( groupId.replace( '/', '.' ) );
            path.append( '-' );
        }

        path.append( artifactId );

        if ( !artifact.isVersionless() )
        {
            path.append( '-' );
            path.append( version );
        }

        path.append( '.' );
        path.append( extension );

        return Paths.get( path.toString() );
    }
}
