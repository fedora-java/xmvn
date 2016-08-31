/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.sisu.bean.BeanProperties;
import org.eclipse.sisu.bean.BeanProperty;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Listens to various MOJO executions and captures useful information.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnMojoExecutionListener
    implements MojoExecutionListener, ResolutionListener
{
    private static class MojoGoal
    {
        private final String groupId;

        private final String artifactId;

        private final String goal;

        MojoGoal( String groupId, String artifactId, String goal )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.goal = goal;
        }

        boolean equals( MojoExecution execution )
        {
            return execution.getGroupId().equals( groupId ) && execution.getArtifactId().equals( artifactId )
                && execution.getGoal().equals( goal );
        }
    }

    private static final MojoGoal JAVADOC_AGGREGATE = new MojoGoal( "org.apache.maven.plugins", //
                                                                    "maven-javadoc-plugin", //
                                                                    "aggregate" );

    private static final MojoGoal MAVEN_COMPILE = new MojoGoal( "org.apache.maven.plugins", //
                                                                "maven-compiler-plugin", //
                                                                "compile" );

    private static final MojoGoal TYCHO_COMPILE = new MojoGoal( "org.eclipse.tycho", //
                                                                "tycho-compiler-plugin", //
                                                                "compile" );

    private static final MojoGoal XMVN_BUILDDEP = new MojoGoal( "org.fedoraproject.xmvn", //
                                                                "xmvn-mojo", //
                                                                "builddep" );

    private static final MojoGoal XMVN_JAVADOC = new MojoGoal( "org.fedoraproject.xmvn", //
                                                               "xmvn-mojo", //
                                                               "javadoc" );

    private static final Path XMVN_STATE_DIR = Paths.get( ".xmvn" );

    private static final Path APIDOCS_SYMLINK = XMVN_STATE_DIR.resolve( "apidocs" );

    private static final Path PROPERTIES_FILE = XMVN_STATE_DIR.resolve( "properties" );

    private final List<String[]> resolutions = new ArrayList<>();

    private static String getBeanProperty( Object bean, String getterName )
        throws MojoExecutionException
    {
        try
        {
            for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
            {
                try
                {
                    Method getter = clazz.getDeclaredMethod( getterName );
                    getter.setAccessible( true );
                    return getter.invoke( bean ).toString();
                }
                catch ( NoSuchMethodException e )
                {
                }
            }

            throw new MojoExecutionException( "Unable to find bean property getter method " + getterName );
        }
        catch ( ReflectiveOperationException e )
        {
            throw new MojoExecutionException( "Failed to get bean property", e );
        }
    }

    private static void createApidocsSymlink( Path javadocDir )
        throws MojoExecutionException
    {
        try
        {
            if ( !Files.exists( XMVN_STATE_DIR ) )
                Files.createDirectory( XMVN_STATE_DIR );

            if ( Files.isSymbolicLink( APIDOCS_SYMLINK ) )
                Files.delete( APIDOCS_SYMLINK );

            Files.createSymbolicLink( APIDOCS_SYMLINK, javadocDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create apidocs symlink", e );
        }
    }

    private void setProjectProperty( MavenProject project, String key, String value )
        throws MojoExecutionException
    {
        Properties properties = new Properties();

        try
        {
            if ( !Files.exists( XMVN_STATE_DIR ) )
                Files.createDirectory( XMVN_STATE_DIR );

            if ( Files.exists( PROPERTIES_FILE ) )
            {
                try (InputStream stream = Files.newInputStream( PROPERTIES_FILE ))
                {
                    properties.load( stream );
                }
            }

            String projectKey = project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion();
            properties.setProperty( projectKey + "/" + key, value );

            try (OutputStream stream = Files.newOutputStream( PROPERTIES_FILE ))
            {
                properties.store( stream, "XMvn project properties" );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to set project property", e );
        }
    }

    @Override
    public void afterMojoExecutionSuccess( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        Mojo mojo = event.getMojo();
        MojoExecution execution = event.getExecution();
        MavenProject project = event.getProject();

        if ( JAVADOC_AGGREGATE.equals( execution ) )
        {
            String javadocDir = getBeanProperty( mojo, "getReportOutputDirectory" );
            createApidocsSymlink( Paths.get( javadocDir ) );
        }
        else if ( XMVN_JAVADOC.equals( execution ) )
        {
            String javadocDir = getBeanProperty( mojo, "getOutputDir" );
            createApidocsSymlink( Paths.get( javadocDir ) );
        }
        else if ( MAVEN_COMPILE.equals( execution ) )
        {
            setProjectProperty( project, "compilerSource", getBeanProperty( mojo, "getSource" ) );
            setProjectProperty( project, "compilerTarget", getBeanProperty( mojo, "getTarget" ) );
        }
        else if ( TYCHO_COMPILE.equals( execution ) )
        {
            setProjectProperty( project, "compilerSource", getBeanProperty( mojo, "getSourceLevel" ) );
            setProjectProperty( project, "compilerTarget", getBeanProperty( mojo, "getTargetLevel" ) );
        }
    }

    @Override
    public void beforeMojoExecution( MojoExecutionEvent event )
    {
        Mojo mojo = event.getMojo();
        MojoExecution execution = event.getExecution();

        // Disable doclint
        if ( JAVADOC_AGGREGATE.equals( execution ) )
        {
            for ( BeanProperty<Object> property : new BeanProperties( mojo.getClass() ) )
            {
                if ( property.getName().equals( "additionalparam" ) )
                    property.set( mojo, "-Xdoclint:none" );
            }
        }
        else if ( XMVN_BUILDDEP.equals( execution ) )
        {
            for ( BeanProperty<Object> property : new BeanProperties( mojo.getClass() ) )
            {
                if ( property.getName().equals( "resolutions" ) )
                    property.set( mojo, Collections.unmodifiableList( new ArrayList<>( resolutions ) ) );
            }
        }
    }

    @Override
    public void afterExecutionFailure( MojoExecutionEvent event )
    {
        // Nothing to do
    }

    @Override
    public void resolutionRequested( ResolutionRequest request )
    {
        // Nothing to do
    }

    @Override
    public void resolutionCompleted( ResolutionRequest request, ResolutionResult result )
    {
        if ( result.getArtifactPath() != null )
        {
            String[] tuple =
                new String[] { request.getArtifact().toString(), result.getCompatVersion(), result.getNamespace() };
            resolutions.add( tuple );
        }
    }
}
