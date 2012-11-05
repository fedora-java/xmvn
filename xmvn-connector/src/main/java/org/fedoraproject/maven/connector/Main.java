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

import static org.fedoraproject.maven.utils.Logger.info;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.validation.ModelValidator;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.codehaus.plexus.PlexusContainer;
import org.fedoraproject.maven.resolver.SystemResolver;
import org.sonatype.aether.repository.WorkspaceReader;

public class Main
{
    private final MavenCli cli;

    private Main()
    {
        cli = new MavenCli()
        {
            @Override
            protected void customizeContainer( PlexusContainer container )
            {
                extendContainer( container );
            }
        };
    }

    public static void main( String[] args )
    {
        info( "Maven RPM extension" );
        info( "Written by Mikolaj Izdebski <mizdebsk@redhat.com>" );

        Main cli = new Main();
        cli.exec( args );
    }

    private void exec( String[] args )
    {
        int ret = cli.doMain( args, null, null, null );
        SystemResolver.printInvolvedPackages();
        System.exit( ret );
    }

    private void extendContainer( PlexusContainer container )
    {
        LoggerProvider.initialize( container );

        container.addComponent( new FedoraWorkspaceReader(), WorkspaceReader.class, "ide" );
        container.addComponent( new FedoraPluginVersionResolver(), PluginVersionResolver.class, "default" );
        container.addComponent( new FedoraModelValidator(), ModelValidator.class, "default" );
    }
}
