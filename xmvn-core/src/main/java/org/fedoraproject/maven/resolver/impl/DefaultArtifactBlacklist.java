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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.resolver.ArtifactBlacklist;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * Default implementation of {@code ArtifactBlacklist} container.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ArtifactBlacklist.class )
public class DefaultArtifactBlacklist
    implements ArtifactBlacklist
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private DependencyMap depmap;

    private boolean initialized;

    private final Set<Artifact> blacklist = new TreeSet<>();

    @Override
    public boolean contains( String groupId, String artifactId )
    {
        return contains( new Artifact( groupId, artifactId ) );
    }

    @Override
    public synchronized boolean contains( Artifact artifact )
    {
        if ( !initialized )
        {
            initialized = true;
            createInitialBlacklist();
            blacklistAliases();
        }

        return blacklist.contains( artifact.clearVersionAndExtension() );
    }

    @Override
    public void add( String groupId, String artifactId )
    {
        add( new Artifact( groupId, artifactId ) );
    }

    @Override
    public synchronized void add( Artifact artifact )
    {
        if ( !initialized )
        {
            initialized = true;
            createInitialBlacklist();
            blacklistAliases();
        }

        blacklist.add( artifact.clearVersionAndExtension() );
    }

    /**
     * Enumerate all blacklisted artifacts.
     * 
     * @return set view of artifact blacklist
     */
    public Set<Artifact> setView()
    {
        return Collections.unmodifiableSet( blacklist );
    }

    /**
     * Construct the initial artifact blacklist.
     */
    private void createInitialBlacklist()
    {
        add( Artifact.DUMMY );
        add( Artifact.DUMMY_JPP );

        for ( org.fedoraproject.maven.config.Artifact artifact : configurator.getConfiguration().getResolverSettings().getBlacklist() )
            add( artifact.getGroupId(), artifact.getArtifactId() );

        logger.debug( "Initial artifact blacklist is: " + Artifact.collectionToString( blacklist, true ) );
    }

    /**
     * Blacklist all aliases of already blacklisted artifacts.
     */
    private void blacklistAliases()
    {
        Set<Artifact> aliasBlacklist = new TreeSet<>();
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        for ( Artifact artifact : blacklist )
        {
            Set<Artifact> relatives = depmap.relativesOf( artifact );
            aliasBlacklist.addAll( relatives );
            logger.debug( "Blacklisted relatives of " + artifact + ": " + Artifact.collectionToString( relatives ) );
        }

        blacklist.addAll( aliasBlacklist );
        logger.debug( "Final artifact blacklist is: " + Artifact.collectionToString( blacklist, true ) );
    }
}
