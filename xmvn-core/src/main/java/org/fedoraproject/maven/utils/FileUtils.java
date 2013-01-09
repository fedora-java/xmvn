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
package org.fedoraproject.maven.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Various utilities related to file and path manipulation.
 * 
 * @author Mikolaj Izdebski
 */
public class FileUtils
{
    /**
     * Current working directory.
     */
    public static final File CWD = new File( "." );

    /**
     * Root directory of the file system.
     */
    public static final File ROOT = new File( "/" );

    /**
     * Bit bucket. Any data written to this file is discarded.
     */
    public static final File BIT_BUCKET = new File( "/dev/null" );

    /**
     * Follow every symlink in every component of given file recursively, just like <code>readlink -f</code> does.
     * 
     * @param file path in which symlinks are to be followed
     * @return canonical file
     */
    public static File followSymlink( File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return file;
        }
    }

    /**
     * Return process current working directory.
     * 
     * @return current working directory
     */
    public static File getCwd()
    {
        return CWD.getAbsoluteFile();
    }

    /**
     * Create hard link or copy file if creating hard link is not possible.
     * 
     * @param source source file
     * @param target link target or destination file
     * @throws IOException if any I/O exception occurs
     */
    public static void linkOrCopy( Path source, Path target )
        throws IOException
    {
        try
        {
            Files.createLink( target, source );
        }
        catch ( IOException | UnsupportedOperationException e )
        {
            Files.copy( source, target, LinkOption.NOFOLLOW_LINKS );
        }
    }

    /**
     * Create a temporary symbolic link pointing to specified target path
     * 
     * @param target target of the symbolic link
     * @return path to created symlink
     * @throws IOException if any I/O exception occurs
     */
    public static Path createAnonymousSymlink( Path target )
        throws IOException
    {
        File symlinkFile = File.createTempFile( "xmvn", ".symlink" );
        symlinkFile.delete();
        Path symlinkPath = symlinkFile.toPath();
        Files.createSymbolicLink( symlinkPath, target );
        return symlinkPath;
    }

    /**
     * Convert POSIX permissions from integer to string format.
     * 
     * @param perm POSIX file permissions in integer format, for example <code>0764</code>
     * @return POSIX file permissions in string format, for example <code>rwxr-xr--</code>
     */
    public static Set<PosixFilePermission> getPermissions( int perm )
    {
        if ( perm < 0 || perm > 0777 )
            throw new IllegalArgumentException( "Permissions must be in range from 0 to 0777" );

        StringBuilder sb = new StringBuilder();
        sb.append( ( perm & 0400 ) != 0 ? 'r' : '-' );
        sb.append( ( perm & 0200 ) != 0 ? 'w' : '-' );
        sb.append( ( perm & 0100 ) != 0 ? 'x' : '-' );
        sb.append( ( perm & 0040 ) != 0 ? 'r' : '-' );
        sb.append( ( perm & 0020 ) != 0 ? 'w' : '-' );
        sb.append( ( perm & 0010 ) != 0 ? 'x' : '-' );
        sb.append( ( perm & 0004 ) != 0 ? 'r' : '-' );
        sb.append( ( perm & 0002 ) != 0 ? 'w' : '-' );
        sb.append( ( perm & 0001 ) != 0 ? 'x' : '-' );

        return PosixFilePermissions.fromString( sb.toString() );
    }

    /**
     * Set file permissions, just like chmod(2).
     * 
     * @param path path to file to change permissions of
     * @param mode permissions in integer format
     * @throws IOException in an I/O exception occurs
     */
    public static void chmod( Path path, int mode )
        throws IOException
    {
        Files.setPosixFilePermissions( path, getPermissions( mode ) );
    }
}
