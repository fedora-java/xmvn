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
package org.fedoraproject.xmvn.connector.aether;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates dependency version report after Maven session ends.
 * 
 * @author Mikolaj Izdebski
 */
class DependencyVersionReportGenerator
    extends AbstractExecutionListener
    implements ResolutionListener
{
    private final Logger logger = LoggerFactory.getLogger( DependencyVersionReportGenerator.class );

    private final Map<Artifact, ResolutionResult> data = new LinkedHashMap<>();

    @Override
    public void resolutionRequested( ResolutionRequest request )
    {
        if ( logger.isDebugEnabled() )
            request.setProviderNeeded( true );
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
        for ( Entry<Artifact, ResolutionResult> entry : data.entrySet() )
        {
            Artifact artifact = entry.getKey();
            ResolutionResult result = entry.getValue();

            String provider = result.getProvider();
            if ( provider == null )
                provider = "(none)";
            Set<String> requestedVersions = shortReport.get( provider );
            if ( requestedVersions == null )
            {
                requestedVersions = new TreeSet<>();
                shortReport.put( provider, requestedVersions );
            }
            requestedVersions.add( artifact.getVersion() );

            logger.debug( "  {} => {}, provided by {}", artifact, result.getCompatVersion(), provider );
        }

        logger.debug( "Simplified XMvn dependency report:" );
        logger.debug( "<pkg-name> (<rpm-version>): <requested-versions>" );
        StringBuilder sb = new StringBuilder();
        for ( Entry<String, Set<String>> entry : shortReport.entrySet() )
        {
            sb.setLength( 0 );
            sb.append( "  " );
            sb.append( entry.getKey() );
            sb.append( ": " );

            Iterator<String> it = entry.getValue().iterator();
            sb.append( it.next() );
            while ( it.hasNext() )
            {
                sb.append( ", " );
                sb.append( it.next() );
            }

            logger.debug( "{}", sb );
        }
    }
}
