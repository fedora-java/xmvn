/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.p2.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IInstallableUnitFragment;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
public class Resolver
{
    private static final Logger logger = LoggerFactory.getLogger( Resolver.class );

    private static Set<IInstallableUnit> executeQuery( Repository repository, IQuery<IInstallableUnit> query )
    {
        IQueryable<IInstallableUnit> queryable = repository.getMetadataRepository();
        IQueryResult<IInstallableUnit> result = queryable.query( query, null );
        return result.toUnmodifiableSet();
    }

    public static Set<IInstallableUnit> resolveAll( Repository repository )
    {
        IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
        return executeQuery( repository, query );
    }

    public static IInstallableUnit resolveOne( Repository repository, String id, String version )
    {
        VersionRange versionRange = version != null ? new VersionRange( version ) : null;
        IQuery<IInstallableUnit> query = QueryUtil.createIUQuery( id, versionRange );
        Set<IInstallableUnit> result = executeQuery( repository, query );
        if ( result.isEmpty() )
            return null;
        if ( result.size() > 1 )
            throw new RuntimeException( "More than one IU found for " + id + ( version != null ? "/" + version : "" ) );
        return result.iterator().next();
    }

    private static Collection<IRequirement> getRequirements( IInstallableUnit iu )
    {
        List<IRequirement> requirements = new ArrayList<IRequirement>( iu.getRequirements() );
        requirements.addAll( iu.getMetaRequirements() );

        if ( iu instanceof IInstallableUnitFragment )
        {
            IInstallableUnitFragment fragment = (IInstallableUnitFragment) iu;
            requirements.addAll( fragment.getHost() );
        }

        for ( Iterator<IRequirement> iterator = requirements.iterator(); iterator.hasNext(); )
        {
            IRequirement req = iterator.next();
            if ( req.getMax() == 0 )
                iterator.remove();
        }

        return requirements;
    }

    public static void resolveDependencies( Set<IInstallableUnit> result, Set<IInstallableUnit> symlink,
                                            IQueryable<IInstallableUnit> queryable, Set<IInstallableUnit> reactor,
                                            Set<IInstallableUnit> platform, Set<IInstallableUnit> internal,
                                            Set<IInstallableUnit> external )
    {
        LinkedList<IInstallableUnit> toProcess = new LinkedList<IInstallableUnit>( result );
        while ( !toProcess.isEmpty() )
        {
            IInstallableUnit iu = toProcess.removeFirst().unresolved();
            logger.debug( "##### IU {}", iu );

            for ( IRequirement req : getRequirements( iu ) )
            {
                logger.debug( "    Requires: {}", req );

                IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery( req.getMatches() );
                Set<IInstallableUnit> matches = queryable.query( query, null ).toUnmodifiableSet();
                if ( matches.isEmpty() )
                {
                    if ( req.getMin() == 0 )
                        logger.info( "Unable to satisfy optional dependency from {} to {}", iu, req );
                    else
                        logger.warn( "Unable to satisfy dependency from {} to {}", iu, req );
                    continue;
                }

                Set<IInstallableUnit> resolved = new LinkedHashSet<>( matches );
                resolved.retainAll( reactor );
                if ( !resolved.isEmpty() )
                {
                    for ( IInstallableUnit match : resolved )
                    {
                        logger.debug( "      => {} (built unit)", match );
                        if ( result.add( match ) )
                            toProcess.addLast( match );
                    }

                    continue;
                }

                resolved.addAll( matches );
                resolved.retainAll( platform );
                if ( !resolved.isEmpty() )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        for ( IInstallableUnit match : resolved )
                            logger.debug( "      => {} (part of platform)", match );
                    }

                    continue;
                }

                resolved.addAll( matches );
                resolved.retainAll( internal );
                if ( !resolved.isEmpty() )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        for ( IInstallableUnit match : resolved )
                            logger.debug( "      => {} (dropins)", match );
                    }

                    continue;
                }

                if ( matches.size() > 1 )
                    logger.warn( "More than one external bundle satisfies dependency from {} to {}", iu, req );

                if ( !external.containsAll( matches ) )
                    throw new RuntimeException( "Requirement was resolved from unknown repository" );

                for ( IInstallableUnit match : matches )
                {
                    logger.debug( "      => {} (external, will be symlinked)", match );
                    if ( symlink.add( match ) )
                        toProcess.addLast( match );
                }
            }
        }
    }
}
