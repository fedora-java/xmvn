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
import static org.fedoraproject.xmvn.mojo.Utils.saveEffectivePom;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
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

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    private final Deployer deployer;

    @Inject
    public InstallMojo( Deployer deployer )
    {
        this.deployer = deployer;
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

    private void deployArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        DeploymentRequest request = new DeploymentRequest();
        request.setArtifact( artifact );

        DeploymentResult result = deployer.deploy( request );
        if ( result.getException() != null )
            throw new MojoExecutionException( "Failed to deploy artifact " + artifact, result.getException() );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        handleSystemDependencies();

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                Artifact mainArtifact = aetherArtifact( project.getArtifact() );
                mainArtifact = mainArtifact.setFile( project.getArtifact().getFile() );
                logger.debug( "Installing main artifact {}", mainArtifact );
                logger.debug( "Artifact file is {}", mainArtifact.getFile() );

                if ( mainArtifact.getFile() != null )
                    deployArtifact( mainArtifact );

                if ( mainArtifact.getFile() != null && !isTychoProject( project ) )
                {
                    Artifact effectivePomArtifact =
                        new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), "pom",
                                             mainArtifact.getClassifier(), mainArtifact.getVersion() );
                    effectivePomArtifact = effectivePomArtifact.setStereotype( "effective" );
                    Path effectivePom = saveEffectivePom( project.getModel() );
                    logger.debug( "Effective POM path: {}", effectivePom );
                    effectivePomArtifact = effectivePomArtifact.setFile( effectivePom.toFile() );
                    deployArtifact( effectivePomArtifact );
                }

                Artifact rawPomArtifact =
                    new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), "pom",
                                         mainArtifact.getClassifier(), mainArtifact.getVersion() );
                rawPomArtifact = rawPomArtifact.setStereotype( "raw" );
                Path rawPom = project.getFile().toPath();
                logger.debug( "Raw POM path: {}", rawPom );
                rawPomArtifact = rawPomArtifact.setFile( rawPom.toFile() );
                deployArtifact( rawPomArtifact );

                for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
                {
                    Artifact attachedArtifact = aetherArtifact( mavenArtifact );
                    attachedArtifact = attachedArtifact.setFile( mavenArtifact.getFile() );
                    logger.debug( "Installing attached artifact {}", attachedArtifact );

                    deployArtifact( attachedArtifact );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
