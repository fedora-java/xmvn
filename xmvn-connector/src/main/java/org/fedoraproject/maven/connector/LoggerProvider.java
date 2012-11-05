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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;

class LoggerProvider
    implements org.fedoraproject.maven.utils.Logger.Provider
{

    private final Logger plexusLogger;

    public LoggerProvider( PlexusContainer container )
        throws ComponentLookupException
    {
        plexusLogger = container.lookup( Logger.class );
    }

    @Override
    public void debug( String message )
    {
        plexusLogger.debug( message );
    }

    @Override
    public void info( String message )
    {
        plexusLogger.info( message );
    }

    @Override
    public void warn( String message )
    {
        plexusLogger.warn( message );
    }

    @Override
    public void error( String message )
    {
        plexusLogger.error( message );
    }

    public static void initialize( PlexusContainer container )
    {
        try
        {
            LoggerProvider provider = new LoggerProvider( container );
            org.fedoraproject.maven.utils.Logger.setProvider( provider );
        }
        catch ( ComponentLookupException e )
        {
        }
    }
}
