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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;

public class Installer
{
    private final File root;

    public Installer( String rootPath )
        throws IOException
    {
        File buildRoot = new File( rootPath );

        if ( !buildRoot.isDirectory() && buildRoot.mkdir() == false )
            throw new IOException( "Failed to create directory: " + rootPath );

        this.root = buildRoot;
    }

    public File createDirectory( String path )
        throws IOException
    {
        File dir = new File( root, path );
        if ( !dir.isDirectory() && dir.mkdirs() == false )
            throw new IOException( "Unable to create directory: " + dir.getPath() );
        return dir;
    }

    public File touchFile( String dirPath, String fileName )
        throws IOException
    {
        File dir = createDirectory( dirPath );
        File file = new File( dir, fileName );
        file.createNewFile();
        return file;
    }

    public File installFile( File source, String targetDir, String targetName )
        throws IOException
    {
        File dir = createDirectory( targetDir );
        File target = new File( dir, targetName );
        linkOrCopy( source, target );
        return target;
    }

    private void linkOrCopy( File source, File target )
        throws IOException
    {
        try
        {
            Files.createLink( target.toPath(), source.toPath() );
        }
        catch ( IOException | UnsupportedOperationException e )
        {
            Files.copy( source.toPath(), target.toPath(), LinkOption.NOFOLLOW_LINKS );
        }
    }
}
