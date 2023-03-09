/*-
 * Copyright (c) 2015-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Abstract base class for all integration tests.
 * 
 * @author Mikolaj Izdebski
 */
public abstract class AbstractIntegrationTest
{
    public static final String STDOUT = "stdout.txt";

    public static final String STDERR = "stderr.txt";

    private Path mavenHome;

    private Path baseDir;

    public Path getMavenHome()
    {
        return mavenHome;
    }

    public Path getBaseDir()
    {
        return baseDir;
    }

    public Path getRootDir()
    {
        return getBaseDir().getParent().getParent().getParent();
    }

    public void expandBaseDir( String source, String target )
        throws Exception
    {
        String metadata = new String( Files.readAllBytes( Paths.get( source ) ), StandardCharsets.UTF_8 );
        metadata = metadata.replaceAll( "@@@", baseDir.toString() );
        Files.write( Paths.get( target ), metadata.getBytes( StandardCharsets.UTF_8 ) );
    }

    public void expandBaseDirInPlace( String sourceAndTarget )
        throws Exception
    {
        expandBaseDir( sourceAndTarget, sourceAndTarget );
    }

    @BeforeAll
    public static void ensureCorrectWorkingDirectory()
        throws Exception
    {
        String workdirSuffix = System.getProperty( "xmvnITWorkdirSuffix", "" );
        String workdir = "xmvn-it/target/work" + workdirSuffix;
        Path cwd = Paths.get( "." ).toRealPath();
        if ( !cwd.endsWith( workdir ) )
        {
            throw new RuntimeException( "XMvn integration tests must be ran from " + workdir + " directory" );
        }
    }

    @BeforeEach
    public void createBaseDir( TestInfo testInfo )
        throws Exception
    {
        mavenHome = Paths.get( "../dependency/xmvn-4.0.0" ).toAbsolutePath();

        baseDir = Paths.get( "." ).toRealPath();
        delete( baseDir );
        String testName = testInfo.getTestMethod().get().getName();
        Path baseDirTemplate = Paths.get( "../../src/test/resources" ).resolve( testName );
        if ( Files.isDirectory( baseDirTemplate, LinkOption.NOFOLLOW_LINKS ) )
        {
            copy( baseDirTemplate, baseDir );
        }
        else
        {
            Files.createDirectories( baseDir );
        }

        expandBaseDir( "../../src/test/resources/metadata.xml", "metadata.xml" );
    }

    @AfterEach
    public void saveBaseDir( TestInfo testInfo )
        throws Exception
    {
        String testName = testInfo.getTestMethod().get().getName();
        Path saveDir = Paths.get( "../saved-work" ).resolve( testName ).toAbsolutePath();
        Files.createDirectories( saveDir );
        delete( saveDir );
        copy( baseDir, saveDir );
        delete( baseDir );
    }

    private void delete( Path path )
        throws IOException
    {
        try ( DirectoryStream<Path> ds = Files.newDirectoryStream( path ) )
        {
            for ( Path child : ds )
            {
                if ( Files.isDirectory( child, LinkOption.NOFOLLOW_LINKS ) )
                {
                    delete( child );
                }

                Files.deleteIfExists( child );
            }
        }
    }

    private void copy( Path source, Path target )
        throws Exception
    {
        try ( DirectoryStream<Path> ds = Files.newDirectoryStream( source ) )
        {
            for ( Path child : ds )
            {
                Path targetChild = target.resolve( child.getFileName() );
                Files.copy( child, targetChild );

                if ( Files.isDirectory( child, LinkOption.NOFOLLOW_LINKS ) )
                {
                    copy( child, targetChild );
                }
            }
        }
    }

    public Stream<String> getStdout()
        throws Exception
    {
        return Files.lines( baseDir.resolve( STDOUT ) );
    }

    public Stream<String> getStderr()
        throws Exception
    {
        return Files.lines( baseDir.resolve( STDERR ) );
    }

    public static int getJavaVersion()
    {
        return Integer.parseInt( System.getProperty( "java.version" ).replaceAll( "\\..*", "" ) );
    }
}
