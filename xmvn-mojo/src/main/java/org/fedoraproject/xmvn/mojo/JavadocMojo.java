/*-
 * Copyright (c) 2016-2023 Red Hat, Inc.
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
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "javadoc", aggregator = true, requiresDependencyResolution = ResolutionScope.COMPILE )
public class JavadocMojo
    extends AbstractMojo
{
    @Inject
    private Logger logger;

    @Inject
    private ProjectDependenciesResolver resolver;

    @Inject
    private ToolchainManager toolchainManager;

    private ModuleGleaner moduleGleaner = new ModuleGleaner();

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

    @Parameter( property = "source", defaultValue = "${maven.compiler.source}" )
    private String source;

    @Parameter( defaultValue = "${maven.compiler.release}" )
    private String release;

    @Parameter( property = "xmvn.javadoc.ignoreJPMS" )
    private boolean ignoreJPMS;

    private List<String> options = new ArrayList<>();

    private static String quoted( Object obj )
    {
        String arg = obj.toString();
        arg = StringUtils.replace( arg, "\\", "\\\\" );
        arg = StringUtils.replace( arg, "'", "\\'" );
        return "'" + arg + "'";
    }

    // Called by XMvn lifecycle participant
    private Path getOutputDir()
    {
        return buildDirectory.toPath().resolve( "xmvn-apidocs" );
    }

    private Set<Path> findJavaSources( List<JavadocModule> modules )
        throws IOException
    {
        Set<Path> sourcePaths = modules.stream() //
                                       .map( JavadocModule::getSourcePaths ) //
                                       .flatMap( Collection::stream ) //
                                       .collect( Collectors.toSet() );

        Pattern pattern = Pattern.compile( ".*\\.java$" );

        Set<Path> found = new LinkedHashSet<>();

        for ( Path dir : sourcePaths )
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

    private void discoverModule( List<JavadocModule> modules, List<String> reactorArtifacts, MavenProject project )
    {
        if ( project == null || "pom".equals( project.getPackaging() )
            || !"java".equals( project.getArtifact().getArtifactHandler().getLanguage() ) )
        {
            return;
        }

        Path artifactPath;
        if ( project.getArtifact().getFile() != null )
        {
            artifactPath = project.getArtifact().getFile().toPath();
        }
        else if ( project.getBuild().getOutputDirectory() != null )
        {
            artifactPath = Paths.get( project.getBuild().getOutputDirectory() );
        }
        else
        {
            return;
        }

        DependencyResolutionRequest request = new DefaultDependencyResolutionRequest();
        request.setMavenProject( project );
        request.setRepositorySession( session.getRepositorySession() );
        request.setResolutionFilter( new AndDependencyFilter( new ScopeDependencyFilter( "runtime", "test" ),
                                                              new ExclusionsDependencyFilter( reactorArtifacts ) ) );

        List<Path> dependencies = new ArrayList<>();
        try
        {
            DependencyResolutionResult result = resolver.resolve( request );
            dependencies.addAll( result.getResolvedDependencies().stream() //
                                       .map( Dependency::getArtifact ) //
                                       .map( artifact -> artifact.getFile().toPath() ) //
                                       .collect( Collectors.toList() ) );
        }
        catch ( DependencyResolutionException e )
        {
            // Ignore dependency resolution errors
        }

        String moduleName = ignoreJPMS ? null : moduleGleaner.glean( artifactPath );

        List<Path> sourcePaths = project.getCompileSourceRoots().stream() //
                                        .filter( Objects::nonNull ) //
                                        .map( Paths::get ) //
                                        .map( sourcePath -> sourcePath.isAbsolute() ? sourcePath
                                                        : project.getBasedir().toPath().resolve( sourcePath ).toAbsolutePath() ) //
                                        .filter( Files::isDirectory ) //
                                        .collect( Collectors.toList() );

        modules.add( new JavadocModule( moduleName, artifactPath, sourcePaths, dependencies ) );
    }

    private List<JavadocModule> discoverModules()
    {
        List<JavadocModule> modules = new ArrayList<>();

        List<String> reactorArtifacts = reactorProjects.stream() //
                                                       .map( project -> ( project.getGroupId() + ":"
                                                           + project.getArtifactId() ) ) //
                                                       .collect( Collectors.toList() );

        for ( MavenProject project : reactorProjects )
        {
            discoverModule( modules, reactorArtifacts, project );
            if ( project.getExecutionProject() != project )
            {
                discoverModule( modules, reactorArtifacts, project.getExecutionProject() );
            }
        }

        return modules;
    }

    private void addOpt( String name )
    {
        options.add( name );
        logger.debug( "Javadc option: " + name );
    }

    private boolean addOpt( String name, String value )
    {
        return addOptPrefix( name, "", value );
    }

    private boolean addOpt( String name, Path path )
    {
        return addOpt( name, path.toString() );
    }

    private void addOpt( String name, String value, String deflt )
    {
        addOpt( name, value == null || value.isEmpty() ? deflt : value );
    }

    private boolean addOptPrefix( String name, String prefix, String value )
    {
        if ( value != null && !value.isEmpty() )
        {
            addOpt( name );
            addOpt( prefix + quoted( value ) );
            return true;
        }
        return false;
    }

    private void addOptPath( String name, Stream<Path> stream )
    {
        addOptPrefixPath( name, "", stream );
    }

    private void addOptPrefixPath( String name, String prefix, Stream<Path> stream )
    {
        addOptPrefix( name, prefix, stream.map( Path::toString ).collect( Collectors.joining( ":" ) ) );
    }

    private Path selectJavadocExecutable()
    {
        String javadocTool = null;
        Toolchain tc = toolchainManager.getToolchainFromBuildContext( "jdk", session );
        if ( tc != null )
        {
            logger.info( "Toolchain in xmvn-mojo: " + tc );
            javadocTool = tc.findTool( "javadoc" );
        }
        Path javadocExecutable;
        if ( javadocTool != null && !javadocTool.isEmpty() )
        {
            javadocExecutable = Paths.get( javadocTool );
        }
        else if ( System.getenv().containsKey( "JAVA_HOME" ) )
        {
            javadocExecutable = Paths.get( System.getenv( "JAVA_HOME" ) ).resolve( "bin" ).resolve( "javadoc" );
        }
        else
        {
            javadocExecutable = Paths.get( "/usr/bin/javadoc" );
        }
        logger.debug( "Using javadoc executable " + javadocExecutable );
        return javadocExecutable;
    }

    private void skipJPMSOnSourceBelow9()
    {
        try
        {
            ignoreJPMS = Float.parseFloat( release != null ? release : source != null ? source : "9" ) < 9;
        }
        catch ( NumberFormatException e )
        {
            // pass, we assume that we use modular Java
        }
        if ( ignoreJPMS )
        {
            logger.info( "Ignoring JPMS" );
        }
    }

    private void invokeJavadoc( Path outputDir )
        throws IOException, InterruptedException, MojoFailureException
    {
        ProcessBuilder pb = new ProcessBuilder( selectJavadocExecutable().toString(), "@args" );
        pb.directory( outputDir.toRealPath().toFile() );
        pb.redirectInput( new File( "/dev/null" ) );
        pb.redirectOutput( new File( "/dev/null" ) );
        pb.redirectError( Redirect.INHERIT );
        Process process = pb.start();

        int exitCode = process.waitFor();
        logger.debug( "javadoc exit code is " + exitCode );
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "Javadoc failed with exit code " + exitCode );
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        skipJPMSOnSourceBelow9();

        List<JavadocModule> modules = discoverModules();

        Path outputDir = getOutputDir();
        Path moduleSourcePath = outputDir.resolve( "module-source-path" );

        addOpt( "-private" );
        addOpt( "-use" );
        addOpt( "-version" );
        addOpt( "-Xdoclint:none" );
        addOpt( "-encoding", encoding, "UTF-8" );
        addOpt( "-charset", docencoding, "UTF-8" );
        addOpt( "-d", outputDir );
        addOpt( "-docencoding", docencoding, "UTF-8" );
        addOpt( "-doctitle", "Javadoc for package XXX" );

        if ( !addOpt( "--release", release ) )
        {
            addOpt( "-source", source );
        }

        addOptPath( "-classpath", modules.stream() //
                                         .filter( JavadocModule::isNotModular ) //
                                         .map( JavadocModule::getClassPaths ) //
                                         .flatMap( Collection::stream ) );

        addOptPath( "-sourcepath", modules.stream() //
                                          .filter( JavadocModule::isNotModular ) //
                                          .map( JavadocModule::getSourcePaths ) //
                                          .flatMap( Collection::stream ) );

        addOptPath( "--module-path", modules.stream() //
                                            .filter( JavadocModule::isModular ) //
                                            .map( JavadocModule::getClassPaths ) //
                                            .flatMap( Collection::stream ) );

        if ( modules.stream().anyMatch( JavadocModule::isModular ) )
        {
            addOpt( "--module-source-path", moduleSourcePath );
        }

        modules.stream() //
               .filter( JavadocModule::isModular ) //
               .forEach( module -> addOptPrefixPath( "--patch-module", module.getModuleName() + "=",
                                                     module.getSourcePaths().stream() ) );
        try
        {
            Set<Path> sourceFiles = findJavaSources( modules );

            Path modinfoJava = Paths.get( "module-info.java" );

            if ( sourceFiles.stream().map( Path::getFileName ).allMatch( modinfoJava::equals ) )
            {
                logger.warn( "Skipping Javadoc generation: no Java sources found" );
                return;
            }

            sourceFiles.stream() //
                       .filter( file -> !ignoreJPMS || !file.endsWith( modinfoJava ) ) //
                       .map( JavadocMojo::quoted ) //
                       .forEach( this::addOpt );

            Files.createDirectories( outputDir );

            for ( JavadocModule module : modules )
            {
                if ( module.isModular() )
                {
                    Files.createDirectories( moduleSourcePath.resolve( module.getModuleName() ) );
                }
            }

            Files.write( outputDir.resolve( "args" ), options, StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING );

            invokeJavadoc( outputDir );
        }
        catch ( IOException | InterruptedException e )
        {
            throw new MojoExecutionException( "Unable to execute javadoc command: " + e.getMessage(), e );
        }

        logger.debug( "Javadoc generated successfully" );
    }
}
