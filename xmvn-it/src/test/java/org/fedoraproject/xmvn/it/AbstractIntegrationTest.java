/*-
 * Copyright (c) 2015 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractIntegrationTest
{
    private static final String STDOUT = "stdout.txt";

    private static final String STDERR = "stderr.txt";

    @Rule
    public TestName testName = new TestName();

    private Path mavenHome;

    private Path baseDir;

    private boolean expectFailure;

    public Path getMavenHome()
    {
        return mavenHome;
    }

    public Path getBaseDir()
    {
        return baseDir;
    }

    @BeforeClass
    public static void ensureCorrectWorkingDirectory()
        throws Exception
    {
        Path cwd = Paths.get( "." ).toRealPath();
        if ( !cwd.endsWith( "xmvn-it/target/work" ) )
            throw new RuntimeException( "XMvn integration tests must be ran from xmvn-it/target/work directory" );
    }

    @Before
    public void createBaseDir()
        throws Exception
    {
        mavenHome = Paths.get( "../dependency/xmvn-2.6.0-SNAPSHOT" ).toAbsolutePath();

        baseDir = Paths.get( "." ).toRealPath();
        delete( baseDir );
        Path baseDirTemplate = Paths.get( "../../src/test/resources" ).resolve( testName.getMethodName() );
        if ( Files.isDirectory( baseDirTemplate, LinkOption.NOFOLLOW_LINKS ) )
        {
            copy( baseDirTemplate, baseDir );
        }
        else
        {
            Files.createDirectories( baseDir );
        }

        expectFailure = false;

        Path metadataPath = Paths.get( "../../src/test/resources/metadata.xml" );
        String metadata = new String( Files.readAllBytes( metadataPath ), StandardCharsets.UTF_8 );
        metadata = metadata.replaceAll( "@@@", baseDir.toString() );
        Files.write( Paths.get( "metadata.xml" ), metadata.getBytes( StandardCharsets.UTF_8 ) );
    }

    @After
    public void saveBaseDir()
        throws Exception
    {
        Path saveDir = Paths.get( "../saved-work" ).resolve( testName.getMethodName() ).toAbsolutePath();
        Files.createDirectories( saveDir );
        delete( saveDir );
        copy( baseDir, saveDir );
        delete( baseDir );
    }

    public void expectFailure()
    {
        expectFailure = true;
    }

    private void delete( Path path )
        throws IOException
    {
        for ( Path child : Files.newDirectoryStream( path ) )
        {
            if ( Files.isDirectory( child, LinkOption.NOFOLLOW_LINKS ) )
                delete( child );

            Files.deleteIfExists( child );
        }
    }

    private void copy( Path source, Path target )
        throws Exception
    {
        for ( Path child : Files.newDirectoryStream( source ) )
        {
            Path targetChild = target.resolve( child.getFileName() );
            Files.copy( child, targetChild );

            if ( Files.isDirectory( child, LinkOption.NOFOLLOW_LINKS ) )
                copy( child, targetChild );
        }
    }

    private URL[] getBootClasspath()
        throws IOException
    {
        Set<URL> bootClassPath = new LinkedHashSet<>();
        try (DirectoryStream<Path> dir = Files.newDirectoryStream( mavenHome.resolve( "boot" ), "*.jar" ))
        {
            for ( Path jar : dir )
            {
                bootClassPath.add( jar.toUri().toURL() );
            }
        }

        return bootClassPath.toArray( new URL[bootClassPath.size()] );
    }

    public void performTest( String... args )
        throws Exception
    {
        try (PrintStream out = new PrintStream( Files.newOutputStream( baseDir.resolve( STDOUT ) ) );
                        PrintStream err = new PrintStream( Files.newOutputStream( baseDir.resolve( STDERR ) ) ))
        {
            assertEquals( expectFailure ? 1 : 0, run( out, err, args ) );
        }

        assertFalse( getStderr().findAny().isPresent() );

        if ( expectFailure )
        {
            assertTrue( getStdout().anyMatch( s -> s.equals( "[INFO] BUILD FAILURE" ) ) );
        }
    }

    private int run( PrintStream out, PrintStream err, String... args )
        throws Exception
    {
        Properties originalProperties = System.getProperties();
        System.setProperties( null );
        System.setProperty( "maven.home", mavenHome.toString() );
        System.setProperty( "user.dir", baseDir.toString() );
        System.setProperty( "maven.multiModuleProjectDirectory", baseDir.toString() );
        System.setProperty( "xmvn.config.sandbox", "true" );

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader().getParent();
        try (URLClassLoader bootClassLoader = new URLClassLoader( getBootClasspath(), parentClassLoader ))
        {
            Thread.currentThread().setContextClassLoader( bootClassLoader );

            Class<?> launcherClass = bootClassLoader.loadClass( "org.codehaus.plexus.classworlds.launcher.Launcher" );
            Object launcher = launcherClass.newInstance();

            try (InputStream config = Files.newInputStream( mavenHome.resolve( "bin/m2.conf" ) ))
            {
                launcherClass.getMethod( "configure", InputStream.class ).invoke( launcher, config );
            }

            Object classWorld = launcherClass.getMethod( "getWorld" ).invoke( launcher );
            ClassLoader classRealm = (ClassLoader) launcherClass.getMethod( "getMainRealm" ).invoke( launcher );

            Class<?> cliClass = (Class<?>) launcherClass.getMethod( "getMainClass" ).invoke( launcher );
            Object mavenCli = cliClass.getConstructor( classWorld.getClass() ).newInstance( classWorld );

            Thread.currentThread().setContextClassLoader( classRealm );
            return (int) cliClass.getMethod( "doMain", String[].class, String.class, PrintStream.class,
                                             PrintStream.class ).invoke( mavenCli, args, baseDir.toString(), out, err );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
            System.setProperties( originalProperties );
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
}
