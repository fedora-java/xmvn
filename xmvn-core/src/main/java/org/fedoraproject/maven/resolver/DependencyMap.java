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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
class DependencyMap
{
    private static final String NO_NAMESPACE = "<no-namespace>";

    private final Logger logger;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, Map<Artifact, Set<Artifact>>> mapping = new TreeMap<>();

    private final Map<String, Map<Artifact, Set<Artifact>>> reverseMapping = new TreeMap<>();

    public DependencyMap( Logger logger )
    {
        this.logger = logger;
    }

    public boolean isEmpty()
    {
        try
        {
            lock.readLock().lock();
            return mapping.isEmpty();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Deprecated
    public void addMapping( String groupId, String artifactId, String version, String jppGroupId, String jppArtifactId )
    {
        addMapping( null, groupId, artifactId, version, jppGroupId, jppArtifactId );
    }

    public void addMapping( String namespace, String groupId, String artifactId, String version, String jppGroupId,
                            String jppArtifactId )
    {
        Artifact mavenArtifact = new Artifact( groupId, artifactId, version );
        Artifact jppArtifact = new Artifact( jppGroupId, jppArtifactId, version );

        addMapping( namespace, mavenArtifact, jppArtifact );
    }

    private static void addMapping( String namespace, Map<String, Map<Artifact, Set<Artifact>>> map, Artifact from,
                                    Artifact to )
    {
        if ( namespace == null )
            namespace = NO_NAMESPACE;

        Map<Artifact, Set<Artifact>> map1 = map.get( namespace );
        if ( map1 == null )
        {
            map1 = new TreeMap<>();
            map.put( namespace, map1 );
        }

        Set<Artifact> set = map1.get( from );
        if ( set == null )
        {
            set = new TreeSet<>();
            map1.put( from, set );
        }

        set.add( to );
    }

    @Deprecated
    public void addMapping( Artifact from, Artifact to )
    {
        addMapping( null, from, to );
    }

    public void addMapping( String namespace, Artifact from, Artifact to )
    {
        try
        {
            lock.writeLock().lock();
            addMapping( namespace, mapping, from, to );
            addMapping( namespace, reverseMapping, to, from );
        }
        finally
        {
            lock.writeLock().unlock();
        }

        logger.debug( "Added mapping " + from + " => " + to );
    }

    /**
     * Search given mapping recursively in depth-first order.
     * 
     * @param map graph to search
     * @param visited set of visited nodes on the path from the root
     * @param resolved list of visited nodes
     * @param parent starting point
     */
    private void walk( Map<Artifact, Set<Artifact>> map, Set<Artifact> visited, List<Artifact> resolved, Artifact parent )
    {
        visited.add( parent );

        if ( map.containsKey( parent ) )
        {
            for ( Artifact child : map.get( parent ) )
            {
                if ( visited.contains( child ) )
                    continue;

                logger.debug( "Artifact " + parent + " was mapped to " + child );
                walk( map, visited, resolved, child );
            }
        }

        resolved.add( parent );
        visited.remove( parent );
    }

    /**
     * Walk given mapping in depth-first order.
     * 
     * @param map graph to search
     * @param current starting point of the search
     * @return list of all nodes in depth-first order
     */
    private List<Artifact> depthFirstWalk( String namespace, Map<String, Map<Artifact, Set<Artifact>>> map,
                                           Artifact current )
    {
        if ( namespace == null )
            namespace = NO_NAMESPACE;

        Map<Artifact, Set<Artifact>> map1 = map.get( namespace );
        if ( map1 == null )
            return Collections.singletonList( current );

        Set<Artifact> visited = new TreeSet<>();
        List<Artifact> result = new LinkedList<>();

        walk( map1, visited, result, current );

        return result;
    }

    /**
     * Compute a list of artifacts reachable from given start artifact in a reflective transitive closure of dependency
     * graph. The list is in depth-first order. The list is never empty because it always contains the given artifact.
     * 
     * @param artifact start point of depth-first search
     * @return list of artifacts to which given artifact can be mapped
     */
    @Deprecated
    public List<Artifact> translate( Artifact artifact )
    {
        return translate( null, artifact );
    }

    /**
     * Compute a list of artifacts reachable from given start artifact in a reflective transitive closure of dependency
     * graph. The list is in depth-first order. The list is never empty because it always contains the given artifact.
     * Only mappings with specified namespace are considered.
     * 
     * @param namespace namespace to consider
     * @param artifact start point of depth-first search
     * @return list of artifacts to which given artifact can be mapped
     */
    public List<Artifact> translate( String namespace, Artifact artifact )
    {
        logger.debug( "Trying to translate artifact " + artifact );

        try
        {
            lock.readLock().lock();
            List<Artifact> resolved = depthFirstWalk( namespace, mapping, artifact );
            logger.debug( "Translation result is " + Artifact.collectionToString( resolved ) );
            return resolved;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Find all artifacts somehow related to given artifact.
     * 
     * @param artifact artifact relatives of which are to be found
     * @return related set containing all artifacts reachable from given artifact and artifacts from which given
     *         artifact is reachable
     */
    @Deprecated
    public Set<Artifact> relativesOf( Artifact artifact )
    {
        return relativesOf( null, artifact );
    }

    /**
     * Find all artifacts somehow related to given artifact. Only mappings with specified namespace are considered.
     * 
     * @param namespace namespace to consider
     * @param artifact artifact relatives of which are to be found
     * @return related set containing all artifacts reachable from given artifact and artifacts from which given
     *         artifact is reachable
     */
    public Set<Artifact> relativesOf( String namespace, Artifact artifact )
    {
        try
        {
            lock.readLock().lock();
            Set<Artifact> resultSet = new TreeSet<>();
            for ( Artifact aa : depthFirstWalk( namespace, reverseMapping, artifact ) )
                resultSet.addAll( depthFirstWalk( namespace, mapping, aa ) );
            return resultSet;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
