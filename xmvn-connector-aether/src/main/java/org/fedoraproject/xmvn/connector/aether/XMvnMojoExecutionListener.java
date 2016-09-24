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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Listens to various MOJO executions and captures useful information.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = XMvnMojoExecutionListener.class )
public class XMvnMojoExecutionListener
    implements ResolutionListener, Initializable
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

    @Requirement
    private BuildPluginManager buildPluginManager;

    @Requirement
    private MavenPluginManager mavenPluginManager;

    @Requirement
    private LegacySupport legacySupport;

    private Path xmvnStateDir = Paths.get( ".xmvn" );

    void setXmvnStateDir( Path xmvnStateDir )
    {
        this.xmvnStateDir = xmvnStateDir;
    }

    private Object dispatchBuildPluginManagerMethodCall( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Object ret = method.invoke( mavenPluginManager, args );

        if ( method.getName().equals( "getConfiguredMojo" ) )
        {
            beforeMojoExecution( ret, (MojoExecution) args[2] );
        }
        else if ( method.getName().equals( "releaseMojo" ) )
        {
            afterMojoExecution( args[0], (MojoExecution) args[1], legacySupport.getSession().getCurrentProject() );
        }

        return ret;
    }

    @Override
    public void initialize()
    {
        Object proxy = Proxy.newProxyInstance( XMvnMojoExecutionListener.class.getClassLoader(),
                                               new Class<?>[] { MavenPluginManager.class },
                                               this::dispatchBuildPluginManagerMethodCall );
        trySetBeanProperty( buildPluginManager, "mavenPluginManager", proxy );
    }

    private final List<String[]> resolutions = new ArrayList<>();

    private static String getBeanProperty( Object bean, String getterName )
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

            throw new RuntimeException( "Unable to find bean property getter method " + getterName );
        }
        catch ( ReflectiveOperationException e )
        {
            throw new RuntimeException( "Failed to get bean property", e );
        }
    }

    private static void trySetBeanProperty( Object bean, String fieldName, Object value )
    {
        try
        {
            for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
            {
                try
                {
                    Field field = clazz.getDeclaredField( fieldName );
                    field.setAccessible( true );
                    field.set( bean, value );
                    return;
                }
                catch ( NoSuchFieldException e )
                {
                }
            }
        }
        catch ( ReflectiveOperationException e )
        {
            throw new RuntimeException( "Failed to get bean property", e );
        }
    }

    private void createApidocsSymlink( Path javadocDir )
    {
        try
        {
            Path apidocsSymlink = xmvnStateDir.resolve( "apidocs" );

            if ( !Files.exists( xmvnStateDir ) )
                Files.createDirectory( xmvnStateDir );

            if ( Files.isSymbolicLink( apidocsSymlink ) )
                Files.delete( apidocsSymlink );

            Files.createSymbolicLink( apidocsSymlink, javadocDir );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to create apidocs symlink", e );
        }
    }

    private void setProjectProperty( MavenProject project, String key, String value )
    {
        Properties properties = new Properties();

        try
        {
            Path propertiesFile = xmvnStateDir.resolve( "properties" );

            if ( !Files.exists( xmvnStateDir ) )
                Files.createDirectory( xmvnStateDir );

            if ( Files.exists( propertiesFile ) )
            {
                try ( InputStream stream = Files.newInputStream( propertiesFile ) )
                {
                    properties.load( stream );
                }
            }

            String projectKey = project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion();
            properties.setProperty( projectKey + "/" + key, value );

            try ( OutputStream stream = Files.newOutputStream( propertiesFile ) )
            {
                properties.store( stream, "XMvn project properties" );
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to set project property", e );
        }
    }

    void afterMojoExecution( Object mojo, MojoExecution execution, MavenProject project )
    {
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

    void beforeMojoExecution( Object mojo, MojoExecution execution )
    {
        // Disable doclint
        if ( JAVADOC_AGGREGATE.equals( execution ) )
        {
            trySetBeanProperty( mojo, "additionalparam", "-Xdoclint:none" );
        }
        else if ( XMVN_BUILDDEP.equals( execution ) )
        {
            trySetBeanProperty( mojo, "resolutions", Collections.unmodifiableList( new ArrayList<>( resolutions ) ) );
        }
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
