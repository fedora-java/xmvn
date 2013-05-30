/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.tools.bisect;

import java.util.Map.Entry;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.utils.AtomicFileCounter;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = BisectCli.class )
public class BisectCli
{
    @Requirement
    private Logger logger;

    @Requirement
    private CommandLineParser commandLineParser;

    @Requirement
    private BuildExecutor buildExecutor;

    private static String getBuildLogName( int buildId )
    {
        return String.format( "bisect-build-%d.log", buildId );
    }

    private static String getInitialBuildName()
    {
        return "bisect-initial.log";
    }

    public void run( String[] args )
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
                logger.fatalError( "Standard local build was successfull, expected failure." );
                logger.info( "Build log is available in " + getBuildLogName( 0 ) );
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
            logger.fatalError( "Build failed even when resolving artifacts completely from bisection repository" );
            logger.info( "Build log is available in " + getInitialBuildName() );
            System.exit( 1 );
        }

        while ( goodId - badId > 1 )
        {
            int tryId;
            if ( commandLineParser.useBinarySearch() )
                tryId = badId + ( goodId - badId ) / 2;
            else
                tryId = badId + 1;

            logger.info( "Bisection iteration: current range is [" + ( badId + 1 ) + "," + ( goodId - 1 )
                + "], trying " + tryId );
            counter.setValue( tryId );

            success = buildExecutor.executeBuild( request, getBuildLogName( tryId ), commandLineParser.isVerbose() );
            logger.info( "Bisection build number " + tryId + " " + ( success ? "succeeded" : "failed" ) );

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
        logger.info( "Failed build:     " + badId + ", see " + badLog );
        logger.info( "Successful build: " + goodId + ", see " + goodLog );
        logger.info( "Try:" );
        logger.info( "  $ git diff --no-index --color " + badLog + " " + goodLog );
    }

    public static void main( String[] args )
    {
        try
        {
            DefaultPlexusContainer container = new DefaultPlexusContainer();
            LoggingUtils.configureContainerLogging( container, "xmvn-bisect", false );
            BisectCli cli = container.lookup( BisectCli.class );
            cli.run( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}
