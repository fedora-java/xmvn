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
package org.fedoraproject.xmvn.tools.install.impl.p2;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
class EclipseApplication
{
    /**
     * Location where Equinox launcher can possibly be installed.
     */
    private static final String[] EQUINOX_BASE_DIRS = { "/usr/lib/eclipse/plugins", "/usr/lib64/eclipse/plugins" };

    /**
     * Unique glob describing Equinox launcher bundle.
     */
    private static final String EQUINOX_LAUNCHER_BUNDLE_GLOB = "org.eclipse.equinox.launcher_*.jar";

    private final String application;

    public EclipseApplication( String application )
    {
        this.application = application;
    }

    private Path findEquinoxLauncher()
        throws IOException
    {
        for ( String baseDir : EQUINOX_BASE_DIRS )
        {
            try (DirectoryStream<Path> stream =
                Files.newDirectoryStream( Paths.get( baseDir ), EQUINOX_LAUNCHER_BUNDLE_GLOB ))
            {
                for ( Path bundle : stream )
                    return bundle.toAbsolutePath();
            }
        }

        throw new RuntimeException( "Unable to find Equinox launcher bundle" );
    }

    public void run( List<String> arguments )
    {
        try
        {
            Path workspace = Files.createTempDirectory( "xmvn-p2-workspace-" );

            List<String> command = new ArrayList<>();

            command.add( "java" );

            command.add( "-jar" );
            command.add( findEquinoxLauncher().toString() );

            command.add( "-nosplash" );

            // command.add( "-install" );
            // command.add( workspace.toString() );

            command.add( "-configuration" );
            command.add( workspace.resolve( "configuration" ).toString() );

            command.add( "-data" );
            command.add( workspace.resolve( "data" ).toString() );

            command.add( "-application" );
            command.add( application );

            command.addAll( arguments );

            ProcessBuilder builder = new ProcessBuilder( command );
            builder.redirectInput( Redirect.PIPE );
            builder.redirectOutput( Redirect.INHERIT );
            builder.redirectError( Redirect.INHERIT );

            Process process = builder.start();
            process.getOutputStream().close();
            int exitCode = process.waitFor();
            if ( exitCode != 0 )
                throw new IOException( "Eclipse subprocess terminated with non-zero exit code " + exitCode );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
