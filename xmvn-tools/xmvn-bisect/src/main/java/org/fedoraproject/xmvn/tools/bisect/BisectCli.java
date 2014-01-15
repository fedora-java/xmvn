/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.fedoraproject.xmvn.utils.AtomicFileCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author Mikolaj Izdebski
 */
@Named
public class BisectCli
{
    private final Logger logger = LoggerFactory.getLogger( BisectCli.class );

    private static String[] args;

    private final CommandLineParser commandLineParser;

    private final BuildExecutor buildExecutor;

    @Inject
    public BisectCli( CommandLineParser commandLineParser, BuildExecutor buildExecutor )
        throws Exception
    {
        this.commandLineParser = commandLineParser;
        this.buildExecutor = buildExecutor;
    }

    private static String getBuildLogName( int buildId )
    {
        return String.format( "bisect-build-%d.log", buildId );
    }

    private static String getInitialBuildName()
    {
        return "bisect-initial.log";
    }

    private void run()
        throws Exception
    {
        commandLineParser.parseCommandLine( args );
        boolean verbose = commandLineParser.isVerbose();

        InvocationRequest request = commandLineParser.createInvocationRequest();
        request.setShellEnvironmentInherited( true );

        for ( Entry<String, String> entry : commandLineParser.getSystemProperties().entrySet() )
            System.setProperty( entry.getKey(), entry.getValue() );

        request.addShellEnvironment( "M2_HOME", commandLineParser.getSystemProperties().get( "maven.home" ) );

        request.getProperties().put( "xmvn.bisect.repository", commandLineParser.getRepoPath() );
        request.getProperties().put( "xmvn.bisect.counter", commandLineParser.getCounterPath() );

        int counterInitialValue = 1000000000;
        AtomicFileCounter counter = new AtomicFileCounter( commandLineParser.getCounterPath(), 0 );

        if ( !commandLineParser.isSkipSanityChecks() )
        {
            logger.info( "Checking if standard local build really fails" );
            boolean success = buildExecutor.executeBuild( request, getBuildLogName( 0 ), verbose );
            if ( success )
            {
                logger.error( "Standard local build was successfull, expected failure." );
                logger.info( "Build log is available in {}", getBuildLogName( 0 ) );
                System.exit( 1 );
            }
        }

        int badId = 0;
        logger.info( "Running initial upstream build" );
        counter.setValue( counterInitialValue );
        boolean success = buildExecutor.executeBuild( request, getInitialBuildName(), verbose );
        int goodId = counterInitialValue - counter.getValue();
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
            counter.setValue( tryId );

            success = buildExecutor.executeBuild( request, getBuildLogName( tryId ), commandLineParser.isVerbose() );
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
            Module module = new WireModule( new SpaceModule( new URLClassSpace( BisectCli.class.getClassLoader() ) ) );
            Injector injector = Guice.createInjector( module );
            BisectCli cli = injector.getInstance( BisectCli.class );
            cli.run();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}
