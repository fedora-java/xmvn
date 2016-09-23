/*-
 * Copyright (c) 2016 Red Hat, Inc.
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Logger.class )
public class DefaultLogger
    implements Logger, Initializable
{
    @Requirement
    private org.codehaus.plexus.logging.Logger plexusLogger;

    private Logger delegate;

    @Override
    public void initialize()
        throws InitializationException
    {
        try
        {
            // First try to use SLF4J logging (available in Maven 3.1.0+)
            delegate = new Slf4jLoggerAdapter();
        }
        catch ( LinkageError e )
        {
            // Fall back to Plexus logger
            delegate = new PlexusLoggerAdapter( plexusLogger );
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
