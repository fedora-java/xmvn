/*-
 * Copyright (c) 2013-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.bisect;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest.CheckSumPolicy;
import org.apache.maven.shared.invoker.InvocationRequest.ReactorFailureBehavior;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Mikolaj Izdebski
 */
public class BisectCliRequest
{
    @Parameter( description = "goals" )
    private List<String> goals;

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-l", "--linear" }, description = "Use linear search instead of binary search" )
    private boolean linearSearch;

    @Parameter( names = { "-C", "--counter" }, description = "Path to temporary semaphore file" )
    private String counterPath;

    @Parameter( names = { "-q", "--skip-sanity" }, description = "Skip sanity checks to speedup the process" )
    private boolean noSanityChecks;

    @Parameter( names = { "-am", "--also-make" }, description = "Enable 'also make' mode" )
    private boolean alsoMake;

    @Parameter( names = { "-amd", "--also-make-dependents" }, description = "Enable 'also make dependents' mode" )
    private boolean alsoMakeDependents;

    @Parameter( names = { "-F",
        "--base-directory" }, description = "Path to the base directory of the POM for the Maven invocation" )
    private String basedir;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    private boolean debug;

    @Parameter( names = { "-fb",
        "--failure-behavior" }, description = "Set the failure mode of the Maven invocation, one of: fail-at-end, fail-fast, fail-never" )
    private String failureBehavior;

    @Parameter( names = { "-gcp", "--global-checksum-policy" }, description = "Checksum mode" )
    private String globalChecksumPolicy;

    @Parameter( names = { "-gs", "--global-settings" }, description = "Path to the global settings" )
    private String globalSettings;

    @Parameter( names = { "-B", "--batch-mode" }, description = "Run Maven in non-interactive (batch) mode" )
    private boolean batchMode;

    @Parameter( names = { "-J",
        "--java-home" }, description = "Path to the base directory of the Java installation used to run Maven" )
    private String javaHome;

    @Parameter( names = { "-lr",
        "--local-repository" }, description = "Path to the base directory of the local repository to use for the Maven invocation" )
    private String localRepository;

    @Parameter( names = { "-O", "--maven-opts" }, description = "Set the value of the MAVEN_OPTS environment variable" )
    private String mavenOpts;

    @Parameter( names = { "-o", "--offline" }, description = "Run Maven in offline mode" )
    private boolean offline;

    @Parameter( names = { "-f", "--file" }, description = "Path to the POM for the Maven invocation" )
    private String pomFile;

    @Parameter( names = { "-P", "--activate-profiles" }, description = "Activate profiles" )
    private List<String> profiles;

    @Parameter( names = { "-pl", "--projects" }, description = "Reactor project list" )
    private List<String> projects;

    @DynamicParameter( names = { "-D", "--define" }, description = "System properties for the Maven invocation" )
    private Map<String, String> defines = new TreeMap<>();

    @Parameter( names = { "-N", "--non-recursive" }, description = "Recursion behavior of a reactor invocation" )
    private boolean nonRecursive;

    @Parameter( names = { "-rf", "--resume-from" }, description = "Resume reactor from specified project" )
    private String resumeFrom;

    @Parameter( names = { "-e",
        "--errors" }, description = "Print stack traces for exceptions during maven invocation" )
    private boolean showErrors;

    @Parameter( names = { "-V",
        "--show-version" }, description = "Enable displaying version without stopping the build" )
    private boolean showVersion;

    @Parameter( names = { "-T",
        "--threads" }, description = "Thread count, for instance 2.0C where C is core multiplied" )
    private String threads;

    @Parameter( names = { "-t", "--toolchains" }, description = "Alternate path for the user toolchains file" )
    private String toolchains;

    @Parameter( names = { "-U",
        "--update-snapshots" }, description = "Enforce an update check for plugins and snapshots" )
    private boolean updateSnapshots;

    @Parameter( names = { "-s", "--settings" }, description = "Path to user settings" )
    private String userSettings;

    public BisectCliRequest( String[] args )
    {
        setDefaultValues();

        try
        {
            JCommander jcomm = new JCommander( this );
            jcomm.setProgramName( "xmvn-bisect" );
            jcomm.parse( args );

            if ( help )
            {
                System.out.println( "xmvn-bisect: Build Maven project using bisection method" );
                System.out.println();
                jcomm.usage();
                System.exit( 0 );
            }

            for ( String param : defines.keySet() )
                System.setProperty( param, defines.get( param ) );
        }
        catch ( ParameterException e )
        {
            System.err.println( e.getMessage() + ". Specify -h for usage." );
            System.exit( 1 );
        }
    }

