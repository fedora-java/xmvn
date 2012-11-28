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

import java.io.File;

import org.fedoraproject.maven.resolver.DefaultResolver;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.resolver.SystemResolver;

public class ResolverCli
{
    public static void main( String[] args )
    {
        Resolver resolver = new DefaultResolver();
        String[] tokens = new String[4];

        for ( String s : args )
        {
            s += "::::";

            for ( int i = 0; i < tokens.length; i++ )
            {
                int j = s.indexOf( ':' );

                String tok = s.substring( 0, j ).trim();
                if ( tok.equals( "" ) )
                    tok = null;

                s = s.substring( j + 1 );
                tokens[i] = tok;
            }

            File file = resolver.resolve( tokens[0], tokens[1], tokens[2], tokens[3] );
            System.out.println( file );
        }

        SystemResolver.printInvolvedPackages();
    }
}
