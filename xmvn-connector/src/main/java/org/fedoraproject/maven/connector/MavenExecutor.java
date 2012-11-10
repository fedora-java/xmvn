/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.Configuration;

public class MavenExecutor
{
    public void execute( String... goals )
        throws Throwable
    {
        DefaultPlexusContainer container = null;

        try
        {
            container = new DefaultPlexusContainer();
            int loggerThreshold = Configuration.isMavenDebug() ? Logger.LEVEL_DEBUG : Logger.LEVEL_WARN;
            container.getLoggerManager().setThreshold( loggerThreshold );
            Maven maven = container.lookup( Maven.class );

            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setGoals( Arrays.asList( goals ) );
            request.setLocalRepositoryPath( Configuration.getLocalRepoPath() );
            request.setInteractiveMode( false );
            request.setOffline( Configuration.isMavenOnline() == false );
            request.setPom( new File( "pom.xml" ) );
            request.setSystemProperties( System.getProperties() );

            MavenExecutionResult result = maven.execute( request );
            if ( result.hasExceptions() )
                throw result.getExceptions().iterator().next();
        }
        finally
        {
            if ( container != null )
                container.dispose();
        }
    }
}
