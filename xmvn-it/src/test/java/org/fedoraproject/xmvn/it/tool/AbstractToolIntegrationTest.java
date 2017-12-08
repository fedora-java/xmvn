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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fedoraproject.xmvn.it.AbstractIntegrationTest;

/**
 * Abstract base class for integration tests that involve invoking XMvn tools (resolve, bisect and so on).
 * 
 * @author Mikolaj Izdebski
 */
public abstract class AbstractToolIntegrationTest
    extends AbstractIntegrationTest
{
    public ProcessBuilder buildToolSubprocess( String tool, String... args )
        throws Exception
    {
        Path javaHome = Paths.get( System.getProperty( "java.home" ) );
        Path javaCmd = javaHome.resolve( "bin/java" );
        assertTrue( Files.isExecutable( javaCmd ) );
        assertTrue( Files.isRegularFile( javaCmd ) );

        String subDir;
        if ( tool.equals( "xmvn-install" ) )
            subDir = "installer";
        else if ( tool.equals( "xmvn-resolve" ) )
            subDir = "resolver";
        else
            subDir = tool.replaceAll( "^xmvn-", "" );

        Path libDir = getMavenHome().resolve( "lib" ).resolve( subDir );
        Path toolJar = Files.newDirectoryStream( libDir, tool + "*.jar" ).iterator().next();
        assertTrue( Files.isRegularFile( toolJar ) );

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
}
