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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    private static Model getRawModel( MavenProject project )
        throws MojoExecutionException
    {
        try
        {
            Reader reader = new FileReader( project.getFile() );
            try
            {
                return new MavenXpp3Reader().read( reader );
            }
            finally
            {
                reader.close();
            }
        }
        catch ( XmlPullParserException | IOException e )
        {
            throw new MojoExecutionException( "Failed to read POM", e );
        }
    }

    private void writeSimpleEffectiveModel( File file, Model pom )
        throws MojoExecutionException
    {
        pom.setParent( null );

        try
        {
            Writer sWriter = new StringWriter();
            MavenXpp3Writer pomWriter = new MavenXpp3Writer();
            pomWriter.write( sWriter, pom );
            sWriter.close();
            Writer fWriter = new FileWriter( file );
            XMLWriter writer = new PrettyPrintXMLWriter( fWriter, "  ", "UTF-8", null );
            writer.writeMarkup( sWriter.toString().replaceAll( "<\\?xml[^>]+\\?>", "" ) );
            fWriter.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write effective POM", e );
        }
    }

    private void generateEffectiveRequires( Model pom, Package targetPackage )
    {
        for ( Dependency dep : pom.getDependencies() )
        {
            String scope = dep.getScope();
            if ( scope.equals( "compile" ) || scope.equals( "runtime" ) || scope.equals( "provided" ) )
                targetPackage.addRequires( dep.getGroupId(), dep.getArtifactId() );
        }
    }

    private void generateRawRequires( Model pom, Package targetPackage )
    {
        Parent parent = pom.getParent();
        if ( parent != null )
        {
            String groupId = parent.getGroupId();
            if ( groupId == null )
                groupId = pom.getGroupId();
            targetPackage.addDevelRequires( groupId, parent.getArtifactId() );
        }

        if ( pom.getPackaging().equals( "pom" ) && pom.getBuild() != null )
        {
            for ( Plugin plugin : pom.getBuild().getPlugins() )
            {
                String groupId = plugin.getGroupId();
                if ( groupId == null )
                    groupId = "org.apache.maven.plugins";
                targetPackage.addDevelRequires( groupId, plugin.getArtifactId() );
            }
        }
    }

    private BigDecimal getJavaCompilerTarget( MavenProject project )
    {
        BigDecimal version = new BigDecimal( "1.5" );

        if ( project.getBuild() != null )
        {
            for ( Plugin plugin : project.getBuild().getPlugins() )
            {
                String groupId = plugin.getGroupId();
                String artifactId = plugin.getArtifactId();
                if ( groupId.equals( "org.apache.maven.plugins" ) && artifactId.equals( "maven-compiler-plugin" ) )
                {
                    Collection<Object> configurations = new LinkedList<Object>();
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
                            if ( version.compareTo( target ) < 0 )
                                version = target;
                        }
                        catch ( NullPointerException | NumberFormatException e )
                        {
                        }
                    }

                }
            }
        }

        return version;
    }

    private void installProject( MavenProject project, Package targetPackage )
        throws MojoExecutionException, IOException
    {
        Artifact artifact = project.getArtifact();
        File file = artifact.getFile();

        String packaging = project.getPackaging();
        if ( !packaging.equals( "pom" ) && file == null )
            throw new MojoExecutionException(
                                              "Failed to install project "
                                                  + artifact.getGroupId()
                                                  + ":"
                                                  + artifact.getArtifactId()
                                                  + ": Packaging is not \"pom\" but artifact file is null. Make sure you run rpminstall plugin after \"package\" phase." );

        if ( file != null )
        {
            if ( !file.getName().endsWith( ".jar" ) )
            {
                throw new MojoExecutionException( "Artifact file name \"" + file.getName()
                    + "\" has unsupported extension. The only supported extension is \".jar\"" );
            }

            BigDecimal targetVersion = getJavaCompilerTarget( project );
            File pomFile = File.createTempFile( "xmvn-" + project.getArtifactId() + "-", ".pom.xml" );
            writeSimpleEffectiveModel( pomFile, project.getModel() );
            targetPackage.addPomFile( pomFile, artifact );
            targetPackage.addJarFile( file, artifact, targetVersion );
        }
        else
        {
            targetPackage.addPomFile( project.getFile(), artifact );
            generateRawRequires( getRawModel( project ), targetPackage );
        }

        generateEffectiveRequires( project.getModel(), targetPackage );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Map<String, Package> packages = new TreeMap<>();
        PackagingLayout layout = new PackagingLayout();

        Package mainPackage = new Package( "" );
        packages.put( "", mainPackage );

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                String packageName = layout.getPackageName( project.getArtifactId() );
                Package pkg = packages.get( packageName );

                if ( pkg == null )
                {
                    pkg = new Package( packageName );
                    packages.put( packageName, pkg );
                }

                installProject( project, pkg );
            }

            Installer installer = new Installer( ".root" );

            for ( Package pkg : packages.values() )
                pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
