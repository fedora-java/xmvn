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
package org.fedoraproject.maven.installer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.BuildSettings;
import org.fedoraproject.maven.resolver.ResolutionRequest;
import org.fedoraproject.maven.resolver.ResolutionResult;
import org.fedoraproject.maven.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
public class DependencyExtractor
{
    // List of dependency scopes for which auto-requires are generated. Must be in ascending order.
    private static final String[] runtimeScopes = new String[] { "compile", "runtime" };

    private static final String[] buildAndTestScopes = new String[] { "compile", "provided", "test" };

    private static final String[] buildOnlyScopes = new String[] { "compile", "provided", };

    /**
     * Obtain raw model for given project.
     * 
     * @param project project to get model of
     * @return raw model
     * @throws IOException if parsing XML file fails
     */
    public static Model getRawModel( MavenProject project )
        throws IOException
    {
        try
        {
            try (Reader reader = new FileReader( project.getFile() ))
            {
                return new MavenXpp3Reader().read( reader );
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "Failed to parse POM file", e );
        }
    }

    public static void simplifyEffectiveModel( Model model )
    {
        model.setParent( null );
    }

    public static void writeModel( Model model, Path path )
        throws IOException
    {
        try (Writer writer = new FileWriter( path.toFile() ))
        {
            MavenXpp3Writer pomWriter = new MavenXpp3Writer();
            pomWriter.write( writer, model );
        }
    }

    private static void generateEffectiveRequires( Resolver resolver, Model model, DependencyVisitor visitor,
                                                   String[] scopes )
    {
        for ( Dependency dep : model.getDependencies() )
        {
            String scope = dep.getScope();
            if ( Arrays.binarySearch( scopes, scope ) >= 0 )
            {
                Artifact artifact =
                    new DefaultArtifact( dep.getGroupId(), dep.getArtifactId(), dep.getClassifier(), dep.getType(),
                                         dep.getVersion() );
                ResolutionResult result = resolver.resolve( new ResolutionRequest( artifact ) );
                if ( result.getArtifactFile() == null )
                    throw new RuntimeException( "Unresolved artifact during dependency generation:" + artifact );
                String resolvedVersion = result.getCompatVersion() != null ? result.getCompatVersion() : "SYSTEM";
                artifact = artifact.setVersion( resolvedVersion );
                visitor.visitRuntimeDependency( artifact );
            }
        }
    }

    public static void generateEffectiveRuntimeRequires( Resolver resolver, Model model, DependencyVisitor visitor )
    {
        generateEffectiveRequires( resolver, model, visitor, runtimeScopes );
    }

    public static void generateEffectiveBuildRequires( Resolver resolver, Model model, DependencyVisitor visitor,
                                                       BuildSettings settings )
    {
        String[] scopes = settings.isSkipTests() ? buildOnlyScopes : buildAndTestScopes;
        generateEffectiveRequires( resolver, model, visitor, scopes );
    }

    public static void generateRawRequires( Resolver resolver, Model model, DependencyVisitor visitor )
    {
        Parent parent = model.getParent();
        if ( parent != null )
        {
            String groupId = parent.getGroupId();
            if ( groupId == null )
                groupId = model.getGroupId();

            Artifact artifact = new DefaultArtifact( groupId, parent.getArtifactId(), "pom", parent.getVersion() );
            ResolutionResult result = resolver.resolve( new ResolutionRequest( artifact ) );
            if ( result.getArtifactFile() == null )
                throw new RuntimeException( "Unresolved artifact during dependency generation:" + artifact );
            String resolvedVersion = result.getCompatVersion() != null ? result.getCompatVersion() : "SYSTEM";
            artifact = artifact.setVersion( resolvedVersion );
            visitor.visitBuildDependency( artifact );
        }

        if ( model.getPackaging().equals( "pom" ) && model.getBuild() != null )
        {
            for ( Plugin plugin : model.getBuild().getPlugins() )
            {
                String groupId = plugin.getGroupId();
                if ( groupId == null )
                    groupId = "org.apache.maven.plugins";

                Artifact artifact =
                    new DefaultArtifact( plugin.getGroupId(), plugin.getArtifactId(), "jar", plugin.getVersion() );
                ResolutionResult result = resolver.resolve( new ResolutionRequest( artifact ) );
                if ( result.getArtifactFile() == null )
                    throw new RuntimeException( "Unresolved artifact during dependency generation:" + artifact );
                String resolvedVersion = result.getCompatVersion() != null ? result.getCompatVersion() : "SYSTEM";
                artifact = artifact.setVersion( resolvedVersion );
                visitor.visitBuildDependency( artifact );
            }
        }
    }

    public static void getJavaCompilerTarget( MavenProject project, DependencyVisitor visitor )
    {
        if ( project.getBuild() == null )
            return;

        for ( Plugin plugin : project.getBuild().getPlugins() )
        {
            String groupId = plugin.getGroupId();
            String artifactId = plugin.getArtifactId();
            if ( groupId.equals( "org.apache.maven.plugins" ) && artifactId.equals( "maven-compiler-plugin" ) )
            {
                Collection<Object> configurations = new LinkedList<>();
                configurations.add( plugin.getConfiguration() );

                Collection<PluginExecution> executions = plugin.getExecutions();
                for ( PluginExecution exec : executions )
                    configurations.add( exec.getConfiguration() );

                for ( Object configObj : configurations )
                {
                    try
                    {
                        Xpp3Dom config = (Xpp3Dom) configObj;
                        BigDecimal target = new BigDecimal( config.getChild( "target" ).getValue().trim() );
                        visitor.visitJavaVersionDependency( target );
                    }
                    catch ( NullPointerException | NumberFormatException e )
                    {
                    }
                }
            }
        }
    }
}
