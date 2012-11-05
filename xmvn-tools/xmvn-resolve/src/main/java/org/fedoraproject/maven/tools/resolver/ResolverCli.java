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
package org.fedoraproject.maven.tools.resolver;

import static org.fedoraproject.maven.utils.Logger.error;
import static org.fedoraproject.maven.utils.Logger.info;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.resolver.DefaultResolver;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.resolver.SystemResolver;

public class ResolverCli
{

    private static void usage()
    {
        String me = "maven-rpm-resolver";
        info( me, " - resolve Maven artifacts installed in the system" );
        info();
        info( "Usage: ", me, " [-X] [-q] groupId:artifactId:[version:[extension]] [...]" );
        info();
        info( "    -X, --debug   Verbose output, display debugging information" );
        info( "    -q, --quiet   Supress output, display only errors" );
        info();
        info( "Written by Mikolaj Izdebski <mizdebsk@redhat.com>" );
    }

    public static void main( String[] argArray )
    {
        List<String> args = new LinkedList<>( Arrays.asList( argArray ) );

        Configuration.LOGGER_VERBOSITY = 2;
        for ( Iterator<String> iter = args.iterator(); iter.hasNext(); )
        {
            String arg = iter.next();

            if ( arg.equals( "--help" ) || arg.equals( "-h" ) )
            {
                usage();
                System.exit( 0 );
            }

            if ( arg.equals( "--debug" ) || arg.equals( "-X" ) )
            {
                Configuration.LOGGER_VERBOSITY = 3;
                iter.remove();
            }

            if ( arg.equals( "--quiet" ) || arg.equals( "-q" ) )
            {
                Configuration.LOGGER_VERBOSITY = 0;
                iter.remove();
            }
        }

        if ( args.isEmpty() )
        {
            error( "Missing argument. Specify --help for usage." );
            System.exit( 1 );
        }

        Resolver resolver = new DefaultResolver();

        for ( String arg : args )
        {
            StringTokenizer tok = new StringTokenizer( arg, ":" );
            String groupId = tok.nextToken();
            String artifactId = tok.nextToken();
            String version = tok.hasMoreTokens() ? tok.nextToken() : "SYSTEM";
            String extension = tok.hasMoreTokens() ? tok.nextToken() : "pom";

            resolver.resolve( groupId, artifactId, version, extension );
        }

        SystemResolver.printInvolvedPackages();
    }
}
