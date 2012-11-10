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
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    private void installProject( MavenProject project, Package targetPackage )
        throws MojoExecutionException
    {
        Artifact artifact = project.getArtifact();
        File pomFile = project.getFile();
        File file = artifact.getFile();

        String packaging = project.getPackaging();
        if ( !packaging.equals( "pom" ) && file == null )
            throw new MojoExecutionException(
                                              "Failed to install project "
                                                  + artifact.getGroupId()
                                                  + ":"
                                                  + artifact.getArtifactId()
                                                  + ": Packaging is not \"pom\" but artifact file is null. Make sure you run rpminstall plugin after \"package\" phase." );

        targetPackage.addPomFile( pomFile, artifact );

        if ( file != null )
        {
            if ( !file.getName().endsWith( ".jar" ) )
            {
                throw new MojoExecutionException( "Artifact file name \"" + file.getName()
                    + "\" has unsupported extension. The only supported extension is \".jar\"" );
            }

            targetPackage.addJarFile( file, artifact );
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Set<Package> packages = new TreeSet<>();

        String packageName = rootProject.getArtifactId();
        Package mainPackage = new Package( packageName );
        packages.add( mainPackage );

        for ( MavenProject project : reactorProjects )
        {
            installProject( project, mainPackage );
        }

        File buildRoot = new File( ".m2-rpminstall-root" );
        if ( buildRoot.exists() )
            buildRoot.delete();
        buildRoot.mkdir();

        Installer installer = new Installer( buildRoot );
        try
        {
            for ( Package pkg : packages )
                pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
