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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.ArtifactImpl;
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
    implements ArtifactBlacklist, Initializable
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private DependencyMap depmap;

    private final Set<ArtifactImpl> blacklist = new TreeSet<>();

    @Override
    public boolean contains( String groupId, String artifactId )
    {
        return contains( new ArtifactImpl( groupId, artifactId ) );
    }

    @Override
    public void initialize()
    {
        createInitialBlacklist();
        blacklistAliases();
    }

    @Override
    public synchronized boolean contains( ArtifactImpl artifact )
    {
        return blacklist.contains( artifact.clearVersionAndExtension() );
    }

    @Override
    public void add( String groupId, String artifactId )
    {
        add( new ArtifactImpl( groupId, artifactId ) );
    }

    @Override
    public synchronized void add( ArtifactImpl artifact )
    {
        blacklist.add( artifact.clearVersionAndExtension() );
    }

    /**
     * Enumerate all blacklisted artifacts.
     * 
     * @return set view of artifact blacklist
     */
    public Set<ArtifactImpl> setView()
    {
        return Collections.unmodifiableSet( blacklist );
    }

    /**
     * Construct the initial artifact blacklist.
     */
    private void createInitialBlacklist()
    {
        add( ArtifactImpl.DUMMY );
        add( ArtifactImpl.DUMMY_JPP );

        for ( org.fedoraproject.maven.config.Artifact artifact : configurator.getConfiguration().getResolverSettings().getBlacklist() )
            add( artifact.getGroupId(), artifact.getArtifactId() );

        logger.debug( "Initial artifact blacklist is: " + ArtifactImpl.collectionToString( blacklist, true ) );
    }

    /**
     * Blacklist all aliases of already blacklisted artifacts.
     */
    private void blacklistAliases()
    {
        Set<ArtifactImpl> aliasBlacklist = new TreeSet<>();
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        for ( ArtifactImpl artifact : blacklist )
        {
            Set<ArtifactImpl> relatives = depmap.relativesOf( artifact );
            aliasBlacklist.addAll( relatives );
            logger.debug( "Blacklisted relatives of " + artifact + ": " + ArtifactImpl.collectionToString( relatives ) );
        }

        blacklist.addAll( aliasBlacklist );
        logger.debug( "Final artifact blacklist is: " + ArtifactImpl.collectionToString( blacklist, true ) );
    }
}
