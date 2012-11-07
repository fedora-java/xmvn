/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.resolver.SystemResolver;

@Component( role = Main.class )
public class Main
{
    @Requirement
    private Logger logger;

    public static int main( String[] args, ClassWorld world )
        throws PlexusContainerException, ComponentLookupException
    {
        DefaultPlexusContainer container = null;

        try
        {
            System.out.println( "[INFO] Initializing Plexus..." );
            container = new DefaultPlexusContainer();
            container.getLoggerManager().setThreshold( Logger.LEVEL_DEBUG );
            return container.lookup( Main.class ).exec( world, container, args );
        }
        finally
        {
            container.dispose();
        }
    }

    private int exec( ClassWorld world, PlexusContainer container, String[] args )
    {
        try
        {
            // Ugly, aint it?
            System.setProperty( "maven.version", "3.0.4" );
            System.setProperty( "maven.build.version", "3.0.4" );
            System.setProperty( "maven.test.skip", "true" );

            logger.info( "Running XMvn..." );

            MavenExecutor executor = new MavenExecutor();
            logger.info( "Building project..." );
            executor.execute( "verify", "org.fedoraproject.xmvn:rpminstall-maven-plugin:install" );
            logger.info( "Generating javadocs..." );
            executor.execute( "org.apache.maven.plugins:maven-javadoc-plugin:aggregate" );
            logger.info( "Build finished SUCCESSFULLY" );

            SystemResolver.printInvolvedPackages();
            return 0;
        }
        catch ( Throwable e )
        {
            logger.fatalError( e.getMessage() );
            return 1;
        }
    }
}
