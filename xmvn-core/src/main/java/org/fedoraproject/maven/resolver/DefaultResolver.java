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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.Layout;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.SingletonRepository;
import org.fedoraproject.maven.utils.AtomicFileCounter;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Resolver.class )
public class DefaultResolver
    extends AbstractResolver
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    private AtomicFileCounter bisectCounter;

    private Repository bisectRepo;

    private boolean initialized;

    private final Collection<Resolver> resolvers = new LinkedList<>();

    private void initializeBisect()
    {
        try
        {
            String bisectCounterPath = System.getProperty( "xmvn.bisect.counter" );
            String bisectRepoPath = System.getProperty( "xmvn.bisect.repository" );

            if ( StringUtils.isEmpty( bisectRepoPath ) || StringUtils.isEmpty( bisectCounterPath ) )
            {
                logger.debug( "Bisection build is not enabled" );
                return;
            }

            File bisectRepoRoot = new File( bisectRepoPath );
            if ( !bisectRepoRoot.isDirectory() )
            {
                logger.fatalError( "xmvn.bisect.repository is not a directory" );
                throw new RuntimeException( "xmvn.bisect.repository is not a directory" );
            }

            bisectRepo = new SingletonRepository( bisectRepoRoot, Layout.MAVEN );
            bisectCounter = new AtomicFileCounter( bisectCounterPath );

            logger.info( "Enabled XMvn bisection build" );
        }
        catch ( IOException e )
        {
            logger.fatalError( "Unable to initialize XMvn bisection build", e );
            throw new RuntimeException( e );
        }
    }

    private void initialize()
    {
        initializeBisect();

        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        resolvers.add( new LocalResolver( settings ) );

        for ( String prefix : settings.getPrefixes() )
        {
            File root = new File( prefix );
            if ( root.isDirectory() )
            {
                resolvers.add( new JavaHomeResolver( root, settings, logger ) );
                Resolver resolver = new SystemResolver( root, settings, logger );
                resolvers.add( new CachingResolver( resolver, logger ) );
            }
        }

        initialized = true;
    }

    private boolean resolveFromBisectRepo()
    {
        if ( bisectCounter == null || bisectRepo == null )
            return false;

        try
        {
            return bisectCounter.tryDecrement() > 0;
        }
        catch ( IOException e )
        {
            logger.fatalError( "Failed to decrement bisection counter", e );
            throw new RuntimeException( e );
        }
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        if ( !initialized )
            initialize();

        Artifact artifact = request.getArtifact();

        if ( resolveFromBisectRepo() )
        {
            logger.debug( "Resolving artifact " + artifact + " from bisection repository." );
            File artifactFile = bisectRepo.findArtifact( artifact, true );
            return new DefaultResolutionResult( artifactFile );
        }

        logger.debug( "Trying to resolve artifact " + artifact );

        for ( Resolver resolver : resolvers )
        {
            ResolutionResult result = resolver.resolve( request );
            if ( result != null )
                return result;
        }

        logger.debug( "Unresolved artifact " + artifact );
        return new DefaultResolutionResult();
    }
}
