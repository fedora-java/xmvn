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
package org.fedoraproject.maven.connector.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

/**
 * Command that displays usage information about other commands.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Command.class, hint = "help" )
public class HelpCommand
    extends Command
{
    @Requirement
    private Logger logger;

    public HelpCommand()
    {
        super( "help" );
    }

    @Override
    public String getDescription()
    {
        return "display usage information about commands";
    }

    @Override
    public Options getOptions()
    {
        return new Options();
    }

    @Override
    public int execute( PlexusContainer container, CommandLine cli )
        throws Throwable
    {
        String[] args = cli.getArgs();

        if ( args.length != 1 )
        {
            logger.error( "The help command accepts exactly one argument - name a command to display usage information about." );
            return 1;
        }

        String commandName = args[0];
        Command command = Command.getByName( container, commandName );

        if ( command == null )
        {
            logger.error( "There is no command named \"" + commandName + "\"." );
            return 1;
        }

        String fullName = "xmvn " + command.getName();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( fullName, command.getOptions(), true );

        return 0;
    }

}
