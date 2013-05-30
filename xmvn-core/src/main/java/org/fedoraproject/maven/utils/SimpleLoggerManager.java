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

import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

/**
 * @author Mikolaj Izdebski
 */
public class SimpleLoggerManager
    implements LoggerManager
{
    private class ComponentKey
        implements Comparable<ComponentKey>
    {
        private final String role;

        private final String hint;

        public ComponentKey( String role, String hint )
        {
            this.role = role;
            this.hint = hint;
        }

        @Override
        public int compareTo( ComponentKey that )
        {
            if ( role.equals( that.role ) )
                return role.compareTo( that.role );
            return hint.compareTo( that.hint );
        }
    }

    private final String me;

    private int threshold;

    private final Map<ComponentKey, Logger> componentLoggers = new TreeMap<>();

    public SimpleLoggerManager( String executable, int threshold )
    {
        this.me = executable;
        this.threshold = threshold;
    }

    @Override
    public synchronized int getActiveLoggerCount()
    {
        return componentLoggers.size();
    }

    @Override
    public Logger getLoggerForComponent( String role )
    {
        return getLoggerForComponent( role, "" );
    }

    @Override
    public synchronized Logger getLoggerForComponent( String role, String hint )
    {
        ComponentKey key = new ComponentKey( role, hint );
        Logger logger = componentLoggers.get( key );

        if ( logger == null )
        {
            String loggerName = SimpleLogger.class.getName() + " for component " + role;
            if ( !hint.isEmpty() )
                loggerName += " [role hint = " + hint + "]";

            logger = new SimpleLogger( me, loggerName, threshold );
            componentLoggers.put( key, logger );
        }

        return logger;
    }

    @Override
    public synchronized int getThreshold()
    {
        return threshold;
    }

    @Override
    public void returnComponentLogger( String role )
    {
        returnComponentLogger( role, "" );
    }

    @Override
    public synchronized void returnComponentLogger( String role, String hint )
    {
        ComponentKey key = new ComponentKey( role, hint );
        componentLoggers.remove( key );
    }

    @Override
    public synchronized void setThreshold( int threshold )
    {
        this.threshold = threshold;
    }

    @Override
    public synchronized void setThresholds( int threshold )
    {
        this.threshold = threshold;

        for ( Logger logger : componentLoggers.values() )
            logger.setThreshold( threshold );
    }

    public void setThreshold( String role, int threshold )
    {
        Logger logger = getLoggerForComponent( role );
        logger.setThreshold( threshold );
    }

    public void setThreshold( String role, String hint, int threshold )
    {
        Logger logger = getLoggerForComponent( role, hint );
        logger.setThreshold( threshold );
    }

    public int getThreshold( String role )
    {
        Logger logger = getLoggerForComponent( role );
        return logger.getThreshold();
    }

    public int getThreshold( String role, String hint )
    {
        Logger logger = getLoggerForComponent( role, hint );
        return logger.getThreshold();
    }
}
