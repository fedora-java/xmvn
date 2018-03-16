/*-
 * Copyright (c) 2013-2018 Red Hat, Inc.
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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * @author Mikolaj Izdebski
 */
public class BisectCli
{
    private static final int BISECT_MAX = 1000000000;

    private final Invoker invoker = new DefaultInvoker();

    private InvocationRequest request;

    private Path counter;

    private boolean executeBuild( String logPath )
        throws MavenInvocationException
    {
        try ( PrintWriter log = new PrintWriter( logPath ) )
        {
            request.setOutputHandler( log::println );
            request.setErrorHandler( log::println );

            return invoker.execute( request ).getExitCode() == 0;
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static String getBuildLogName( int buildId )
    {
        return String.format( "bisect-build-%d.log", buildId );
    }

    private static String getInitialBuildName()
    {
        return "bisect-initial.log";
    }

    private void setValue( int value )
        throws Exception
    {
        Files.write( counter, Collections.singleton( Integer.toString( value ) ) );
    }

    private int getValue()
        throws Exception
    {
        return Integer.parseInt( Files.readAllLines( counter ).iterator().next() );
    }

    private void run( BisectCliRequest commandLineParser )
        throws Exception
    {
        request = commandLineParser.createInvocationRequest();
        request.setShellEnvironmentInherited( true );

        commandLineParser.getSystemProperties().forEach( ( key, value ) -> System.setProperty( key, value ) );

        Path xmvnHome = XMvnHomeLocator.getHome();
        invoker.setMavenHome( xmvnHome.toFile() );
        System.err.printf( "Using XMvn at %s%n", xmvnHome );

        request.getProperties().put( "xmvn.bisect.counter", commandLineParser.getCounterPath() );

        int counterInitialValue = BISECT_MAX;
        counter = Paths.get( commandLineParser.getCounterPath() );
        setValue( 0 );

        if ( !commandLineParser.isSkipSanityChecks() )
        {
            System.err.println( "Checking if standard local build really fails" );
            boolean success = executeBuild( getBuildLogName( 0 ) );
            if ( success )
            {
                System.err.println( "ERROR: Standard local build was successful, expected failure." );
                System.err.printf( "Build log is available in %s%n", getBuildLogName( 0 ) );
                System.exit( 1 );
            }
        }

        int badId = 0;
        System.err.println( "Running initial upstream build" );
        setValue( counterInitialValue );
        boolean success = executeBuild( getInitialBuildName() );
        int goodId = counterInitialValue - getValue();
        if ( !success )
        {
            System.err.println( "ERROR: Build failed even when resolving artifacts completely from bisection repository" );
            System.err.printf( "Build log is available in %s%n", getInitialBuildName() );
            System.exit( 1 );
        }

        while ( goodId - badId > 1 )
        {
            int tryId;
            if ( commandLineParser.useBinarySearch() )
                tryId = badId + ( goodId - badId ) / 2;
            else
                tryId = badId + 1;

            System.err.printf( "Bisection iteration: current range is [%d,%d], trying %d%n", badId + 1, goodId - 1,
                               tryId );
            setValue( tryId );

            success = executeBuild( getBuildLogName( tryId ) );
            System.err.printf( "Bisection build number %d %s%n", tryId, success ? "succeeded" : "failed" );

            if ( success )
                goodId = tryId;
            else
                badId = tryId;
        }

        String goodLog = getBuildLogName( goodId );
        if ( goodId == counterInitialValue )
            goodLog = getInitialBuildName();
        String badLog = getBuildLogName( badId );
        if ( badId == 0 )
            badLog = "default.log";

        System.err.println( "Bisection build finished" );
        System.err.printf( "Failed build:     %d, see %s%n", badId, badLog );
        System.err.printf( "Successful build: %d, see %s%n", goodId, goodLog );
        System.err.println( "Try:" );
        System.err.printf( "  $ git diff --no-index --color %s %s%n", badLog, goodLog );
    }

    public static void main( String[] args )
        throws Exception
    {
        try
        {
            BisectCliRequest cliRequest = new BisectCliRequest( args );

            BisectCli cli = new BisectCli();

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            // Helper exceptions used with our integration tests should be ignored
            if ( e.getClass().getName().startsWith( "org.fedoraproject.xmvn.it." ) )
                throw (RuntimeException) e;

            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
