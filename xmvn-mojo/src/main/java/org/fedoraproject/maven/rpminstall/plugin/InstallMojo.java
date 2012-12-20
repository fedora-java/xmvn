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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
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
        DependencyVisitor metadata = targetPackage.getMetadata();

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

            targetPackage.addJarFile( file, baseFile, extraList );

            DependencyExtractor.getJavaCompilerTarget( project, metadata );
        }
        else
        {
            pomFile = project.getFile().toPath();
            jppGroup = Paths.get( "JPP" ).resolve( Configuration.getInstallName() );
            jppName = Paths.get( groupId + "@" + artifactId );

            Model rawModel = DependencyExtractor.getRawModel( project );
            DependencyExtractor.generateRawRequires( rawModel, metadata );
        }

        targetPackage.addPomFile( pomFile, jppGroup, jppName );
        targetPackage.createDepmaps( groupId, artifactId, version, jppGroup, jppName );

        DependencyExtractor.generateEffectiveRuntimeRequires( project.getModel(), metadata );
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
