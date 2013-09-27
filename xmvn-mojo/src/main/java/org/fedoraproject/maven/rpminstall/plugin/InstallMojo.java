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

import static org.fedoraproject.maven.rpminstall.plugin.Utils.aetherArtifact;
import static org.fedoraproject.maven.rpminstall.plugin.Utils.saveEffectivePom;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
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

                logger.error( "Reactor project " + aetherArtifact( project.getArtifact() )
                    + " has system-scoped dependencies: " + ArtifactUtils.collectionToString( systemDeps, true ) );
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

    private Xpp3Dom readInstallationPlan()
        throws IOException, MojoExecutionException
    {
        try (Reader reader = new FileReader( ".xmvn-reactor" ))
        {
            return Xpp3DomBuilder.build( reader );
        }
        catch ( FileNotFoundException e )
        {
            return new Xpp3Dom( "reactorInstallationPlan" );
        }
        catch ( XmlPullParserException e )
        {
            throw new MojoExecutionException( "Failed to parse existing reactor installation plan", e );
        }
    }

    private void addChild( Xpp3Dom parent, String tag, Object value )
    {
        if ( value != null )
        {
            Xpp3Dom child = new Xpp3Dom( tag );
            child.setValue( value.toString() );
            parent.addChild( child );
        }
    }

    private void addArtifact( Xpp3Dom parent, Artifact artifact, Path rawPom, Path effectivePom )
    {
        Xpp3Dom child = ArtifactUtils.toXpp3Dom( artifact, "artifact" );

        addChild( child, "file", artifact.getFile() );
        addChild( child, "rawPomPath", rawPom );
        addChild( child, "effectivePomPath", effectivePom );

        parent.addChild( child );
    }

    private void writeInstallationPlan( Xpp3Dom dom )
        throws IOException
    {
        try (Writer writer = new FileWriter( ".xmvn-reactor" ))
        {
            XmlSerializer s = new MXSerializer();
            s.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  " );
            s.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
            s.setOutput( writer );
            s.startDocument( "US-ASCII", null );
            s.comment( " Reactor installation plan generated by XMvn " );
            s.text( "\n" );

            dom.writeToSerializer( null, s );

            s.endDocument();
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        handleSystemDependencies();

        try
        {
            Xpp3Dom dom = readInstallationPlan();

            for ( MavenProject project : reactorProjects )
            {
                Artifact mainArtifact = aetherArtifact( project.getArtifact() );
                mainArtifact = mainArtifact.setFile( project.getArtifact().getFile() );
                logger.debug( "Installing main artifact " + mainArtifact );
                logger.debug( "Artifact file is " + mainArtifact.getFile() );

                Path rawPom = project.getFile().toPath();
                Path effectivePom = saveEffectivePom( project.getModel() );
                logger.debug( "Raw POM path: " + rawPom );
                logger.debug( "Effective POM path: " + effectivePom );

                addArtifact( dom, mainArtifact, rawPom, effectivePom );

                for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
                {
                    Artifact attachedArtifact = aetherArtifact( mavenArtifact );
                    attachedArtifact = attachedArtifact.setFile( mavenArtifact.getFile() );
                    logger.debug( "Installing attached artifact " + attachedArtifact );

                    addArtifact( dom, attachedArtifact, null, null );
                }
            }

            writeInstallationPlan( dom );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
