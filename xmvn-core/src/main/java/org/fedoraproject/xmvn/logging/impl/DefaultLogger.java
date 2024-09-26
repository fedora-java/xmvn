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

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.logging.Logger;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultLogger
    implements Logger
{
    private final org.slf4j.Logger delegate;

    public DefaultLogger()
    {
        if ( System.getProperty( "xmvn.debug" ) != null )
        {
            System.setProperty( "org.slf4j.simpleLogger.log.XMvn", "trace" );
        }

        delegate = LoggerFactory.getLogger( "XMvn" );
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
