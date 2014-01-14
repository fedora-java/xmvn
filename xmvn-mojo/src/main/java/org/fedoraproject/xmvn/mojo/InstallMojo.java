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

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.utils.ArtifactUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
@Component( role = InstallMojo.class )
public class InstallMojo
    extends AbstractMojo
{
    private final Logger logger = LoggerFactory.getLogger( InstallMojo.class );

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Requirement
    private Deployer deployer;

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
                if ( dependency.getScope() != null && dependency.getScope().equals( "system" ) )
                {
                    systemDeps.add( new DefaultArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                                         dependency.getType(), dependency.getClassifier(),
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

    private void deployArtifact( Artifact artifact, Path rawPom, Path effectivePom )
        throws MojoExecutionException
    {
        DeploymentRequest request = new DeploymentRequest();
        request.setArtifact( artifact );
        request.setRawModelPath( rawPom );
        request.setEffectiveModelPath( effectivePom );

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

                Path rawPom = project.getFile().toPath();
                Path effectivePom = saveEffectivePom( project.getModel() );
                logger.debug( "Raw POM path: {}", rawPom );
                logger.debug( "Effective POM path: {}", effectivePom );

                deployArtifact( mainArtifact, rawPom, effectivePom );

                for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
                {
                    Artifact attachedArtifact = aetherArtifact( mavenArtifact );
                    attachedArtifact = attachedArtifact.setFile( mavenArtifact.getFile() );
                    logger.debug( "Installing attached artifact {}", attachedArtifact );

                    deployArtifact( attachedArtifact, null, null );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
