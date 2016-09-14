/*-
 * Copyright (c) 2013-2016 Red Hat, Inc.
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

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.locator.XMvnHomeLocator;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class BisectCli
{
    private final Logger logger = LoggerFactory.getLogger( BisectCli.class );

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
        logger.info( "Using XMvn at {}", xmvnHome );

        request.getProperties().put( "xmvn.bisect.counter", commandLineParser.getCounterPath() );

        int counterInitialValue = BISECT_MAX;
        counter = Paths.get( commandLineParser.getCounterPath() );
        setValue( 0 );

        if ( !commandLineParser.isSkipSanityChecks() )
        {
            logger.info( "Checking if standard local build really fails" );
            boolean success = executeBuild( getBuildLogName( 0 ) );
            if ( success )
            {
                logger.error( "Standard local build was successful, expected failure." );
                logger.info( "Build log is available in {}", getBuildLogName( 0 ) );
                System.exit( 1 );
            }
        }

        int badId = 0;
        logger.info( "Running initial upstream build" );
        setValue( counterInitialValue );
        boolean success = executeBuild( getInitialBuildName() );
        int goodId = counterInitialValue - getValue();
        if ( !success )
        {
            logger.error( "Build failed even when resolving artifacts completely from bisection repository" );
            logger.info( "Build log is available in {}", getInitialBuildName() );
            System.exit( 1 );
        }

        while ( goodId - badId > 1 )
        {
            int tryId;
            if ( commandLineParser.useBinarySearch() )
                tryId = badId + ( goodId - badId ) / 2;
            else
                tryId = badId + 1;

            logger.info( "Bisection iteration: current range is [{},{}], trying {}", badId + 1, goodId - 1, tryId );
            setValue( tryId );

            success = executeBuild( getBuildLogName( tryId ) );
            logger.info( "Bisection build number {} {}", tryId, success ? "succeeded" : "failed" );

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

        logger.info( "Bisection build finished" );
        logger.info( "Failed build:     {}, see ", badId, badLog );
        logger.info( "Successful build: {}, see ", goodId, goodLog );
        logger.info( "Try:" );
        logger.info( "  $ git diff --no-index --color {} {}", badLog, goodLog );
    }

    public static void main( String[] args )
        throws Exception
    {
        try
        {
            BisectCliRequest cliRequest = new BisectCliRequest( args );

            Module module = new WireModule( new SpaceModule( new URLClassSpace( BisectCli.class.getClassLoader() ) ) );
            Injector injector = Guice.createInjector( module );
            BisectCli cli = injector.getInstance( BisectCli.class );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
