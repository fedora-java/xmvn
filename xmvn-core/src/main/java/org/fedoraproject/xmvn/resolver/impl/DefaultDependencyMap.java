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
package org.fedoraproject.xmvn.resolver.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.resolver.DependencyMap;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
@Deprecated
public class DefaultDependencyMap
    implements DependencyMap
{
    private final Logger logger = LoggerFactory.getLogger( DefaultDependencyMap.class );

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<Artifact, Set<Artifact>> mapping = new LinkedHashMap<>();

    private final Map<Artifact, Set<Artifact>> reverseMapping = new LinkedHashMap<>();

    @Inject
    public DefaultDependencyMap( Configurator configurator )
    {
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();

        List<Path> metadataDirs = new ArrayList<>();
        for ( String prefix : settings.getPrefixes() )
        {
            Path root = Paths.get( prefix );
            if ( Files.isDirectory( root ) )
            {
                for ( String dir : settings.getMetadataRepositories() )
                    metadataDirs.add( root.resolve( dir ) );
            }
        }

        DepmapReader reader = new DepmapReader();
        reader.readMappings( this, metadataDirs );
    }

    @Override
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

    private static void addMapping( Map<Artifact, Set<Artifact>> map, Artifact from, Artifact to )
    {
        Set<Artifact> set = map.get( from );
        if ( set == null )
        {
            set = new LinkedHashSet<>();
            map.put( from, set );
        }

        set.add( to );
    }

    @Override
    public void addMapping( Artifact from, Artifact to )
    {
        from =
            new DefaultArtifact( from.getGroupId(), from.getArtifactId(), from.getExtension(), from.getClassifier(),
                                 null );
        to = new DefaultArtifact( to.getGroupId(), to.getArtifactId(), to.getExtension(), to.getClassifier(), null );

        try
        {
            lock.writeLock().lock();
            addMapping( mapping, from, to );
            addMapping( reverseMapping, to, from );
        }
        finally
        {
            lock.writeLock().unlock();
        }

        logger.debug( "Added mapping {} => {}", from, to );
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

                logger.debug( "Artifact {} was mapped to {}", parent, child );
                walk( map, visited, resolved, child );
                resolved.add( child );
            }
        }

        visited.remove( parent );
    }

    /**
     * Walk given mapping in depth-first order.
     * 
     * @param map graph to search
     * @param current starting point of the search
     * @return list of all nodes in depth-first order
     */
    private List<Artifact> depthFirstWalk( Map<Artifact, Set<Artifact>> map, Artifact current )
    {
        Set<Artifact> visited = new LinkedHashSet<>();
        List<Artifact> result = new LinkedList<>();

        walk( map, visited, result, current );

        return result;
    }

    @Override
    public List<Artifact> translate( Artifact artifact )
    {
        artifact =
            new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                                 artifact.getClassifier(), null );
        logger.debug( "Trying to translate artifact {}", artifact );

        try
        {
            lock.readLock().lock();
            List<Artifact> resolved = depthFirstWalk( mapping, artifact );
            logger.debug( "Translation result is {}", ArtifactUtils.collectionToString( resolved ) );
            return resolved;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<Artifact> relativesOf( Artifact artifact )
    {
        artifact =
            new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                                 artifact.getClassifier(), null );

        try
        {
            lock.readLock().lock();
            Set<Artifact> resultSet = new LinkedHashSet<>();
            for ( Artifact aa : depthFirstWalk( reverseMapping, artifact ) )
                resultSet.addAll( depthFirstWalk( mapping, aa ) );
            return resultSet;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
