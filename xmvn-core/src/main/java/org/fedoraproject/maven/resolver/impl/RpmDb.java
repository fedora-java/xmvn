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
package org.fedoraproject.maven.resolver.impl;

import static org.fedoraproject.maven.utils.FileUtils.followSymlink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.fedoraproject.maven.utils.FileUtils;

/**
 * @author Mikolaj Izdebski
 */
class RpmDb
{
    private static Map<String, String> paths;

    private static final Object lock = new Object();

    private static Iterable<String> execQuery( String query )
        throws IOException
    {
        String[] cmdLine = new String[] { "/bin/rpm", "-qa", "--qf", query };

        ProcessBuilder builder = new ProcessBuilder( cmdLine );
        builder.redirectError( FileUtils.BIT_BUCKET );
        Process child = builder.start();
        child.getOutputStream().close();

        Reader reader = new InputStreamReader( child.getInputStream() );
        Collection<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader( reader ))
        {
            String line;
            while ( ( line = bufferedReader.readLine() ) != null )
                lines.add( line );
        }

        int exitStatus;
        try
        {
            exitStatus = child.waitFor();
        }
        catch ( InterruptedException e )
        {
            throw new IOException( e );
        }
        if ( exitStatus != 0 )
            throw new IOException( "rpm failed with exit status " + exitStatus );

        return lines;
    }

    private static void buildDatabase()
    {
        paths = new TreeMap<>();

        try
        {
            String query = "[%{NAME}|%{FILENAMES}\n]";
            Iterable<String> rows;
            rows = execQuery( query );

            for ( String row : rows )
            {
                int splitPoint = row.indexOf( '|' );
                String name = row.substring( 0, splitPoint );
                String path = row.substring( splitPoint + 1 );
                paths.put( path, name );
            }
        }
        catch ( IOException e )
        {
        }
    }

    public String lookupFile( String path )
    {
        return lookupFile( new File( path ) );
    }

    public String lookupFile( File file )
    {
        file = followSymlink( file );

        synchronized ( lock )
        {
            if ( paths == null )
                buildDatabase();
            return paths.get( file.getPath() );
        }
    }
}
