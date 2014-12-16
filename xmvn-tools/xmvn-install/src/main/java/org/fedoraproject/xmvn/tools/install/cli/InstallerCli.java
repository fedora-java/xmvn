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
package org.fedoraproject.xmvn.tools.install.cli;

import java.io.IOException;
import java.nio.file.Paths;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.Installer;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = InstallerCli.class )
public class InstallerCli
{
    private final Logger logger = LoggerFactory.getLogger( InstallerCli.class );

    @Requirement
    private Installer installer;

    private int run( InstallerCliRequest cliRequest )
    {
        InstallationRequest request = new InstallationRequest();
        request.setCheckForUnmatchedRules( !cliRequest.isRelaxed() );
        request.setBasePackageName( cliRequest.getPackageName() );
        request.setInstallRoot( Paths.get( cliRequest.getDestDir() ) );
        request.setInstallationPlan( Paths.get( cliRequest.getPlanPath() ) );

        try
        {
            installer.install( request );
            return 0;
        }
        catch ( ArtifactInstallationException | IOException e )
        {
            logger.error( "Artifact installation failed", e );
            return 1;
        }
    }

    public static void main( String[] args )
    {
        try
        {
            InstallerCliRequest cliRequest = new InstallerCliRequest( args );

            PlexusContainer container = new DefaultPlexusContainer();
            InstallerCli cli = container.lookup( InstallerCli.class );

            System.exit( cli.run( cliRequest ) );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
