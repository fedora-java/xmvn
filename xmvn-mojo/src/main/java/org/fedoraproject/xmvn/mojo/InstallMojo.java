/*-
 * Copyright (c) 2012-2019 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import static org.fedoraproject.xmvn.mojo.Utils.xmvnArtifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
public class InstallMojo
    extends AbstractMojo
{
    private static final Set<String> TYCHO_PACKAGING_TYPES = new LinkedHashSet<>();

    static
    {
        TYCHO_PACKAGING_TYPES.add( "eclipse-plugin" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-test-plugin" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-feature" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-update-site" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-application" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-repository" );
    }

    private static boolean isTychoProject( MavenProject project )
    {
        return TYCHO_PACKAGING_TYPES.contains( project.getPackaging() );
    }

    @Component
    private Logger logger;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Parameter( readonly = true, defaultValue = "${repositorySystemSession}" )
    private RepositorySystemSession repoSession;

    @Component
    private Deployer deployer;

    public InstallMojo()
    {
        // No-argument constructor is required by Plexus
    }

    InstallMojo( Deployer deployer, Logger logger )
    {
        this.deployer = deployer;
        this.logger = logger;
    }

    void setReactorProjects( List<MavenProject> reactorProjects )
    {
        this.reactorProjects = reactorProjects;
    }

    /**
     * Dump project dependencies with "system" scope and fail if there are any such dependencies are found.
     */
    private void handleSystemDependencies()
        throws MojoFailureException
    {
        boolean systemDepsFound = false;

        for ( MavenProject project : reactorProjects )
        {
            Set<Artifact> systemDeps = new LinkedHashSet<>();

            for ( Dependency dependency : project.getModel().getDependencies() )
            {
                // Ignore dependencies injected by Tycho
                if ( isTychoProject( project ) )
                    continue;

                if ( dependency.getScope() != null && dependency.getScope().equals( "system" ) )
                {
                    systemDeps.add( new DefaultArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                                         dependency.getClassifier(), dependency.getType(),
                                                         dependency.getVersion() ) );
                }
            }

            if ( !systemDeps.isEmpty() )
            {
                systemDepsFound = true;

                logger.error( "Reactor project " + xmvnArtifact( project.getArtifact() )
                    + " has system-scoped dependencies: " + Utils.collectionToString( systemDeps, true ) );
            }
        }

        if ( systemDepsFound )
        {
            throw new MojoFailureException( "Some reactor artifacts have dependencies with scope \"system\"."
                + " Such dependencies are not supported by XMvn installer."
                + " You should either remove any dependencies with scope \"system\""
                + " before the build or not run XMvn instaler." );
        }
    }

    private String getProjectProperty( Artifact artifact, String key )
    {
        Path propertiesPath = Paths.get( ".xmvn/properties" );
        if ( !Files.exists( propertiesPath ) )
            return null;

        Properties properties = new Properties();
        try ( InputStream stream = Files.newInputStream( propertiesPath ) )
        {
            properties.load( stream );
        }
        catch ( IOException e )
        {
            return null;
        }

        String artifactKey = artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion();
        return properties.getProperty( artifactKey + "/" + key );
    }

    private void deployArtifact( Artifact artifact, String type, Model model )
        throws MojoExecutionException
    {
        DeploymentRequest request = new DeploymentRequest();
        request.setArtifact( artifact );
        request.addProperty( "type", type );
        request.addProperty( "requiresJava", getProjectProperty( artifact, "compilerTarget" ) );

        for ( Dependency dependency : model.getDependencies() )
        {
            String scope = dependency.getScope();
            if ( scope == null || scope.equals( "compile" ) || scope.equals( "runtime" ) )
            {
                Artifact dependencyArtifact = Utils.dependencyArtifact( repoSession, dependency );

                List<Artifact> exclusions = new ArrayList<>();
                for ( Exclusion e : dependency.getExclusions() )
                {
                    exclusions.add( new DefaultArtifact( e.getGroupId(), e.getArtifactId() ) );
                }

                request.addDependency( dependencyArtifact, dependency.isOptional(), exclusions );
            }
        }

        DeploymentResult result = deployer.deploy( request );
        if ( result.getException() != null )
            throw new MojoExecutionException( "Failed to deploy artifact " + artifact, result.getException() );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        handleSystemDependencies();

        for ( MavenProject project : reactorProjects )
        {
            Artifact mainArtifact = xmvnArtifact( project.getArtifact() );
            Path mainArtifactPath = mainArtifact.getPath();
            logger.debug( "Installing main artifact " + mainArtifact );
            logger.debug( "Artifact file is " + mainArtifactPath );

            if ( mainArtifactPath != null && !Files.isRegularFile( mainArtifactPath ) )
            {
                logger.info( "Skipping installation of artifact " + mainArtifactPath
                    + ": artifact file is not a regular file" );
                mainArtifactPath = null;
            }

            String type = project.getPackaging();
            if ( mainArtifactPath != null )
                deployArtifact( mainArtifact, type, project.getModel() );

            if ( !isTychoProject( project ) )
            {
                Artifact rawPomArtifact =
                    new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), "pom",
                                         mainArtifact.getClassifier(), mainArtifact.getVersion() );
                File rawPomFile = project.getFile();
                Path rawPomPath = rawPomFile != null ? rawPomFile.toPath() : null;
                logger.debug( "Raw POM path: " + rawPomPath );
                rawPomArtifact = rawPomArtifact.setPath( rawPomPath );
                deployArtifact( rawPomArtifact, type, project.getModel() );
            }

            Set<Artifact> attachedArtifacts = new LinkedHashSet<>();
            for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
                attachedArtifacts.add( xmvnArtifact( mavenArtifact ) );

            for ( Artifact attachedArtifact : attachedArtifacts )
            {
                Path attachedArtifactPath = attachedArtifact.getPath();
                logger.debug( "Installing attached artifact " + attachedArtifact );
                logger.debug( "Artifact file is " + attachedArtifactPath );

                if ( attachedArtifactPath != null && !Files.isRegularFile( attachedArtifactPath ) )
                {
                    logger.info( "Skipping installation of attached artifact " + attachedArtifact
                        + ": artifact file is not a regular file" );
                    continue;
                }

                deployArtifact( attachedArtifact, type, project.getModel() );
            }
        }
    }
}
