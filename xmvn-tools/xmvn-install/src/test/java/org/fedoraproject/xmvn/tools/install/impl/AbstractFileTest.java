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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxWriter;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractFileTest
{
    private final List<File> files = new ArrayList<>();

    protected Path workdir;

    private final List<String> descriptors = new ArrayList<>();

    protected void add( File file )
        throws Exception
    {
        files.add( file );
    }

    @Before
    public void setUpWorkdir()
            throws IOException
    {
        String testName = getClass().getName();
        Path workPath = Paths.get( "target" ).resolve( "test-work" );
        Files.createDirectories( workPath );
        workdir = Files.createTempDirectory( workPath, testName );
    }

    @After
    public void tearDownWorkdir()
            throws IOException
    {
        Files.walkFileTree( workdir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile( Path file, BasicFileAttributes attrs )
                    throws IOException
            {
                Files.delete( file );
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory( Path dir, IOException exc )
                    throws IOException
            {
                Files.delete( dir );
                return FileVisitResult.CONTINUE;
            }

        } );
    }

    protected Path performInstallation()
        throws Exception
    {
        try
        {
            for ( File file : files )
                file.install( workdir );

            for ( File file : files )
                descriptors.add( file.getDescriptor() );

            return workdir;
        }
        finally
        {
            files.clear();
        }
    }

    protected void assertDirectoryStructure( String... expected )
        throws Exception
    {
        assertDirectoryStructure( workdir, expected );
    }

    protected void assertDirectoryStructure( Path root, String... expected )
        throws Exception
    {
        List<String> actualList = new ArrayList<>();
        Files.walkFileTree( root, new FileSystemWalker( root, actualList ) );
        assertEqualsImpl( actualList, "directory structure", expected );
    }

    protected void assertDescriptorEquals( String... expected )
    {
        assertEqualsImpl( descriptors, "file descriptor", expected );
    }

    private void assertEqualsImpl( List<String> actualList, String what, String... expected )
    {
        List<String> expectedList = new ArrayList<>();
        for ( String string : expected )
            expectedList.add( string );

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
            System.err.println( "EXPECTED " + what + ":" );
            for ( String string : expectedList )
                System.err.println( "  " + string );

            System.err.println( "ACTUAL " + what + ":" );
            for ( String string : actualList )
                System.err.println( "  " + string );

            throw e;
        }
    }

    Path getResource( String name )
    {
        return Paths.get( "src/test/resources/", name ).toAbsolutePath();
    }

    void assertFilesEqual( Path expected, Path actual )
        throws IOException
    {
        byte expectedContent[] = Files.readAllBytes( expected );
        byte actualContent[] = Files.readAllBytes( actual );
        assertTrue( Arrays.equals( expectedContent, actualContent ) );
    }

    protected void assertDescriptorEquals( Package pkg, String... expected )
        throws IOException
    {
        Path mfiles = workdir.resolve( ".mfiles" );
        pkg.writeDescriptor( mfiles );
        List<String> lines = Files.readAllLines( mfiles, Charset.defaultCharset() );

        Iterator<String> iterator = lines.iterator();

        for ( String expectedLine : expected )
        {
            assertTrue( iterator.hasNext() );
            assertEquals( expectedLine, iterator.next() );
        }
    }

    protected Path prepareInstallationPlanFile( String filename )
            throws Exception
    {
        Path metadataPath = getResource( filename );
        PackageMetadata metadata = new MetadataStaxReader().read( metadataPath.toString() );
        for ( ArtifactMetadata artifact : metadata.getArtifacts() )
        {
            String path = artifact.getPath();
            if ( path != null )
            {
                path = path.replace( "src/test/resources", getResource( "" ).toAbsolutePath().toString() );
                artifact.setPath( path );
            }
        }
        Path newMetadata = workdir.resolve( filename );
        try ( OutputStream os = Files.newOutputStream( newMetadata ) )
        {
            new MetadataStaxWriter().write( os, metadata );
        }
        return newMetadata;
    }

    protected InstallationPlan createInstallationPlan( String filename )
            throws Exception
    {
        
        return new InstallationPlan( prepareInstallationPlanFile( filename ) );
    }
}
