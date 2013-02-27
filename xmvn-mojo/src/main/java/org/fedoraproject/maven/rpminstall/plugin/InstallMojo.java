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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.installer.DefaultPackage;
import org.fedoraproject.maven.installer.Installer;
import org.fedoraproject.maven.installer.ProjectInstallationException;
import org.fedoraproject.maven.installer.ProjectInstaller;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
@Component( role = InstallMojo.class )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Requirement
    private Configurator configurator;

    @Requirement
    private List<ProjectInstaller> installers;

    private InstallerSettings settings;

    private void installProject( MavenProject project, DefaultPackage targetPackage, PackagingRule rule )
        throws MojoExecutionException
    {
        String packaging = project.getPackaging();
        String projectId = project.getGroupId() + ":" + project.getArtifactId();

        for ( ProjectInstaller installer : installers )
        {
            if ( !installer.getSupportedPackagingTypes().contains( packaging ) )
                continue;

            try
            {
                installer.installProject( project, targetPackage, rule );
                return;
            }
            catch ( ProjectInstallationException | IOException e )
            {
                throw new MojoExecutionException( "Failed to install project " + projectId, e );
            }
        }

        throw new MojoExecutionException( "Unable to find suitable installer to install project " + projectId );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Configuration configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();

        Map<String, DefaultPackage> packages = new TreeMap<>();

        DefaultPackage mainPackage = new DefaultPackage( DefaultPackage.MAIN, settings );
        packages.put( DefaultPackage.MAIN, mainPackage );

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
                    packageName = DefaultPackage.MAIN;
                DefaultPackage pkg = packages.get( packageName );

                if ( pkg == null )
                {
                    pkg = new DefaultPackage( packageName, settings );
                    packages.put( packageName, pkg );
                }

                installProject( project, pkg, rule );
            }

            Path installRoot = Paths.get( settings.getInstallRoot() );
            Installer installer = new Installer( installRoot );

            for ( DefaultPackage pkg : packages.values() )
                if ( pkg.isInstallable() )
                    pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
