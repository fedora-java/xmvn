/*-
 * Copyright (c) 2016-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.logging.impl;

import org.fedoraproject.xmvn.logging.Logger;

/**
 * @author Mikolaj Izdebski
 */
public class ConsoleLogger
    implements Logger
{
    private final boolean debugEnabled = System.getProperty( "xmvn.debug" ) != null;

    @Override
    public boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    private void print( String prefix, String format, Object... args )
    {
        StringBuilder sb = new StringBuilder( prefix );

        int i = 0;
        for ( Object arg : args )
        {
            int j = format.indexOf( "{}", i );
            sb.append( format.substring( i, j ) );
            sb.append( arg );
            i = j + 2;
        }

        sb.append( format.substring( i ) );

        System.err.println( sb );
    }

    @Override
    public void debug( String format, Object... args )
    {
        if ( debugEnabled )
        {
            print( "DEBUG: ", format, args );
        }
    }

    @Override
    public void info( String format, Object... args )
    {
        print( "", format, args );
    }

    @Override
    public void warn( String format, Object... args )
    {
        print( "WARNING: ", format, args );
    }

    @Override
    public void error( String format, Object... args )
    {
        print( "ERROR: ", format, args );
    }
}
