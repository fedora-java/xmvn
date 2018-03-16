/*-
 * Copyright (c) 2015-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * @author Mikolaj Izdebski
 */
final class TempManager
{
    private static Path tempDir;

    private TempManager()
    {
        // Avoid generating default public constructor
    }

    private static void delete( Path path )
    {
        try
        {
            if ( Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) )
            {
                for ( Path child : Files.newDirectoryStream( path ) )
                {
                    delete( child );
                }
            }

            Files.delete( path );
        }
        catch ( IOException e )
        {
            // Ignore
        }
    }

    private static synchronized Path getTempDir()
        throws IOException
    {
        if ( tempDir == null )
        {
            tempDir = Files.createTempDirectory( "xmvn-" );
            Runtime.getRuntime().addShutdownHook( new Thread( () -> delete( tempDir ) ) );
        }

        return tempDir;
    }

    public static Path createTempFile( String prefix, String suffix, FileAttribute<?>... attrs )
        throws IOException
    {
        return Files.createTempFile( getTempDir(), prefix, suffix, attrs );
    }

    public static Path createTempDirectory( String prefix, FileAttribute<?>... attrs )
        throws IOException
    {
        return Files.createTempDirectory( getTempDir(), prefix, attrs );
    }
}
