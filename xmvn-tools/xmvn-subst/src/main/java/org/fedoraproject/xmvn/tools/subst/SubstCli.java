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
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = SubstCli.class )
public class SubstCli
{
    private final Logger logger = LoggerFactory.getLogger( SubstCli.class );

    @Requirement
    private ArtifactVisitor visitor;

    private void run( SubstCliRequest cliRequest )
    {
        visitor.setTypes( cliRequest.getTypes() );
        visitor.setFollowSymlinks( cliRequest.isFollowSymlinks() );
        visitor.setDryRun( cliRequest.isDryRun() );

        try
        {
            for ( String path : cliRequest.getParameters() )
            {
                Files.walkFileTree( Paths.get( path ), visitor );
            }
        }
        catch ( IOException e )
        {
            logger.error( "I/O error occured", e );
        }

        if ( cliRequest.isStrict() && visitor.getFailureCount() > 0 )
            System.exit( 1 );
    }

    public static void main( String[] args )
    {
        try
        {
            SubstCliRequest cliRequest = new SubstCliRequest( args );

            PlexusContainer container = new DefaultPlexusContainer();
            SubstCli cli = container.lookup( SubstCli.class );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