    private static String fileToString( File file )
    {
        if ( file != null )
        {
            return file.toString();
        }

        return null;
    }

    private static File stringToFile( String string )
    {
        if ( StringUtils.isNotEmpty( string ) )
        {
            return new File( string );
        }

        return null;
    }

    private static CheckSumPolicy stringToCheckSumPolicy( String policy )
    {
        if ( policy == null )
            return null;
        if ( policy.equals( "fail" ) )
            return CheckSumPolicy.Fail;
        if ( policy.equals( "warn" ) )
            return CheckSumPolicy.Warn;

        throw new IllegalArgumentException( "Invalid checksum policy selected" );
    }

    private static String checkSumPolicyToString( CheckSumPolicy policy )
    {
        if ( policy == null )
            return null;
        if ( policy.equals( CheckSumPolicy.Fail ) )
            return "fail";
        if ( policy.equals( CheckSumPolicy.Warn ) )
            return "warn";

        throw new IllegalArgumentException( "Invalid checksum policy selected" );
    }

    private void setDefaultValues()
    {
        String userHome = System.getProperty( "user.home" );
        if ( userHome == null )
            userHome = System.getenv( "HOME" );
        if ( userHome == null )
            throw new RuntimeException( "Failed to obtain user home path" );

        counterPath = Paths.get( "bisect-counter" ).toAbsolutePath().toString();

        InvocationRequest request = new DefaultInvocationRequest();

        alsoMake = request.isAlsoMake();
        alsoMakeDependents = request.isAlsoMakeDependents();
        debug = request.isDebug();
        failureBehavior = request.getReactorFailureBehavior().getLongOption();
        globalChecksumPolicy = checkSumPolicyToString( request.getGlobalChecksumPolicy() );
        globalSettings = Objects.toString( request.getGlobalSettingsFile(), "" );
        goals = request.getGoals();
        batchMode = request.isBatchMode();
        javaHome = fileToString( request.getJavaHome() );
        localRepository = fileToString( request.getLocalRepositoryDirectory( null ) );
        mavenOpts = request.getMavenOpts();
        offline = request.isOffline();
        pomFile = fileToString( request.getPomFile() );
        profiles = request.getProfiles();
        projects = request.getProjects();
        nonRecursive = !request.isRecursive();
        resumeFrom = request.getResumeFrom();
        showErrors = request.isShowErrors();
        showVersion = request.isShowVersion();
        threads = request.getThreads();
        toolchains = fileToString( request.getToolchainsFile() );
        updateSnapshots = request.isUpdateSnapshots();
        userSettings = fileToString( request.getUserSettingsFile() );
    }

    public InvocationRequest createInvocationRequest()
    {
        InvocationRequest request = new DefaultInvocationRequest();

        request.setAlsoMake( alsoMake );
        request.setAlsoMakeDependents( alsoMakeDependents );
        request.setDebug( debug );
        request.setReactorFailureBehavior( ReactorFailureBehavior.valueOfByLongOption( failureBehavior ) );
        request.setGlobalChecksumPolicy( stringToCheckSumPolicy( globalChecksumPolicy ) );
        request.setGlobalSettingsFile( stringToFile( globalSettings ) );
        request.setGoals( goals );
        request.setBatchMode( batchMode );
        request.setJavaHome( stringToFile( javaHome ) );
        request.setLocalRepositoryDirectory( stringToFile( localRepository ) );
        request.setMavenOpts( mavenOpts );
        request.setOffline( offline );
        request.setPomFile( stringToFile( pomFile ) );
        request.setProfiles( profiles );
        request.setProjects( projects );
        request.setRecursive( !nonRecursive );
        request.setResumeFrom( resumeFrom );
        request.setShowErrors( showErrors );
        request.setShowVersion( showVersion );
        request.setThreads( threads );
        request.setToolchainsFile( stringToFile( toolchains ) );
        request.setUpdateSnapshots( updateSnapshots );
        request.setUserSettingsFile( stringToFile( userSettings ) );

        Properties properties = new Properties();
        defines.forEach( ( key, value ) -> properties.put( key, value ) );
        request.setProperties( properties );

        return request;
    }

    public Map<String, String> getSystemProperties()
    {
        return Map.copyOf( defines );
    }

    public boolean useBinarySearch()
    {
        return !linearSearch;
    }

    public String getCounterPath()
    {
        return counterPath;
    }

    public boolean isSkipSanityChecks()
    {
        return noSanityChecks;
    }
}
