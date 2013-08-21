/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.fedoraproject.maven.config.Configurator;

/**
 * @author Mikolaj Izdebski
 */
public class LoggingUtils
{
    public static void setLoggerThreshold( Logger logger, Boolean debugSetting )
    {
        int threshold = debugSetting != null && debugSetting ? Logger.LEVEL_DEBUG : Logger.LEVEL_WARN;
        logger.setThreshold( Math.min( logger.getThreshold(), threshold ) );
    }

    public static void configureContainerLogging( DefaultPlexusContainer container, String executable,
                                                  boolean enableDebug )
    {
        int threshold = enableDebug ? Logger.LEVEL_DEBUG : Logger.LEVEL_INFO;
        LoggerManager manager = new SimpleLoggerManager( executable, threshold );
        container.setLoggerManager( manager );

        try
        {
            Configurator configurator = container.lookup( Configurator.class );
            configurator.getConfiguration().getResolverSettings().setDebug( enableDebug );
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }
    }
}
