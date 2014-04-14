/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractFileTest
{
    private final List<File> files = new ArrayList<>();

    private Path installRoot;

    protected void add( File file )
        throws Exception
    {
        files.add( file );
    }

    protected Path performInstallation()
        throws Exception
    {
        try
        {
            String testName = getClass().getName();
            Path workPath = Paths.get( "target" ).resolve( "test-work" );
            Files.createDirectories( workPath );
            installRoot = Files.createTempDirectory( workPath, testName );

            for ( File file : files )
                file.install( installRoot );

            return installRoot;
        }
        finally
        {
            files.clear();
        }
    }

    protected void assertDirectoryStructure( String... expected )
        throws Exception
    {
        assertDirectoryStructure( installRoot, expected );
    }

    protected void assertDirectoryStructure( Path root, String... expected )
        throws Exception
    {
        List<String> expectedList = new ArrayList<>();
        for ( String string : expected )
            expectedList.add( string );

        List<String> actualList = new ArrayList<>();
        Files.walkFileTree( root, new FileSystemWalker( root, actualList ) );

        Collections.sort( expectedList );
        Collections.sort( actualList );

        try
        {
            Iterator<String> expectedIterator = expectedList.iterator();
            Iterator<String> actualIterator = actualList.iterator();

            while ( expectedIterator.hasNext() && actualIterator.hasNext() )
                assertEquals( expectedIterator.next(), actualIterator.next() );

            assertFalse( expectedIterator.hasNext() );
            assertFalse( actualIterator.hasNext() );
        }
        catch ( AssertionError e )
        {
            System.err.println( "EXPECTED directory structure:" );
            for ( String string : expectedList )
                System.err.println( "  " + string );

            System.err.println( "ACTUAL directory structure:" );
            for ( String string : actualList )
                System.err.println( "  " + string );

            throw e;
        }
    }
}
