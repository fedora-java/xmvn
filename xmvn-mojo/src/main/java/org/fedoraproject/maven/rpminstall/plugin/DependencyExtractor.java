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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
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
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fedoraproject.maven.config.BuildSettings;

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
        Writer stringWriter = new StringWriter();
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        pomWriter.write( stringWriter, model );

        try (Writer fWriter = new FileWriter( path.toFile() ))
        {
            XMLWriter writer = new PrettyPrintXMLWriter( fWriter, "  ", "UTF-8", null );
            writer.writeMarkup( stringWriter.toString().replaceAll( "<\\?xml[^>]+\\?>", "" ) );
        }
    }

    private static void generateEffectiveRequires( Model model, DependencyVisitor visitor, String[] scopes )
    {
        for ( Dependency dep : model.getDependencies() )
        {
            String scope = dep.getScope();
            if ( Arrays.binarySearch( scopes, scope ) >= 0 )
                visitor.visitRuntimeDependency( dep.getGroupId(), dep.getArtifactId() );
        }
    }

    public static void generateEffectiveRuntimeRequires( Model model, DependencyVisitor visitor )
    {
        generateEffectiveRequires( model, visitor, runtimeScopes );
    }

    public static void generateEffectiveBuildRequires( Model model, DependencyVisitor visitor, BuildSettings settings )
    {
        String[] scopes = settings.isSkipTests() ? buildOnlyScopes : buildAndTestScopes;
        generateEffectiveRequires( model, visitor, scopes );
    }

    public static void generateRawRequires( Model model, DependencyVisitor visitor )
    {
        Parent parent = model.getParent();
        if ( parent != null )
        {
            String groupId = parent.getGroupId();
            if ( groupId == null )
                groupId = model.getGroupId();
            visitor.visitBuildDependency( groupId, parent.getArtifactId() );
        }

        if ( model.getPackaging().equals( "pom" ) && model.getBuild() != null )
        {
            for ( Plugin plugin : model.getBuild().getPlugins() )
            {
                String groupId = plugin.getGroupId();
                if ( groupId == null )
                    groupId = "org.apache.maven.plugins";
                visitor.visitBuildDependency( groupId, plugin.getArtifactId() );
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
