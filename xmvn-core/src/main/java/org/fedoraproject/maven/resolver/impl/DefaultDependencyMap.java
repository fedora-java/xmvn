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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
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

    private final Map<Artifact, Set<Artifact>> mapping = new LinkedHashMap<>();

    private final Map<Artifact, Set<Artifact>> reverseMapping = new LinkedHashMap<>();

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
    @Deprecated
    public void addMapping( String groupId, String artifactId, String version, String jppGroupId, String jppArtifactId )
    {
        Artifact mavenArtifact =
            new DefaultArtifact( groupId, artifactId, ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION );
        Artifact jppArtifact =
            new DefaultArtifact( jppGroupId, jppArtifactId, ArtifactUtils.DEFAULT_EXTENSION,
                                 ArtifactUtils.DEFAULT_VERSION );

        addMapping( mavenArtifact, jppArtifact );
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
            new DefaultArtifact( from.getGroupId(), from.getArtifactId(), from.getClassifier(), from.getExtension(),
                                 ArtifactUtils.DEFAULT_VERSION );
        to =
            new DefaultArtifact( to.getGroupId(), to.getArtifactId(), to.getClassifier(), to.getExtension(),
                                 ArtifactUtils.DEFAULT_VERSION );

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
            new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                                 artifact.getExtension(), ArtifactUtils.DEFAULT_VERSION );
        logger.debug( "Trying to translate artifact " + artifact );

        try
        {
            lock.readLock().lock();
            List<Artifact> resolved = depthFirstWalk( mapping, artifact );
            logger.debug( "Translation result is " + ArtifactUtils.collectionToString( resolved ) );
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
            new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                                 artifact.getExtension(), ArtifactUtils.DEFAULT_VERSION );

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
