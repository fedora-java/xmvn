/*-
 * Copyright (c) 2016-2021 Red Hat, Inc.
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

import java.lang.reflect.Method;

import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 * @author Roman Vais
 */
class Slf4jLoggerAdapter
    implements Logger
{
    private final org.slf4j.Logger delegate;

    public Slf4jLoggerAdapter()
    {
        boolean debug = System.getProperty( "xmvn.debug" ) != null;

        if ( debug )
        {
            // Try enabling debug output slf4j-simple logger (used in Maven 3.1.x through 3.3.x)
            System.setProperty( "org.slf4j.simpleLogger.log.XMvn", "trace" );
        }

        delegate = LoggerFactory.getLogger( "XMvn" );

        try
        {
            // Try enabling debug output for Gossip logger (Maven 3.4.0+)
            Class<?> gossipLogClass = Class.forName( "com.planet57.gossip.Gossip$LoggerImpl" );
            boolean isGossipLogger = gossipLogClass.isAssignableFrom( delegate.getClass() );

            try
            {
                if ( isGossipLogger && debug )
                {
                    Class<?> levelClass = Class.forName( "com.planet57.gossip.Level" );
                    Method setLogLevel = delegate.getClass().getMethod( "setLevel", levelClass );

                    Object lvl = levelClass.getField( "ALL" ).get( levelClass );

                    setLogLevel.invoke( delegate, lvl );
                }
            }
            catch ( ReflectiveOperationException e )
            {
                delegate.error( "Unable to set logging level for Gossip logger implementation.", e );
            }
        }
        catch ( ClassNotFoundException ex )
        {
            // Gossip is not in use
        }
    }

    @Override
    public boolean isDebugEnabled()
    {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug( String format, Object... args )
    {
        delegate.debug( format, args );
    }

    @Override
    public void info( String format, Object... args )
    {
        delegate.info( format, args );
    }

    @Override
    public void warn( String format, Object... args )
    {
        delegate.warn( format, args );
    }

    @Override
    public void error( String format, Object... args )
    {
        delegate.error( format, args );
    }
}
