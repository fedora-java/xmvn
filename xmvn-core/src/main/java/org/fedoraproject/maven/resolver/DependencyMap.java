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
package org.fedoraproject.maven.resolver;

import static org.fedoraproject.maven.utils.Logger.debug;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.fedoraproject.maven.model.Artifact;

public class DependencyMap
{
    protected final Map<Artifact, Set<Artifact>> mapping = new TreeMap<>();

    public boolean isEmpty()
    {
        return mapping.isEmpty();
    }

    public void addMapping( String groupId, String artifactId, String version, String jppGroupId, String jppArtifactId )
    {
        Artifact mavenArtifact = new Artifact( groupId, artifactId, version );
        Artifact jppArtifact = new Artifact( jppGroupId, jppArtifactId, version );

        addMapping( mavenArtifact, jppArtifact );
    }

    public void addMapping( Artifact from, Artifact to )
    {
        Set<Artifact> set = mapping.get( from );
        if ( set == null )
        {
            set = new TreeSet<>();
            mapping.put( from, set );
        }

        set.add( to );
        debug( "Added mapping ", from, " => ", to );
    }

    private void walk( Set<Artifact> visited, List<Artifact> resolved, Artifact parent )
    {
        visited.add( parent );

        if ( mapping.containsKey( parent ) )
        {
            for ( Artifact child : mapping.get( parent ) )
            {
                if ( visited.contains( child ) )
                    continue;

                debug( "Artifact ", parent, " was mapped to ", child );
                walk( visited, resolved, child );
            }
        }

        resolved.add( parent );
        visited.remove( parent );
    }

    public List<Artifact> translate( Artifact current )
    {
        debug( "Trying to translate artifact ", current );

        Set<Artifact> visited = new TreeSet<>();
        List<Artifact> resolved = new LinkedList<>();
        walk( visited, resolved, current );

        debug( "Translation result is ", Artifact.collectionToString( resolved ) );
        return resolved;
    }
}
