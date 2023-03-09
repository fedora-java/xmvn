/*-
 * Copyright (c) 2014-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.codehaus.plexus.logging.Logger;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Generates dependency version report after Maven session ends.
 * 
 * @author Mikolaj Izdebski
 */
class DependencyVersionReportGenerator
    extends AbstractExecutionListener
    implements ResolutionListener
{
    private final Logger logger;

    private final Map<Artifact, ResolutionResult> data = new LinkedHashMap<>();

    public DependencyVersionReportGenerator( Logger logger )
    {
        this.logger = logger;
    }

    @Override
    public void resolutionRequested( ResolutionRequest request )
    {
        if ( logger.isDebugEnabled() )
        {
            request.setProviderNeeded( true );
        }
    }

    @Override
    public void resolutionCompleted( ResolutionRequest request, ResolutionResult result )
    {
        Artifact artifact = request.getArtifact();
        data.put( artifact, result );
    }

    @Override
    public void sessionEnded( ExecutionEvent event )
    {
        Map<String, Set<String>> shortReport = new TreeMap<>();

        logger.debug( "Full XMvn dependency report:" );
        logger.debug( "<gId>:<aId>:<ext>[:<classifier>:]<version> => <compat-version>, provided by <pkg-name> (<rpm-version>)" );
        data.forEach( ( artifact, result ) ->
        {
            String provider = result.getProvider();
            if ( provider == null )
            {
                provider = "(none)";
            }
            Set<String> requestedVersions = shortReport.get( provider );
            if ( requestedVersions == null )
            {
                requestedVersions = new TreeSet<>();
                shortReport.put( provider, requestedVersions );
            }
            requestedVersions.add( artifact.getVersion() );

            logger.debug( "  " + artifact + " => " + result.getCompatVersion() + ", provided by " + provider );
        } );

        logger.debug( "Simplified XMvn dependency report:" );
        logger.debug( "<pkg-name> (<rpm-version>): <requested-versions>" );
        shortReport.forEach( ( provider, versions ) ->
        {
            logger.debug( "  " + provider + ": " + versions.stream().collect( Collectors.joining( ", " ) ) );
        } );
    }
}
