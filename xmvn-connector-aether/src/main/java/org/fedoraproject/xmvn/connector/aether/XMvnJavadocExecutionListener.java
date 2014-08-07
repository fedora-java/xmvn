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

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Listens to executions of "aggregate" goal of Maven Javadoc Plugin and captures Javadoc report output location.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnJavadocExecutionListener
    implements MojoExecutionListener
{
    private static final String JAVADOC_PLUGIN_GROUPID = "org.apache.maven.plugins";

    private static final String JAVADOC_PLUGIN_ARTIFACTID = "maven-javadoc-plugin";

    private static final String JAVADOC_PLUGIN_GOAL = "aggregate";

    private static final Path XMVN_STATE_DIR = Paths.get( ".xmvn" );

    private static final Path APIDOCS_SYMLINK = XMVN_STATE_DIR.resolve( "apidocs" );

    @Override
    public void afterMojoExecutionSuccess( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        try
        {
            Mojo mojo = event.getMojo();
            MojoExecution execution = event.getExecution();

            if ( execution.getGroupId().equals( JAVADOC_PLUGIN_GROUPID )
                && execution.getArtifactId().equals( JAVADOC_PLUGIN_ARTIFACTID )
                && execution.getGoal().equals( JAVADOC_PLUGIN_GOAL ) )
            {
                Method method = mojo.getClass().getMethod( "getReportOutputDirectory" );
                Path javadocDir = Paths.get( method.invoke( mojo ).toString() );

                if ( !Files.exists( XMVN_STATE_DIR ) )
                    Files.createDirectory( XMVN_STATE_DIR );

                if ( Files.isSymbolicLink( APIDOCS_SYMLINK ) )
                    Files.delete( APIDOCS_SYMLINK );

                Files.createSymbolicLink( APIDOCS_SYMLINK, javadocDir );
            }
        }
        catch ( ReflectiveOperationException | IOException e )
        {
            throw new MojoExecutionException( "Failed to capture Javadoc report output location", e );
        }
    }

    @Override
    public void beforeMojoExecution( MojoExecutionEvent event )
    {
        // Nothing to do
    }

    @Override
    public void afterExecutionFailure( MojoExecutionEvent event )
    {
        // Nothing to do
    }
}
