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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;

@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Requirement
    private Configurator configurator;

    private InstallerSettings settings;

    private void installProject( MavenProject project, Package targetPackage, PackagingRule rule )
        throws MojoExecutionException, IOException
    {
        Artifact artifact = project.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
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
        DependencyVisitor metadata = targetPackage.getMetadata();
        String packageName = settings.getPackageName();

        if ( file != null )
        {
            if ( !file.getFileName().toString().endsWith( ".jar" ) )
            {
                throw new MojoExecutionException( "Artifact file name \"" + file.getFileName()
                    + "\" has unsupported extension. The only supported extension is \".jar\"" );
            }

            pomFile = Files.createTempFile( "xmvn-" + artifactId + "-", ".pom.xml" );
            DependencyExtractor.simplifyEffectiveModel( project.getModel() );
            DependencyExtractor.writeModel( project.getModel(), pomFile );

            List<Path> extraList = new ArrayList<>( rule.getFiles().size() );
            for ( String fileName : rule.getFiles() )
                extraList.add( Paths.get( fileName ) );

            Path baseFile = Paths.get( packageName + "/" + artifactId );
            if ( !extraList.isEmpty() )
                baseFile = extraList.remove( 0 );

            jppName = baseFile.getFileName();
            jppGroup = Paths.get( "JPP" );
            if ( baseFile.getParent() != null )
                jppGroup = jppGroup.resolve( baseFile.getParent() );

            targetPackage.addJarFile( file, baseFile, extraList );

            DependencyExtractor.getJavaCompilerTarget( project, metadata );
        }
        else
        {
            pomFile = project.getFile().toPath();
            jppGroup = Paths.get( "JPP" ).resolve( packageName );
            jppName = Paths.get( groupId + "@" + artifactId );

            Model rawModel = DependencyExtractor.getRawModel( project );
            DependencyExtractor.generateRawRequires( rawModel, metadata );
        }

        targetPackage.addPomFile( pomFile, jppGroup, jppName );
        targetPackage.createDepmaps( groupId, artifactId, version, jppGroup, jppName, rule );

        DependencyExtractor.generateEffectiveRuntimeRequires( project.getModel(), metadata );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Configuration configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();

        Map<String, Package> packages = new TreeMap<>();

        Package mainPackage = new Package( Package.MAIN, settings );
        packages.put( Package.MAIN, mainPackage );

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                String groupId = project.getGroupId();
                String artifactId = project.getArtifactId();
                String version = project.getVersion();
                PackagingRule rule = configuration.createEffectivePackagingRule( groupId, artifactId, version );
                String packageName = rule.getTargetPackage();
                if ( packageName == null )
                    packageName = Package.MAIN;
                Package pkg = packages.get( packageName );

                if ( pkg == null )
                {
                    pkg = new Package( packageName, settings );
                    packages.put( packageName, pkg );
                }

                installProject( project, pkg, rule );
            }

            Path installRoot = Paths.get( settings.getInstallRoot() );
            Installer installer = new Installer( installRoot );

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
