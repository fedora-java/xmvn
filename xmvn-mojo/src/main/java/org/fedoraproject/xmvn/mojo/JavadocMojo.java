/*-
 * Copyright (c) 2016-2021 Red Hat, Inc.
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

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;

import org.fedoraproject.xmvn.config.Configurator;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "javadoc", aggregator = true, requiresDependencyResolution = ResolutionScope.COMPILE )
public class JavadocMojo
    extends AbstractMojo
{
    @Component
    private Logger logger;

    @Component
    private ProjectDependenciesResolver resolver;

    @Component
    private Configurator confugurator;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession session;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Parameter( defaultValue = "${project.build.sourceEncoding}" )
    private String encoding;

    @Parameter( defaultValue = "${project.reporting.outputEncoding}" )
    private String docencoding;

    @Parameter( defaultValue = "${project.build.directory}", required = true )
    private File buildDirectory;

    @Parameter( property = "source" )
    private String source;

    private static String quoted( Object obj )
    {
        String arg = obj.toString();
        arg = StringUtils.replace( arg, "\\", "\\\\" );
        arg = StringUtils.replace( arg, "'", "\\'" );
        return "'" + arg + "'";
    }

    private static Set<Path> findFiles( Collection<Path> dirs, String regex )
        throws IOException
    {
        Pattern pattern = Pattern.compile( regex );
        Set<Path> found = new LinkedHashSet<>();

        for ( Path dir : dirs )
        {
            try ( Stream<Path> stream = Files.find( dir, Integer.MAX_VALUE, //
                                                    ( path, attributes ) -> ( attributes.isRegularFile()
                                                        && pattern.matcher( path.getFileName().toString() ).matches() ) ) )
            {
                stream.forEach( found::add );
            }
        }

        return found;
    }

    private Path getOutputDir()
    {
        return buildDirectory.toPath().resolve( "xmvn-apidocs" );
    }

    private void populateClasspath( Collection<Path> reactorClassPath, Collection<Path> fullClassPath )
    {
        List<String> reactorArtifacts = reactorProjects.stream() //
                                                       .map( project -> ( project.getGroupId() + ":"
                                                           + project.getArtifactId() ) ) //
                                                       .collect( Collectors.toList() );

        reactorClassPath.addAll( reactorProjects.stream() //
                                                .map( project -> project.getBuild().getOutputDirectory() ) //
                                                .filter( StringUtils::isNotEmpty ) //
                                                .map( dir -> Paths.get( dir ) ) //
                                                .filter( path -> Files.isDirectory( path ) ) //
                                                .collect( Collectors.toSet() ) );
        fullClassPath.addAll( reactorClassPath );

        for ( MavenProject project : reactorProjects )
        {
            DependencyResolutionRequest request = new DefaultDependencyResolutionRequest();
            request.setMavenProject( project );
            request.setRepositorySession( session.getRepositorySession() );
            request.setResolutionFilter( new AndDependencyFilter( new ScopeDependencyFilter( "runtime", "test" ),
                                                                  new ExclusionsDependencyFilter( reactorArtifacts ) ) );

            try
            {
                DependencyResolutionResult result = resolver.resolve( request );
                fullClassPath.addAll( result.getResolvedDependencies().stream() //
                                            .map( dependency -> dependency.getArtifact() ) //
                                            .map( artifact -> artifact.getFile().toPath() ) //
                                            .collect( Collectors.toList() ) );
            }
            catch ( DependencyResolutionException e )
            {
                // Ignore dependency resolution errors
            }
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Path javadocExecutable;
        if ( System.getenv().containsKey( "JAVA_HOME" ) )
        {
            javadocExecutable = Paths.get( System.getenv( "JAVA_HOME" ) ) //
                                     .resolve( "bin" ) //
                                     .resolve( "javadoc" );
        }
        else
        {
            javadocExecutable = Paths.get( "/usr/bin/javadoc" );
        }

        try
        {
            if ( StringUtils.isEmpty( encoding ) )
            {
                encoding = "UTF-8";
            }
            if ( StringUtils.isEmpty( docencoding ) )
            {
                docencoding = "UTF-8";
            }

            Set<Path> sourcePaths = Stream.concat( reactorProjects.stream(), //
                                                   reactorProjects.stream().map( p -> p.getExecutionProject() ) ) //
                                          .filter( project -> project != null ) //
                                          .filter( project -> !project.getPackaging().equals( "pom" ) ) //
                                          .filter( project -> project.getArtifact().getArtifactHandler().getLanguage().equals( "java" ) ) //
                                          .filter( project -> project.getCompileSourceRoots() != null ) //
                                          .map( project -> project.getCompileSourceRoots().stream() //
                                                                  .filter( compileRoot -> compileRoot != null ) //
                                                                  .map( compileRoot -> Paths.get( compileRoot ) ) //
                                                                  .map( sourcePath -> sourcePath.isAbsolute()
                                                                                  ? sourcePath
                                                                                  : project.getBasedir().toPath().resolve( sourcePath ).toAbsolutePath() ) //
                                                                  .filter( sourcePath -> Files.isDirectory( sourcePath ) ) ) //
                                          .flatMap( x -> x ) //
                                          .collect( Collectors.toSet() );

            Set<Path> sourceFiles = findFiles( sourcePaths, ".*\\.java" );

            if ( sourceFiles.isEmpty() )
            {
                logger.info( "Skipping Javadoc generation: no Java sources found" );
                return;
            }

            Path outputDir = getOutputDir();
            if ( !Files.isDirectory( outputDir ) )
                Files.createDirectories( outputDir );
            outputDir = outputDir.toRealPath();

            List<String> opts = new ArrayList<>();
            opts.add( "-private" );
            opts.add( "-use" );
            opts.add( "-version" );
            opts.add( "-Xdoclint:none" );

            List<Path> reactorClassPath = new ArrayList<>();
            List<Path> fullClassPath = new ArrayList<>();
            populateClasspath( reactorClassPath, fullClassPath );

            if ( findFiles( reactorClassPath, "module-info\\.class" ).isEmpty() )
            {
                opts.add( "-classpath" );
            }
            else
            {
                opts.add( "--module-path" );
            }
            opts.add( quoted( StringUtils.join( fullClassPath.iterator(), ":" ) ) );
            opts.add( "-encoding" );
            opts.add( quoted( encoding ) );
            opts.add( "-sourcepath" );
            opts.add( quoted( StringUtils.join( sourcePaths.iterator(), ":" ) ) );
            opts.add( "-charset" );
            opts.add( quoted( docencoding ) );
            opts.add( "-d" );
            opts.add( quoted( outputDir ) );
            opts.add( "-docencoding" );
            opts.add( quoted( docencoding ) );
            opts.add( "-doctitle" );
            opts.add( quoted( "Javadoc for package XXX" ) );
            if ( source != null )
            {
                opts.add( "-source" );
                opts.add( quoted( source ) );
            }

            for ( Path file : sourceFiles )
            {
                opts.add( quoted( file ) );
            }

            Files.write( outputDir.resolve( "args" ), opts, StandardOpenOption.CREATE );

            ProcessBuilder pb = new ProcessBuilder( javadocExecutable.toRealPath().toString(), "@args" );
            pb.directory( outputDir.toFile() );
            pb.redirectInput( new File( "/dev/null" ) );
            pb.redirectOutput( new File( "/dev/null" ) );
            pb.redirectError( Redirect.INHERIT );
            Process process = pb.start();

            int exitCode = process.waitFor();
            if ( exitCode != 0 )
            {
                throw new MojoExecutionException( "Javadoc failed with exit code " + exitCode );
            }
        }
        catch ( IOException | InterruptedException e )
        {
            throw new MojoExecutionException( "Unable to execute javadoc command: " + e.getMessage(), e );
        }
    }
}
