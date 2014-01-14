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
package org.fedoraproject.xmvn.connector.ivy;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.ivy.util.Message;
import org.codehaus.plexus.logging.Logger;

/**
 * Implementation of Plexus logger that redirects all logging to Ivy logger.
 * 
 * @author Mikolaj Izdebski
 */
class IvyLogger
    implements Logger
{
    private final String name;

    public IvyLogger( String name )
    {
        this.name = name;
    }

    private static String getExceptionTrace( Throwable exception )
    {
        try (StringWriter stringWriter = new StringWriter())
        {
            try (PrintWriter printWriter = new PrintWriter( stringWriter ))
            {
                exception.printStackTrace( printWriter );
            }

            return stringWriter.toString();
        }
        catch ( IOException exc )
        {
            return null;
        }
    }

    private static String formatMessage( String message, Throwable exception )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "[XMvn] " );
        sb.append( message );

        if ( exception != null )
        {
            sb.append( ": " );
            sb.append( getExceptionTrace( exception ) );
        }

        return sb.toString();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getThreshold()
    {
        return LEVEL_DEBUG;
    }

    @Override
    public void setThreshold( int threshold )
    {
        // Ignored
    }

    @Override
    public Logger getChildLogger( String message )
    {
        return this;
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return true;
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void fatalError( String message )
    {
        fatalError( message, null );
    }

    @Override
    public void fatalError( String message, Throwable exception )
    {
        fatalError( formatMessage( message, exception ) );
    }

    @Override
    public void error( String message )
    {
        error( message, null );
    }

    @Override
    public void error( String message, Throwable exception )
    {
        Message.error( formatMessage( message, exception ) );
    }

    @Override
    public void warn( String message )
    {
        warn( message, null );
    }

    @Override
    public void warn( String message, Throwable exception )
    {
        Message.warn( formatMessage( message, exception ) );
    }

    @Override
    public void info( String message )
    {
        info( message, null );
    }

    @Override
    public void info( String message, Throwable exception )
    {
        Message.info( formatMessage( message, exception ) );
    }

    @Override
    public void debug( String message )
    {
        debug( message, null );
    }

    @Override
    public void debug( String message, Throwable exception )
    {
        Message.verbose( formatMessage( message, exception ) );
    }
}
