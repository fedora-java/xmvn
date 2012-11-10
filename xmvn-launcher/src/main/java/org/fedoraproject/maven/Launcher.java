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
package org.fedoraproject.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Launch XMvn by calling Plexus Classworlds launcher.
 * 
 * @author Mikolaj Izdebski
 */
public class Launcher
{
    private static void launch( String[] args )
        throws Throwable
    {
        String xmvnDir = "/usr/share/java/xmvn";

        String mavenHome = System.getenv( "XMVN_MAVEN_HOME" );
        if ( mavenHome == null )
            mavenHome = System.getProperty( "xmvn.maven.home" );
        if ( mavenHome == null )
            mavenHome = "/usr/share/maven";
        System.setProperty( "xmvn.maven.home", mavenHome );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream conf = new PrintStream( bos );
        conf.println( "main is org.fedoraproject.maven.connector.Main from plexus.core" );
        conf.println( "[plexus.core]" );
        conf.println( "load " + xmvnDir + "/xmvn-core.jar" );
        conf.println( "load " + xmvnDir + "/xmvn-connector.jar" );
        conf.println( "optionally " + mavenHome + "/lib/ext/*.jar" );
        conf.println( "load " + mavenHome + "/lib/*.jar" );
        conf.close();

        org.codehaus.plexus.classworlds.launcher.Launcher launcher;
        launcher = new org.codehaus.plexus.classworlds.launcher.Launcher();
        launcher.configure( new ByteArrayInputStream( bos.toByteArray() ) );
        launcher.launch( args );
    }

    public static void main( String[] args )
    {
        try
        {
            launch( args );
        }
        catch ( Throwable exception )
        {
            while ( ( exception.getClass() == RuntimeException.class || exception instanceof InvocationTargetException )
                && exception.getCause() != null )
            {
                exception = exception.getCause();
            }

            StringBuilder detail = new StringBuilder();
            for ( Throwable exc = exception; exc != null; exc = exc.getCause() )
            {
                if ( exc.getMessage() != null && exc.getMessage().length() < 200
                    && detail.indexOf( exc.getMessage() ) < 0 )
                {
                    detail.append( exc.getMessage() );
                    detail.append( System.lineSeparator() );
                }
            }

            System.err.println();
            System.err.println( "--------------------------------------------------" );
            System.err.println( "FATAL ERROR:" );
            System.err.println();
            exception.printStackTrace();
            System.err.println();
            System.err.println( "--------------------------------------------------" );
            System.err.println( "XMvn was terminated because of unhandled exception" );
            System.err.println( "Class: " + exception.getClass().getCanonicalName() );
            System.err.println( "--------------------------------------------------" );
            System.err.print( detail.toString() );
            System.exit( 1 );
        }
    }
}
