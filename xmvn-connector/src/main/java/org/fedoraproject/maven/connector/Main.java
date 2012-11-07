/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.connector.cli.Command;

@Component( role = Main.class )
public class Main
{
    @Requirement
    private Logger logger;

    public static int main( String[] args, ClassWorld world )
        throws PlexusContainerException, ComponentLookupException
    {
        DefaultPlexusContainer container = null;

        try
        {
            System.out.println( "[INFO] Initializing Plexus..." );
            container = new DefaultPlexusContainer();
            container.getLoggerManager().setThreshold( Logger.LEVEL_DEBUG );
            return container.lookup( Main.class ).exec( world, container, args );
        }
        finally
        {
            container.dispose();
        }
    }

    private int exec( ClassWorld world, PlexusContainer container, String[] args )
    {
        // Ugly, aint it?
        System.setProperty( "maven.version", "3.0.4" );
        System.setProperty( "maven.build.version", "3.0.4" );

        try
        {
            if ( args.length == 0 )
            {
                logger.error( "No command given. Specify --help for usage information." );
                return 1;
            }

            String commandName = args[0];
            String[] commandArgs = new String[args.length - 1];
            System.arraycopy( args, 1, commandArgs, 0, commandArgs.length );

            if ( commandName.equals( "--version" ) )
            {
                logger.info( "XMvn version 0" );
                logger.info( "Written by Mikolaj Izdebski <mizdebsk@redhat.com>" );
                return 0;
            }

            List<Command> commands = Command.getAvailableCommands( container );

            if ( commandName.equals( "--help" ) )
            {
                logger.info( "Usage: xmvn <command> [args]" );
                logger.info( "" );

                logger.info( "Available commands are:" );
                for ( Command command : commands )
                    logger.info( "  * " + command.getName() + " - " + command.getDescription() );

                logger.info( "" );
                logger.info( "For help about particular commands, run \"xmvn help <command>\"." );
                return 0;
            }

            if ( commandArgs.length == 1 && commandArgs[0].equals( "--help" ) )
            {
                commandArgs[0] = commandName;
                commandName = "help";
            }

            Command command = Command.getByName( container, commandName );
            if ( command == null )
            {
                logger.error( "\"" + commandName + "\" is not a valid command. Specify --help for usage information." );
                return 1;
            }

            CommandLine cli;
            try
            {
                CommandLineParser cliParser = new GnuParser();
                cli = cliParser.parse( command.getOptions(), commandArgs );
            }
            catch ( ParseException e )
            {
                logger.error( "Failed to parse options for command \"" + commandName + "\": " + e.getMessage() );
                return 1;
            }

            logger.info( "Executing command \"" + commandName + "\"..." );
            return command.execute( container, cli );
        }
        catch ( Throwable e )
        {
            if ( e instanceof LifecycleExecutionException )
                logger.fatalError( e.getMessage() );
            else
                logger.fatalError( "Internal XMvn error", e );

            return 1;
        }
    }
}
