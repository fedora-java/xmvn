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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.PlexusContainer;
import org.fedoraproject.maven.resolver.SystemResolver;

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
                LoggerProvider.initialize( container );
            }
        };
    }

    public static void main( String[] args )
    {
        info( "Maven RPM extension" );
        info( "Written by Mikolaj Izdebski <mizdebsk@redhat.com>" );

        List<String> options = new LinkedList<>();
        options.add( "--offline" );
        options.add( "--batch-mode" );
        options.add( "-Dmaven.repo.local=.xm2" );
        options.addAll( Arrays.asList( args ) );

        Main cli = new Main();
        args = options.toArray( new String[0] );
        cli.exec( args );
    }

    private void exec( String[] args )
    {
        int ret = cli.doMain( args, null, null, null );
        SystemResolver.printInvolvedPackages();
        System.exit( ret );
    }
}
