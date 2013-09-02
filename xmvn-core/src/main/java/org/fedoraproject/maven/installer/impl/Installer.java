/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.installer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fedoraproject.maven.utils.FileUtils;

/**
 * @author Mikolaj Izdebski
 */
class Installer
{
    private final Path root;

    public Installer( Path root )
        throws IOException
    {
        if ( Files.exists( root ) && !Files.isDirectory( root ) )
            throw new IOException( root + " is not a directory" );

        Files.createDirectories( root );
        this.root = root;
    }

    public Path installDirectory( Path target )
        throws IOException
    {
        Path dir = root.resolve( target );
        return Files.createDirectories( dir );
    }

    public Path installFile( Path source, Path targetDir, Path targetName, int mode )
        throws IOException
    {
        Path dir = installDirectory( targetDir );

        Path target = dir.resolve( targetName );
        FileUtils.linkOrCopy( source, target );
        FileUtils.chmod( target, mode );

        return target;
    }
}
