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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.installer.InstallationRequest;
import org.fedoraproject.maven.installer.old.DependencyExtractor;
import org.fedoraproject.maven.utils.ArtifactUtils;

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
    private Logger logger;

    private Artifact aetherArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
    {
        String groupId = mavenArtifact.getGroupId();
        String artifactId = mavenArtifact.getArtifactId();
        String version = mavenArtifact.getVersion();
        String stereotype = mavenArtifact.getType();

        ArtifactHandler handler = mavenArtifact.getArtifactHandler();
        String extension = handler.getExtension();
        String classifier = handler.getClassifier();
        if ( StringUtils.isNotEmpty( mavenArtifact.getClassifier() ) )
            classifier = mavenArtifact.getClassifier();

        File artifactFile = mavenArtifact.getFile();

        Artifact artifact = new DefaultArtifact( groupId, artifactId, classifier, extension, version );
        artifact = ArtifactUtils.setStereotype( artifact, stereotype );
        artifact = artifact.setFile( artifactFile );
        return artifact;
    }

    private Path saveEffectivePom( Model model )
        throws MojoExecutionException
    {
        try
        {
            DependencyExtractor.simplifyEffectiveModel( model );
            Path source = Files.createTempFile( "xmvn", ".pom.xml" );
            DependencyExtractor.writeModel( model, source );
            return source;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to write POM file: ", e );
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        InstallationRequest request = new InstallationRequest();

        for ( MavenProject project : reactorProjects )
        {
            Artifact mainArtifact = aetherArtifact( project.getArtifact() );
            mainArtifact = mainArtifact.setFile( project.getArtifact().getFile() );
            Path rawPom = project.getFile().toPath();
            Path effectivePom = saveEffectivePom( project.getModel() );
            request.addArtifact( mainArtifact, rawPom, effectivePom );

            for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
            {
                Artifact attachedArtifact = aetherArtifact( mavenArtifact );
                attachedArtifact.setFile( mavenArtifact.getFile() );
                request.addArtifact( mainArtifact, null, null );
            }
        }
    }
}
