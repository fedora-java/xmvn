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
package org.fedoraproject.rpmquery;

import static org.fedoraproject.maven.utils.FileUtils.followSymlink;
import static org.fedoraproject.maven.utils.Logger.error;
import static org.fedoraproject.maven.utils.Logger.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.fedoraproject.maven.utils.FileUtils;

public class RpmDb
{
    private static final Map<String, String> paths = new TreeMap<>();

    private static final Semaphore ready = new Semaphore( 0 );

    private static Iterable<String> execQuery( String query )
        throws IOException
    {
        String[] cmdLine = new String[] { "/bin/rpm", "-qa", "--qf", query };

        ProcessBuilder builder = new ProcessBuilder( cmdLine );
        builder.redirectError( FileUtils.BIT_BUCKET );
        Process child = builder.start();
        child.getOutputStream().close();

        Reader reader = new InputStreamReader( child.getInputStream() );
        BufferedReader bufferedReader = new BufferedReader( reader );

        Collection<String> lines = new ArrayList<>();
        String line;
        while ( ( line = bufferedReader.readLine() ) != null )
            lines.add( line );
        bufferedReader.close();

        int exitStatus;
        try
        {
            exitStatus = child.waitFor();
        }
        catch ( InterruptedException e )
        {
            error( "Interrupted while reaping rpm" );
            throw new IOException( e );
        }
        if ( exitStatus != 0 )
            throw new IOException( "rpm failed with exit status " + exitStatus );

        return lines;
    }

    private static void buildDatabase()
    {
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
            error( "Failed to build RPM database: ", e );
            info( "RPM database will be empty." );
            paths.put( "dummy", "dummy" );
        }
        finally
        {
            ready.release();
        }
    }

    static
    {
        Thread asyncInitializer = new Thread()
        {
            @Override
            public void run()
            {
                buildDatabase();
            }
        };
        asyncInitializer.setDaemon( true );
        asyncInitializer.start();
    }

    public String lookupFile( String path )
    {
        return lookupFile( new File( path ) );
    }

    public String lookupFile( File file )
    {
        file = followSymlink( file );
        ready.acquireUninterruptibly();
        ready.release();
        return paths.get( file.getPath() );
    }
}
