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
package org.fedoraproject.maven.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;

public class FileUtils
{
    public static final File CWD = new File( "." );

    public static final File ROOT = new File( "/" );

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

    public static void linkOrCopy( File source, File target )
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

    public static File createAnonymousSymlink( String target )
        throws IOException
    {
        File symlinkFile = File.createTempFile( "xmvn", ".symlink" );
        symlinkFile.delete();
        Files.createSymbolicLink( symlinkFile.toPath(), new File( target ).toPath() );
        return symlinkFile;
    }
}
