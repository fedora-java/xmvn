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

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.connector.cli.Command;

@Component( role = Main.class )
public class Main
{
    @Requirement
    private Logger logger;

    @Requirement
    private LoggerProvider loggerProvider;

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
        org.fedoraproject.maven.utils.Logger.setProvider( loggerProvider );
        System.setProperty( "maven.version", Configuration.getMavenVersion() );
        System.setProperty( "maven.build.version", Configuration.getMavenVersion() );

        try
        {
            String commandName = args[0];
            Command command = container.lookup( Command.class, commandName );
            return command.execute( container );
        }
        catch ( Throwable e )
        {
            throw new RuntimeException( e );
        }
    }
}
