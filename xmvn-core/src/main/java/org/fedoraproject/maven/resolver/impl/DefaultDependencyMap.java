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
package org.fedoraproject.maven.resolver.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.ArtifactImpl;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = DependencyMap.class )
public class DefaultDependencyMap
    implements DependencyMap, Initializable
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<ArtifactImpl, Set<ArtifactImpl>> mapping = new TreeMap<>();

    private final Map<ArtifactImpl, Set<ArtifactImpl>> reverseMapping = new TreeMap<>();

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

    @Override
    public void addMapping( String groupId, String artifactId, String version, String jppGroupId, String jppArtifactId )
    {
        ArtifactImpl mavenArtifact = new ArtifactImpl( groupId, artifactId, version );
        ArtifactImpl jppArtifact = new ArtifactImpl( jppGroupId, jppArtifactId, version );

        addMapping( mavenArtifact, jppArtifact );
    }

    private static void addMapping( Map<ArtifactImpl, Set<ArtifactImpl>> map, ArtifactImpl from, ArtifactImpl to )
    {
        Set<ArtifactImpl> set = map.get( from );
        if ( set == null )
        {
            set = new TreeSet<>();
            map.put( from, set );
        }

        set.add( to );
    }

    @Override
    public void addMapping( ArtifactImpl from, ArtifactImpl to )
    {
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
    private void walk( Map<ArtifactImpl, Set<ArtifactImpl>> map, Set<ArtifactImpl> visited, List<ArtifactImpl> resolved, ArtifactImpl parent )
    {
        visited.add( parent );

        if ( map.containsKey( parent ) )
        {
            for ( ArtifactImpl child : map.get( parent ) )
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
    private List<ArtifactImpl> depthFirstWalk( Map<ArtifactImpl, Set<ArtifactImpl>> map, ArtifactImpl current )
    {
        Set<ArtifactImpl> visited = new TreeSet<>();
        List<ArtifactImpl> result = new LinkedList<>();

        walk( map, visited, result, current );

        return result;
    }

    @Override
    public List<ArtifactImpl> translate( ArtifactImpl artifact )
    {
        logger.debug( "Trying to translate artifact " + artifact );

        try
        {
            lock.readLock().lock();
            List<ArtifactImpl> resolved = depthFirstWalk( mapping, artifact );
            logger.debug( "Translation result is " + ArtifactImpl.collectionToString( resolved ) );
            return resolved;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<ArtifactImpl> relativesOf( ArtifactImpl artifact )
    {
        try
        {
            lock.readLock().lock();
            Set<ArtifactImpl> resultSet = new TreeSet<>();
            for ( ArtifactImpl aa : depthFirstWalk( reverseMapping, artifact ) )
                resultSet.addAll( depthFirstWalk( mapping, aa ) );
            return resultSet;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public void initialize()
        throws InitializationException
    {
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        List<String> metadataDirs = new ArrayList<>();
        for ( String prefix : settings.getPrefixes() )
        {
            File root = new File( prefix );
            if ( root.isDirectory() )
            {
                for ( String dir : settings.getMetadataRepositories() )
                    metadataDirs.add( new File( root, dir ).toString() );
            }
        }

        DepmapReader reader = new DepmapReader();
        reader.readMappings( this, metadataDirs );
    }
}
