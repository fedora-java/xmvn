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
package org.fedoraproject.maven.utils;

import org.codehaus.plexus.logging.Logger;

/**
 * @author Mikolaj Izdebski
 */
public class SimpleLogger
    implements Logger
{
    private final String me;

    private final String name;

    private volatile int threshold;

    public SimpleLogger( String me, String name, int threshold )
    {
        this.me = me;
        this.name = name;
        this.threshold = threshold;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getThreshold()
    {
        return threshold;
    }

    @Override
    public void setThreshold( int threshold )
    {
        this.threshold = threshold;
    }

    @Override
    public Logger getChildLogger( String message )
    {
        return this;
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return threshold <= LEVEL_FATAL;
    }

    @Override
    public boolean isErrorEnabled()
    {
        return threshold <= LEVEL_ERROR;
    }

    @Override
    public boolean isWarnEnabled()
    {
        return threshold <= LEVEL_WARN;
    }

    @Override
    public boolean isInfoEnabled()
    {
        return threshold <= LEVEL_INFO;
    }

    @Override
    public boolean isDebugEnabled()
    {
        return threshold <= LEVEL_DEBUG;
    }

    private void log( String message, Throwable exception )
    {
        if ( exception != null )
        {
            if ( isDebugEnabled() )
            {
                System.err.println( me + ": " + message );
                exception.printStackTrace( System.err );
            }
            else
            {
                System.err.println( me + ": " + message + ": " + exception );
            }
        }
        else
        {
            System.err.println( me + ": " + message );
        }
    }

    @Override
    public void fatalError( String message )
    {
        fatalError( message, null );
    }

    @Override
    public void fatalError( String message, Throwable exception )
    {
        if ( isFatalErrorEnabled() )
            log( message, exception );
    }

    @Override
    public void error( String message )
    {
        error( message, null );
    }

    @Override
    public void error( String message, Throwable exception )
    {
        if ( isErrorEnabled() )
            log( message, exception );
    }

    @Override
    public void warn( String message )
    {
        warn( message, null );
    }

    @Override
    public void warn( String message, Throwable exception )
    {
        if ( isWarnEnabled() )
            log( message, exception );
    }

    @Override
    public void info( String message )
    {
        info( message, null );
    }

    @Override
    public void info( String message, Throwable exception )
    {
        if ( isInfoEnabled() )
            log( message, exception );
    }

    @Override
    public void debug( String message )
    {
        debug( message, null );
    }

    @Override
    public void debug( String message, Throwable exception )
    {
        if ( isDebugEnabled() )
            log( message, exception );
    }
}
