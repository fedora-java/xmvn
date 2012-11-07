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
import org.codehaus.plexus.classworlds.ClassWorld;
import org.fedoraproject.maven.resolver.SystemResolver;

public class Main
    extends MavenCli
{
    public static int main( String[] args, ClassWorld world )
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
        return cli.exec( args );
    }

    private int exec( String[] args )
    {
        int ret = doMain( args, null, null, null );
        SystemResolver.printInvolvedPackages();
        return ret;
    }

    @Override
    protected void customizeContainer( PlexusContainer container )
    {
        LoggerProvider.initialize( container );
    }
}
