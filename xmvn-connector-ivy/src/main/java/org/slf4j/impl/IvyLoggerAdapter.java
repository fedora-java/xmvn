/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
package org.slf4j.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.ivy.core.IvyContext;
import org.apache.ivy.util.MessageLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Mikolaj Izdebski
 */
class IvyLoggerAdapter
    extends MarkerIgnoringBase
{
    private static final long serialVersionUID = 1L;

    private final MessageLogger logger = IvyContext.getContext().getMessageLogger();

    private String formatMessage( String message, Throwable exception )
    {
        if ( exception == null )
            return message;

        try (StringWriter stringWriter = new StringWriter())
        {
            stringWriter.write( message );
            stringWriter.write( ": " );

            try (PrintWriter printWriter = new PrintWriter( stringWriter ))
            {
                exception.printStackTrace( printWriter );
            }

            return stringWriter.toString();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private String formatMessage( String format, Object arg1, Object arg2 )
    {
        FormattingTuple tuple = MessageFormatter.format( format, arg1, arg2 );
        return formatMessage( tuple.getMessage(), tuple.getThrowable() );
    }

    private String formatMessage( String format, Object... arguments )
    {
        FormattingTuple tuple = MessageFormatter.arrayFormat( format, arguments );
        return formatMessage( tuple.getMessage(), tuple.getThrowable() );
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public void error( String message )
    {
        logger.error( message );
    }

    @Override
    public void error( String message, Throwable exception )
    {
        logger.error( formatMessage( message, exception ) );
    }

    @Override
    public void error( String format, Object arg )
    {
        logger.error( formatMessage( format, arg, null ) );
    }

    @Override
    public void error( String format, Object arg1, Object arg2 )
    {
        logger.error( formatMessage( format, arg1, arg2 ) );
    }

    @Override
    public void error( String format, Object... args )
    {
        logger.error( formatMessage( format, args ) );
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void warn( String message )
    {
        logger.warn( message );
    }

    @Override
    public void warn( String message, Throwable exception )
    {
        logger.warn( formatMessage( message, exception ) );
    }

    @Override
    public void warn( String format, Object arg )
    {
        logger.warn( formatMessage( format, arg, null ) );
    }

    @Override
    public void warn( String format, Object arg1, Object arg2 )
    {
        logger.warn( formatMessage( format, arg1, arg2 ) );
    }

    @Override
    public void warn( String format, Object... args )
    {
        logger.warn( formatMessage( format, args ) );
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public void info( String message )
    {
        logger.info( message );
    }

    @Override
    public void info( String message, Throwable exception )
    {
        logger.info( formatMessage( message, exception ) );
    }

    @Override
    public void info( String format, Object arg )
    {
        logger.info( formatMessage( format, arg, null ) );
    }

    @Override
    public void info( String format, Object arg1, Object arg2 )
    {
        logger.info( formatMessage( format, arg1, arg2 ) );
    }

    @Override
    public void info( String format, Object... args )
    {
        logger.info( formatMessage( format, args ) );
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void debug( String message )
    {
        logger.verbose( message );
    }

    @Override
    public void debug( String format, Object param1 )
    {
        logger.verbose( formatMessage( format, param1, null ) );
    }

    @Override
    public void debug( String format, Object param1, Object param2 )
    {
        logger.verbose( formatMessage( format, param1, param2 ) );
    }

    @Override
    public void debug( String format, Object... args )
    {
        logger.verbose( formatMessage( format, args ) );
    }

    @Override
    public void debug( String message, Throwable exception )
    {
        logger.verbose( formatMessage( message, exception ) );
    }

    @Override
    public boolean isTraceEnabled()
    {
        return true;
    }

    @Override
    public void trace( String message )
    {
        logger.debug( message );
    }

    @Override
    public void trace( String message, Throwable exception )
    {
        logger.debug( formatMessage( message, exception ) );
    }

    @Override
    public void trace( String format, Object param1 )
    {
        logger.debug( formatMessage( format, param1, null ) );
    }

    @Override
    public void trace( String format, Object param1, Object param2 )
    {
        logger.debug( formatMessage( format, param1, param2 ) );
    }

    @Override
    public void trace( String format, Object... args )
    {
        logger.debug( formatMessage( format, args ) );
    }
}
