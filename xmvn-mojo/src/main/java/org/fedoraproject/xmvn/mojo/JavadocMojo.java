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

    private Path outputDir;

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

    // Called by XMvn lifecycle participant
    Path getOutputDir()
    {
        return outputDir;
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

        String moduleName = moduleGleaner.glean( artifactPath );

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

    private boolean writeOpts( Path outputDir, List<JavadocModule> modules, Set<Path> sourceFiles )
        throws IOException
    {
        List<String> opts = new ArrayList<>();
        opts.add( "-private" );
        opts.add( "-use" );
        opts.add( "-version" );
        opts.add( "-Xdoclint:none" );

        String sourceLevel = null;
        if ( release != null )
        {
            opts.add( "--release" );
            opts.add( quoted( release ) );
            sourceLevel = release;
        }
        else if ( source != null )
        {
            opts.add( "-source" );
            opts.add( quoted( source ) );
            sourceLevel = source;
        }

        boolean skipModuleInfo = false;
        if ( sourceLevel != null )
        {
            try
            {
                float f = Float.parseFloat( sourceLevel );
                if ( f < 9 )
                {
                    skipModuleInfo = true;
                }
            }
            catch ( Exception e )
            {
                // pass, we assume that we use modular Java
            }
        }

        Path moduleSourcePath = outputDir.resolve( "module-source-path" );
        List<Path> sourcePaths = new ArrayList<>();
        List<Path> classPath = new ArrayList<>();
        List<Path> modulePath = new ArrayList<>();
        for ( JavadocModule module : modules )
        {
            if ( module.getModuleName() == null || skipModuleInfo )
            {
                classPath.add( module.getArtifactPath() );
                classPath.addAll( module.getDependencies() );
                sourcePaths.addAll( module.getSourcePaths() );
            }
            else
            {
                modulePath.add( module.getArtifactPath() );
                modulePath.addAll( module.getDependencies() );
                Files.createDirectories( moduleSourcePath.resolve( module.getModuleName() ) );
                if ( !module.getSourcePaths().isEmpty() )
                {
                    opts.add( "--patch-module" );
                    opts.add( module.getModuleName() + "="
                        + quoted( StringUtils.join( module.getSourcePaths().iterator(), ":" ) ) );
                }
            }
        }

        if ( !classPath.isEmpty() )

        {
            opts.add( "-classpath" );
            opts.add( quoted( StringUtils.join( classPath.iterator(), ":" ) ) );
        }
        if ( !modulePath.isEmpty() )
        {
            opts.add( "--module-path" );
            opts.add( quoted( StringUtils.join( modulePath.iterator(), ":" ) ) );
        }
        if ( !sourcePaths.isEmpty() )
        {
            opts.add( "-sourcepath" );
            opts.add( quoted( StringUtils.join( sourcePaths.iterator(), ":" ) ) );
        }
        if ( Files.isDirectory( moduleSourcePath ) )
        {
            opts.add( "--module-source-path" );
            opts.add( quoted( moduleSourcePath ) );
        }
        opts.add( "-encoding" );
        opts.add( quoted( encoding ) );
        opts.add( "-charset" );
        opts.add( quoted( docencoding ) );
        opts.add( "-d" );
        opts.add( quoted( outputDir ) );
        opts.add( "-docencoding" );
        opts.add( quoted( docencoding ) );
        opts.add( "-doctitle" );
        opts.add( quoted( "Javadoc for package XXX" ) );

        if ( sourceFiles.stream().allMatch( file -> file.endsWith( "module-info.java" ) ) )
        {
            logger.info( "Skipping Javadoc generation: no Java sources found" );
            return false;
        }

        Stream<Path> sourcesToAdd = sourceFiles.stream();
        if ( skipModuleInfo )
        {
            sourcesToAdd = sourcesToAdd.filter( file -> !file.endsWith( "module-info.java" ) );
        }
        sourcesToAdd.map( JavadocMojo::quoted ).forEach( opts::add );

        Files.write( outputDir.resolve( "args" ), opts, StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING );

        return true;
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
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

        try
        {
            List<JavadocModule> modules = discoverModules();

            if ( StringUtils.isEmpty( encoding ) )
            {
                encoding = "UTF-8";
            }
            if ( StringUtils.isEmpty( docencoding ) )
            {
                docencoding = "UTF-8";
            }

            Set<Path> sourcePaths = modules.stream() //
                                           .map( JavadocModule::getSourcePaths ) //
                                           .flatMap( Collection::stream ) //
                                           .collect( Collectors.toSet() );
            Set<Path> sourceFiles = findFiles( sourcePaths, ".*\\.java" );

            Path outputDir = buildDirectory.toPath().resolve( "xmvn-apidocs" );
            Files.createDirectories( outputDir );

            if ( writeOpts( outputDir, modules, sourceFiles ) )
            {
                ProcessBuilder pb = new ProcessBuilder( javadocExecutable.toRealPath().toString(), "@args" );
                pb.directory( outputDir.toRealPath().toFile() );
                pb.redirectInput( new File( "/dev/null" ) );
                pb.redirectOutput( new File( "/dev/null" ) );
                pb.redirectError( Redirect.INHERIT );
                Process process = pb.start();

                int exitCode = process.waitFor();
                if ( exitCode != 0 )
                {
                    throw new MojoExecutionException( "Javadoc failed with exit code " + exitCode );
                }

                // Sign for XMvn lifecycle participant that javadocs were genererated
                this.outputDir = outputDir;
            }
        }
        catch ( IOException | InterruptedException e )
        {
            throw new MojoExecutionException( "Unable to execute javadoc command: " + e.getMessage(), e );
        }
    }
}
