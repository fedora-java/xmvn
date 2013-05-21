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
package org.fedoraproject.maven.util;

import org.codehaus.plexus.logging.Logger;

/**
 * A Plexus logger implementation that redirects all messaged to the bit bucket.
 * 
 * @author Mikolaj Izdebski
 */
public class BitBucketLogger
    implements Logger
{
    private int threshold;

    @Override
    public void debug( String string )
    {
        // This implementation doesn't log anything
    }

    @Override
    public void debug( String string, Throwable throwable )
    {
        // This implementation doesn't log anything
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void info( String string )
    {
        // This implementation doesn't log anything
    }

    @Override
    public void info( String string, Throwable throwable )
    {
        // This implementation doesn't log anything
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public void warn( String string )
    {
        // This implementation doesn't log anything
    }

    @Override
    public void warn( String string, Throwable throwable )
    {
        // This implementation doesn't log anything
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void error( String string )
    {
        // This implementation doesn't log anything
    }

    @Override
    public void error( String string, Throwable throwable )
    {
        // This implementation doesn't log anything
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public void fatalError( String string )
    {
        // This implementation doesn't log anything
    }

    @Override
    public void fatalError( String string, Throwable throwable )
    {
        // This implementation doesn't log anything
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return true;
    }

    @Override
    public Logger getChildLogger( String string )
    {
        return null;
    }

    @Override
    public String getName()
    {
        return BitBucketLogger.class.getName();
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
}
