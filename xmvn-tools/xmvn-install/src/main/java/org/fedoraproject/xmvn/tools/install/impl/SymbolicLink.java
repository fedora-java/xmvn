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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A symbolic link installed in target package.
 * 
 * @author Mikolaj Izdebski
 */
class SymbolicLink
    extends File
{
    /**
     * Path this symlink points to. Can be relative or absolute.
     */
    private final Path referencedPath;

    /**
     * Create a new symbolic link object.
     * 
     * @param targetPath location where the symbolic link will be placed (relative to install root)
     * @param referencedPath path referenced by this symlink (i.e. contents of the symlink)
     */
    public SymbolicLink( Path targetPath, Path referencedPath )
    {
        super( targetPath );
        this.referencedPath = referencedPath;
    }

    @Override
    protected void installContents( Path targetAbsolutePath )
        throws IOException
    {
        Path parent = getTargetPath().getParent();
        if ( parent == null )
            parent = Paths.get( "/" );
        Path relativePath = parent.relativize( referencedPath );

        Files.createSymbolicLink( targetAbsolutePath, relativePath );
    }
}
