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

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public abstract class Command
{
    private final String name;

    public Command( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public abstract String getDescription();

    public abstract Options getOptions();

    public abstract int execute( PlexusContainer container, CommandLine cli )
        throws Throwable;

    public static List<Command> getAvailableCommands( PlexusContainer container )
    {
        try
        {
            return container.lookupList( Command.class );
        }
        catch ( ComponentLookupException e )
        {
            return Collections.emptyList();
        }
    }

    public static Command getByName( PlexusContainer container, String commandName )
    {
        for ( Command command : getAvailableCommands( container ) )
        {
            if ( command.getName().equals( commandName ) )
            {
                return command;
            }
        }

        return null;
    }
}
