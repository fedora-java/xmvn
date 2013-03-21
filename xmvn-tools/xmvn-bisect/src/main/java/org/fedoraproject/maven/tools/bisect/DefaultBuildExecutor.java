/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.tools.bisect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = BuildExecutor.class )
public class DefaultBuildExecutor
    implements BuildExecutor, InvocationOutputHandler
{
    @Requirement
    private Invoker invoker;

    private PrintWriter log;

    @Override
    public boolean executeBuild( InvocationRequest request, String logPath, boolean verbose )
        throws MavenInvocationException
    {
        try
        {
            log = new PrintWriter( logPath );

            request.setOutputHandler( this );
            request.setErrorHandler( this );

            File mavenHome = new File( request.getProperties().get( "maven.home" ).toString() );
            invoker.setMavenHome( mavenHome );
            InvocationResult result = invoker.execute( request );

            return result.getExitCode() == 0;
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            if ( log != null )
                log.close();
        }
    }

    @Override
    public void consumeLine( String line )
    {
        log.println( line );
        // System.out.println( line );
    }
}
