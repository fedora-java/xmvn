/*-
 * Copyright (c) 2013-2016 Red Hat, Inc.
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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.locator.XMvnHomeClassLoader;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.Installer;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class InstallerCli
{
    private final Logger logger = LoggerFactory.getLogger( InstallerCli.class );

    private final Installer installer;

    @Inject
    public InstallerCli( Installer installer )
    {
        this.installer = installer;
    }

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

            ClassLoader installerClassLoader = InstallerCli.class.getClassLoader();
            XMvnHomeClassLoader xmvnClassLoader = new XMvnHomeClassLoader( installerClassLoader );
            Module module = new WireModule( new SpaceModule( new URLClassSpace( installerClassLoader ) ),
                                            new SpaceModule( new URLClassSpace( xmvnClassLoader ) ) );
            Injector injector = Guice.createInjector( module );
            InstallerCli cli = injector.getInstance( InstallerCli.class );

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
