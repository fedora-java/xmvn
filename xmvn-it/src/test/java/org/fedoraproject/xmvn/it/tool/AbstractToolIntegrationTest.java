/*-
 * Copyright (c) 2015-2017 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.fedoraproject.xmvn.it.AbstractIntegrationTest;

/**
 * Abstract base class for integration tests that involve invoking XMvn tools (resolve, bisect and so on).
 * 
 * @author Mikolaj Izdebski
 */
public abstract class AbstractToolIntegrationTest
    extends AbstractIntegrationTest
{
    private Path getToolLibDir( String tool )
    {
        String subDir;
        if ( tool.equals( "xmvn-install" ) )
            subDir = "installer";
        else if ( tool.equals( "xmvn-resolve" ) )
            subDir = "resolver";
        else
            subDir = tool.replaceAll( "^xmvn-", "" );

        Path libDir = getMavenHome().resolve( "lib" ).resolve( subDir );
        assertTrue( Files.isDirectory( libDir, LinkOption.NOFOLLOW_LINKS ) );
        return libDir;
    }

    private Path getJar( String tool, String glob )
        throws Exception
    {
        try ( DirectoryStream<Path> ds = Files.newDirectoryStream( getToolLibDir( tool ), glob ) )
        {
            Path jar = ds.iterator().next();
            assertTrue( Files.isRegularFile( jar, LinkOption.NOFOLLOW_LINKS ) );
            return jar;
        }
    }

    private Path findToolJar( String tool )
        throws Exception
    {
        return getJar( tool, tool + "-*.jar" );
    }

    private Attributes readManifest( Path jar )
        throws Exception
    {
        URL mfUrl = new URL( "jar:" + jar.toUri().toURL() + "!/META-INF/MANIFEST.MF" );
        try ( InputStream is = mfUrl.openConnection().getInputStream() )
        {
            return new Manifest( is ).getMainAttributes();
        }
    }

    public ProcessBuilder buildToolSubprocess( String tool, String... args )
        throws Exception
    {
        Path javaHome = Paths.get( System.getProperty( "java.home" ) );
        Path javaCmd = javaHome.resolve( "bin/java" );
        assertTrue( Files.isExecutable( javaCmd ) );
        assertTrue( Files.isRegularFile( javaCmd ) );

        Path toolJar = findToolJar( tool );

        List<String> command = new ArrayList<>();
        command.add( javaCmd.toString() );
        command.add( "-Dxmvn.config.sandbox=true" );
        command.add( "-jar" );
        command.add( toolJar.toString() );
        command.addAll( Arrays.asList( args ) );

        ProcessBuilder pb = new ProcessBuilder( command );

        pb.redirectInput( new File( "/dev/null" ) );
        pb.redirectOutput( new File( STDOUT ) );
        pb.redirectError( new File( STDERR ) );

        return pb;
    }

    public int invokeTool( String tool, String... args )
        throws Exception
    {
        Path jar = findToolJar( tool );
        Attributes mf = readManifest( jar );
        List<URL> classPathList = new ArrayList<>();
        classPathList.add( jar.toUri().toURL() );
        for ( String cpJar : mf.getValue( "Class-Path" ).split( " " ) )
        {
            classPathList.add( getJar( tool, cpJar ).toUri().toURL() );
        }
        URL[] classPath = classPathList.stream().toArray( URL[]::new );

        InputStream oldStdin = System.in;
        PrintStream oldStdout = System.out;
        PrintStream oldStderr = System.err;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader().getParent();
        try ( InputStream stdin = new FileInputStream( "/dev/null" );
                        PrintStream stdout = new PrintStream( new File( STDOUT ) );
                        PrintStream stderr = new PrintStream( new File( STDERR ) );
                        URLClassLoader toolClassLoader = new URLClassLoader( classPath, parentClassLoader ) )
        {
            Thread.currentThread().setContextClassLoader( toolClassLoader );
            System.setIn( stdin );
            System.setOut( stdout );
            System.setErr( stderr );

            Class<?> mainClass = toolClassLoader.loadClass( mf.getValue( "Main-Class" ) );
            mainClass.getMethod( "main", String[].class ).invoke( null, (Object) args );
        }
        catch ( InvocationTargetException e )
        {
            Throwable cause = e.getCause();
            System.err.println( cause );
            if ( cause instanceof SystemExit )
                return ( (SystemExit) cause ).getStatus();
            throw e;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldClassLoader );
            System.setIn( oldStdin );
            System.setOut( oldStdout );
            System.setErr( oldStderr );
        }
        return 0;
    }
}
