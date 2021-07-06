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

/**
 * @author Mikolaj Izdebski
 */
class PlexusLoggerAdapter
    implements Logger
{
    private final org.codehaus.plexus.logging.Logger delegate;

    public PlexusLoggerAdapter( org.codehaus.plexus.logging.Logger delegate )
    {
        this.delegate = delegate;

        if ( System.getProperty( "xmvn.debug" ) != null )
            delegate.setThreshold( org.codehaus.plexus.logging.Logger.LEVEL_DEBUG );
    }

    private String format( String format, Object... args )
    {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for ( Object arg : args )
        {
            int j = format.indexOf( "{}", i );
            sb.append( format.substring( i, j ) );
            sb.append( arg );
            i = j + 2;
        }

        sb.append( format.substring( i ) );

        return sb.toString();
    }

    @Override
    public boolean isDebugEnabled()
    {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug( String format, Object... args )
    {
        delegate.debug( format( format, args ) );
    }

    @Override
    public void info( String format, Object... args )
    {
        delegate.info( format( format, args ) );
    }

    @Override
    public void warn( String format, Object... args )
    {
        delegate.warn( format( format, args ) );
    }

    @Override
    public void error( String format, Object... args )
    {
        delegate.error( format( format, args ) );
    }
}
