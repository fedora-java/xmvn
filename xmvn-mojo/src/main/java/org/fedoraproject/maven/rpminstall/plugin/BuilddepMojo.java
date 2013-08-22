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
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.BuildSettings;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.installer.DependencyExtractor;
import org.fedoraproject.maven.installer.DependencyVisitor;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "builddep", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
@Component( role = BuilddepMojo.class )
public class BuilddepMojo
    extends AbstractMojo
    implements DependencyVisitor
{
    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Requirement
    private Configurator configurator;

    @Requirement
    private Resolver resolver;

    private final Set<String> buildDeps = new HashSet<>();

    private final Set<String> reactorArtifacts = new HashSet<>();

    private static BigDecimal MIN_SUPPORTED_JAVA_VERSION = new BigDecimal( "1.5" );

    private BigDecimal javaVersion = null;

    // FIXME: quick & dirty version
    // In future we're going to generate XML and let system-specific tools generate requires strings.
    private static String artifactToString( Artifact artifact )
    {
        String[] s =
            new String[] { artifact.getGroupId(), artifact.getArtifactId(),
                artifact.getExtension().equals( "jar" ) ? "" : artifact.getExtension(), artifact.getClassifier(),
                artifact.getVersion().equals( "SYSTEM" ) ? "" : artifact.getVersion() };

        int n =
            Math.max( Math.max( 1, s[4].isEmpty() ? 0 : 2 ), Math.max( s[2].isEmpty() ? 0 : 3, s[3].isEmpty() ? 0 : 4 ) );
        s[Math.max( n, 2 )] = s[4];
        while ( n-- > 0 )
            s[n] += ":" + s[n + 1];

        String scope = ArtifactUtils.getScope( artifact );
        return ( StringUtils.isNotEmpty( scope ) ? scope + "-" : "" ) + "mvn(" + s[0] + ")";
    }

    @Override
    public void visitBuildDependency( Artifact dependencyArtifact )
    {
        // FIXME: print properly formatted requires!
        buildDeps.add( artifactToString( dependencyArtifact ) );
    }

    @Override
    public void visitRuntimeDependency( Artifact dependencyArtifact )
    {
        visitBuildDependency( dependencyArtifact );
    }

    @Override
    public void visitJavaVersionDependency( BigDecimal version )
    {
        if ( javaVersion == null || javaVersion.compareTo( version ) < 0 )
            javaVersion = version;
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        BuildSettings settings = configurator.getConfiguration().getBuildSettings();

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                String groupId = project.getGroupId();
                String artifactId = project.getArtifactId();
                reactorArtifacts.add( "mvn(" + groupId + ":" + artifactId + ")" );

                Model rawModel = DependencyExtractor.getRawModel( project );
                DependencyExtractor.generateRawRequires( resolver, rawModel, this );
                DependencyExtractor.generateEffectiveBuildRequires( resolver, project.getModel(), this, settings );

                if ( !project.getPackaging().equals( "pom" ) )
                    DependencyExtractor.getJavaCompilerTarget( project, this );
            }

            try (PrintStream ps = new PrintStream( ".xmvn-builddep" ))
            {
                ps.println( "BuildRequires:  maven-local" );

                if ( javaVersion != null && javaVersion.compareTo( MIN_SUPPORTED_JAVA_VERSION ) > 0 )
                {
                    ps.println( "BuildRequires:  java-devel >= 1:" + javaVersion );
                }

                buildDeps.removeAll( reactorArtifacts );
                for ( String dep : buildDeps )
                    ps.println( "BuildRequires:  " + dep );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to generate build dependencies", e );
        }
    }
}
