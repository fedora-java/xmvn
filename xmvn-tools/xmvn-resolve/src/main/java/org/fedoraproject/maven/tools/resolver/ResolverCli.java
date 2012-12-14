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
import org.fedoraproject.maven.utils.StringSplitter;

public class ResolverCli
{
    public static void main( String[] args )
    {
        Resolver resolver = new DefaultResolver();

        for ( String s : args )
        {
            String[] tok = StringSplitter.split( s, 4, ':' );
            File file = resolver.resolve( tok[0], tok[1], tok[2], tok[3] );
            System.out.println( file );
        }

        SystemResolver.printInvolvedPackages();
    }
}
