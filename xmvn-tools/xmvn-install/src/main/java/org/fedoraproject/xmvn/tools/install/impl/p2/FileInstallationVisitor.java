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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.fedoraproject.xmvn.tools.install.impl.Package;

/**
 * @author Mikolaj Izdebski
 */
class FileInstallationVisitor
    implements FileVisitor<Path>
{
    private final Package pkg;

    private final Path basePath;

    private final Path targetRoot;

    public FileInstallationVisitor( Package pkg, Path basePath, Path targetRoot )
    {
        this.pkg = pkg;
        this.basePath = basePath;
        this.targetRoot = targetRoot;
    }

    @Override
    public FileVisitResult postVisitDirectory( Path path, IOException e )
        throws IOException
    {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory( Path path, BasicFileAttributes attrs )
        throws IOException
    {
        if ( Files.isSymbolicLink( path ) )
            return FileVisitResult.SKIP_SUBTREE;

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile( Path path, BasicFileAttributes attrs )
        throws IOException
    {
        if ( !Files.isRegularFile( path ) )
            return FileVisitResult.CONTINUE;

        pkg.addFile( path, targetRoot.resolve( basePath.relativize( path ) ), 0644 );

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed( Path path, IOException e )
        throws IOException
    {
        return FileVisitResult.CONTINUE;
    }
}
