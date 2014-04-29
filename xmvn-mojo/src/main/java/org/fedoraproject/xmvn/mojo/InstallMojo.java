/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import static org.fedoraproject.xmvn.mojo.Utils.aetherArtifact;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
@Named
public class InstallMojo
    extends AbstractMojo
{
    private static final Set<String> TYCHO_PACKAGING_TYPES = new LinkedHashSet<>();

    private static final Set<String> TYCHO_P2_CLASSIFIERS = new LinkedHashSet<>();

    static
    {
        TYCHO_PACKAGING_TYPES.add( "eclipse-plugin" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-test-plugin" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-feature" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-update-site" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-application" );
        TYCHO_PACKAGING_TYPES.add( "eclipse-repository" );

        for ( String packaging : TYCHO_PACKAGING_TYPES )
            TYCHO_P2_CLASSIFIERS.add( "p2." + packaging );
    }

    private static boolean isTychoInjectedDependency( Dependency dependency )
    {
        return TYCHO_P2_CLASSIFIERS.contains( dependency.getGroupId() );
    }

    private static boolean isTychoProject( MavenProject project )
    {
        return TYCHO_PACKAGING_TYPES.contains( project.getPackaging() );
    }

    private final Logger logger = LoggerFactory.getLogger( InstallMojo.class );

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Parameter( readonly = true, defaultValue = "${repositorySystemSession}" )
    private RepositorySystemSession repoSession;

    private final Deployer deployer;

    @Inject
    public InstallMojo( Deployer deployer )
    {
        this.deployer = deployer;
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
                if ( isTychoProject( project ) && isTychoInjectedDependency( dependency ) )
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

                logger.error( "Reactor project {} has system-scoped dependencies: {}",
                              aetherArtifact( project.getArtifact() ),
                              ArtifactUtils.collectionToString( systemDeps, true ) );
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

    private void deployArtifact( Artifact artifact, Model model )
        throws MojoExecutionException
    {
        DeploymentRequest request = new DeploymentRequest();
        request.setArtifact( artifact );

        for ( Dependency dependency : model.getDependencies() )
        {
            String scope = dependency.getScope();
            if ( scope == null || scope.equals( "compile" ) || scope.equals( "runtime" ) )
            {
                Artifact dependencyArtifact;
                String mappedExtension = null;
                String mappedClassifier = null;
                if ( repoSession != null && dependency.getType() != null )
                {
                    ArtifactType type = repoSession.getArtifactTypeRegistry().get( dependency.getType() );
                    if ( type != null )
                    {
                        mappedExtension = type.getExtension();
                        mappedClassifier = type.getClassifier();
                    }
                }
                dependencyArtifact = ArtifactUtils.createTypedArtifact( dependency.getGroupId(),
                                                                        dependency.getArtifactId(),
                                                                        dependency.getType(),
                                                                        dependency.getClassifier(),
                                                                        dependency.getVersion(),
                                                                        mappedExtension,
                                                                        mappedClassifier );
                List<Artifact> exclusions = new ArrayList<>();
                for ( Exclusion e : dependency.getExclusions() )
                {
                    exclusions.add( new DefaultArtifact( e.getGroupId(), e.getArtifactId() ) );
                }

                request.addDependency( dependencyArtifact, exclusions );
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
            Artifact mainArtifact = aetherArtifact( project.getArtifact() );
            Path mainArtifactPath = mainArtifact.getPath();
            logger.debug( "Installing main artifact {}", mainArtifact );
            logger.debug( "Artifact file is {}", mainArtifactPath );

            if ( mainArtifactPath != null && !Files.isRegularFile( mainArtifactPath ) )
            {
                logger.info( "Skipping installation of artifact {}: artifact file is not a regular file",
                             mainArtifactPath );
                mainArtifactPath = null;
            }

            if ( mainArtifactPath != null )
                deployArtifact( mainArtifact, project.getModel() );

            Artifact rawPomArtifact =
                new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), "pom",
                                     mainArtifact.getClassifier(), mainArtifact.getVersion() );
            File rawPomFile = project.getFile();
            Path rawPomPath = rawPomFile != null ? rawPomFile.toPath() : null;
            logger.debug( "Raw POM path: {}", rawPomPath );
            rawPomArtifact = rawPomArtifact.setPath( rawPomPath );
            deployArtifact( rawPomArtifact, project.getModel() );

            for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
            {
                Artifact attachedArtifact = aetherArtifact( mavenArtifact );
                Path attachedArtifactPath = attachedArtifact.getPath();
                logger.debug( "Installing attached artifact {}", attachedArtifact );
                logger.debug( "Artifact file is {}", attachedArtifactPath );

                if ( attachedArtifactPath != null && !Files.isRegularFile( attachedArtifactPath ) )
                {
                    logger.info( "Skipping installation of attached artifact {}: artifact file is not a regular file",
                                 attachedArtifact );
                    continue;
                }

                deployArtifact( attachedArtifact, project.getModel() );
            }
        }
    }
}
