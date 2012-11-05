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
package org.fedoraproject.maven.utils;

import java.io.PrintStream;

import org.fedoraproject.maven.Configuration;

public class Logger
{
    public static interface Provider
    {
        void debug( String message );

        void info( String message );

        void warn( String message );

        void error( String message );
    }

    static class DefaultProvider
        implements Provider
    {

        private static final PrintStream stream = System.out;

        @Override
        public void debug( String message )
        {
            if ( Configuration.LOGGER_VERBOSITY >= 3 )
                stream.println( "[DEBUG] " + message );
        }

        @Override
        public void info( String message )
        {
            if ( Configuration.LOGGER_VERBOSITY >= 2 )
                stream.println( "[INFO] " + message );
        }

        @Override
        public void warn( String message )
        {
            if ( Configuration.LOGGER_VERBOSITY >= 1 )
                stream.println( "[WARN] " + message );
        }

        @Override
        public void error( String message )
        {
            if ( Configuration.LOGGER_VERBOSITY >= 0 )
                stream.println( "[ERROR] " + message );
        }

    }

    private static volatile Provider provider = new DefaultProvider();

    public static void setProvider( Provider provider )
    {
        Logger.provider = provider;
    }

    public static void debug( Object... message )
    {
        provider.debug( concatenate( message ) );
    }

    public static void info( Object... message )
    {
        provider.info( concatenate( message ) );
    }

    public static void warn( Object... message )
    {
        provider.warn( concatenate( message ) );
    }

    public static void error( Object... message )
    {
        provider.error( concatenate( message ) );
    }

    private static String concatenate( Object... list )
    {
        StringBuilder builder = new StringBuilder();

        for ( Object item : list )
            builder.append( item.toString() );

        return builder.toString();
    }
}
