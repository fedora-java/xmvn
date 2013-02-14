/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fedoraproject.maven.config.ArtifactBlacklistXXX;
import org.fedoraproject.maven.resolver.DefaultResolver;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.resolver.SystemResolver;
import org.fedoraproject.maven.utils.StringSplitter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class ResolverCli
{
    @Parameter
    public List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    public boolean debug = false;

    @Parameter( names = { "-c", "--classpath" }, description = "Use colon instead of new line to separate resolved artifacts" )
    public boolean classpath = false;

    @DynamicParameter( names = "-D", description = "Define system property" )
    public Map<String, String> defines = new TreeMap<>();

    public ResolverCli( String[] args )
    {
        try
        {
            JCommander jcomm = new JCommander( this, args );
            jcomm.setProgramName( "xmvn-resolve" );

            if ( help )
            {
                System.out.println( "xmvn-resolve: Resolve artifacts from system repository" );
                System.out.println();
                jcomm.usage();
                System.exit( 0 );
            }

            for ( String param : defines.keySet() )
                System.setProperty( param, defines.get( param ) );
            if ( debug )
                System.setProperty( "xmvn.debug", "true" );
        }
        catch ( ParameterException e )
        {
            System.err.println( e.getMessage() + ". Specify -h for usage." );
            System.exit( 1 );
        }
    }

    private void printResult( List<File> result )
    {
        if ( classpath )
        {
            Iterator<File> it = result.iterator();
            System.out.print( it.next() );
            while ( it.hasNext() )
            {
                System.out.print( ':' );
                System.out.print( it.next() );
            }
            System.out.println();
        }
        else
        {
            for ( File f : result )
                System.out.println( f );
        }
    }

    public void run()
    {
        Resolver resolver = new DefaultResolver();
        List<File> result = new ArrayList<>();

        for ( String s : parameters )
        {
            String[] tok = StringSplitter.split( s, 4, ':' );
            File file = resolver.resolve( tok[0], tok[1], tok[2], tok[3] );
            result.add( file );
        }

        if ( !result.isEmpty() )
            printResult( result );

        SystemResolver.printInvolvedPackages();
        // Load ArtifactBlacklist class so it can print useful debugging information during its static initialization
        ArtifactBlacklistXXX.class.getClass();
    }

    public static void main( String[] args )
    {
        ResolverCli cli = new ResolverCli( args );
        cli.run();
    }
}
