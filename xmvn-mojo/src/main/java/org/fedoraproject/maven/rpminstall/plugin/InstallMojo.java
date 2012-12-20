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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.Rule;

@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    // List of dependency scopes for which auto-requires are generated. Must be in ascending order.
    private static final String[] dependencyScopes = new String[] { "compile", "provided", "runtime" };

    private static Model getRawModel( MavenProject project )
        throws MojoExecutionException
    {
        try
        {
            try (Reader reader = new FileReader( project.getFile() ))
            {
                return new MavenXpp3Reader().read( reader );
            }
        }
        catch ( XmlPullParserException | IOException e )
        {
            throw new MojoExecutionException( "Failed to read POM", e );
        }
    }

    private void writeSimpleEffectiveModel( Path path, Model pom )
        throws MojoExecutionException
    {
        pom.setParent( null );

        try
        {
            Writer sWriter = new StringWriter();
            MavenXpp3Writer pomWriter = new MavenXpp3Writer();
            pomWriter.write( sWriter, pom );

            try (Writer fWriter = new FileWriter( path.toFile() ))
            {
                XMLWriter writer = new PrettyPrintXMLWriter( fWriter, "  ", "UTF-8", null );
                writer.writeMarkup( sWriter.toString().replaceAll( "<\\?xml[^>]+\\?>", "" ) );
            }
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
            if ( Arrays.binarySearch( dependencyScopes, scope ) >= 0 )
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
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getArtifactId();
        Path file = artifact.getFile() != null ? artifact.getFile().toPath() : null;

        String packaging = project.getPackaging();
        if ( !packaging.equals( "pom" ) && file == null )
            throw new MojoExecutionException(
                                              "Failed to install project "
                                                  + groupId
                                                  + ":"
                                                  + artifactId
                                                  + ": Packaging is not \"pom\" but artifact file is null. Make sure you run rpminstall plugin after \"package\" phase." );

        Path jppGroup;
        Path jppName;
        Path pomFile;

        if ( file != null )
        {
            if ( !file.getFileName().toString().endsWith( ".jar" ) )
            {
                throw new MojoExecutionException( "Artifact file name \"" + file.getFileName()
                    + "\" has unsupported extension. The only supported extension is \".jar\"" );
            }

            BigDecimal targetVersion = getJavaCompilerTarget( project );
            pomFile = Files.createTempFile( "xmvn-" + artifactId + "-", ".pom.xml" );
            writeSimpleEffectiveModel( pomFile, project.getModel() );
            List<Path> extraList = new LinkedList<>();

            for ( Rule rule : Configuration.getInstallFiles() )
                if ( rule.matches( groupId, artifactId, version ) )
                    extraList.add( Paths.get( rule.getReplacementString() ) );
            // TODO: Allow use of @1,@2,... in file name

            Path baseFile = Paths.get( Configuration.getInstallName() + "/" + artifactId );
            if ( !extraList.isEmpty() )
                baseFile = extraList.remove( 0 );

            jppGroup = baseFile.getFileName();
            jppName = Paths.get( "JPP" );
            if ( baseFile.getParent() != null )
                jppName = jppName.resolve( baseFile.getParent() );

            targetPackage.addJarFile( file, baseFile, extraList, targetVersion );
        }
        else
        {
            pomFile = project.getFile().toPath();
            jppGroup = Paths.get( "JPP" ).resolve( Configuration.getInstallName() );
            jppName = Paths.get( groupId + "@" + artifactId );
            generateRawRequires( getRawModel( project ), targetPackage );
        }

        targetPackage.addPomFile( pomFile, jppGroup, jppName );
        targetPackage.createDepmaps( groupId, artifactId, version, jppGroup, jppName );

        generateEffectiveRequires( project.getModel(), targetPackage );
    }

    private String getPackageName( Artifact artifact )
    {
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getArtifactId();

        for ( Rule rule : Configuration.getInstallLayout() )
            if ( rule.matches( groupId, artifactId, version ) )
                return rule.getReplacementString();

        return Package.MAIN;
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Map<String, Package> packages = new TreeMap<>();

        Package mainPackage = new Package( Package.MAIN );
        packages.put( Package.MAIN, mainPackage );

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                String packageName = getPackageName( project.getArtifact() );
                Package pkg = packages.get( packageName );

                if ( pkg == null )
                {
                    pkg = new Package( packageName );
                    packages.put( packageName, pkg );
                }

                installProject( project, pkg );
            }

            // TODO: make .root configurable
            Installer installer = new Installer( Paths.get( ".root" ) );

            for ( Package pkg : packages.values() )
                if ( pkg.isInstallable() )
                    pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
