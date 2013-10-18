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
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.resolver.ArtifactBlacklist;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * Default implementation of {@code ArtifactBlacklist} container.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
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

    private final Set<Artifact> blacklist = new LinkedHashSet<>();

    @Override
    public boolean contains( String groupId, String artifactId )
    {
        return contains( new DefaultArtifact( groupId, artifactId, ArtifactUtils.DEFAULT_EXTENSION,
                                              ArtifactUtils.DEFAULT_VERSION ) );
    }

    @Override
    public void initialize()
    {
        createInitialBlacklist();
        blacklistAliases();
    }

    @Override
    public synchronized boolean contains( Artifact artifact )
    {
        return blacklist.contains( new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                                        ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION ) );
    }

    @Override
    public void add( String groupId, String artifactId )
    {
        add( new DefaultArtifact( groupId, artifactId, ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION ) );
    }

    @Override
    public synchronized void add( Artifact artifact )
    {
        blacklist.add( new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                            ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION ) );
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
        add( ArtifactUtils.DUMMY );
        add( ArtifactUtils.DUMMY_JPP );

        for ( org.fedoraproject.maven.config.Artifact artifact : configurator.getConfiguration().getResolverSettings().getBlacklist() )
            add( artifact.getGroupId(), artifact.getArtifactId() );

        logger.debug( "Initial artifact blacklist is: " + ArtifactUtils.collectionToString( blacklist, true ) );
    }

    /**
     * Blacklist all aliases of already blacklisted artifacts.
     */
    private void blacklistAliases()
    {
        Set<Artifact> aliasBlacklist = new LinkedHashSet<>();
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        for ( Artifact artifact : blacklist )
        {
            Set<Artifact> relatives = depmap.relativesOf( artifact );
            aliasBlacklist.addAll( relatives );
            logger.debug( "Blacklisted relatives of " + artifact + ": " + ArtifactUtils.collectionToString( relatives ) );
        }

        blacklist.addAll( aliasBlacklist );
        logger.debug( "Final artifact blacklist is: " + ArtifactUtils.collectionToString( blacklist, true ) );
    }
}
