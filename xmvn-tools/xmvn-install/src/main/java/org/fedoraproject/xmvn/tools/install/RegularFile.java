/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Provider;

/**
 * A regular file created installed in target package.
 * <p>
 * The file can be installed either by coping an existing file (source file), or by writing provided contents.
 * 
 * @author Mikolaj Izdebski
 */
public class RegularFile
    extends File
{
    /**
     * Path to source file which contents will be copied to create target file. Can be {@code null}, in which case byte
     * contents are used instead.
     */
    private final Path sourcePath;

    /**
     * Provider of byte array used to populate target file. It is used only if source path is not provided (is
     * {@code null}).
     */
    private final Provider<byte[]> content;

    /**
     * Create a regular file object, which contents will be populated from a source file. Target file will have default
     * access mode (0644).
     * 
     * @param targetPath file path, relative to installation root
     * @param sourcePath path to source file which will be copied to target path
     */
    public RegularFile( Path targetPath, Path sourcePath )
    {
        this( targetPath, sourcePath, 0644 );
    }

    /**
     * Create a regular file object, which contents will be populated from a byte array. Target file will have default
     * access mode (0644).
     * 
     * @param targetPath file path, relative to installation root
     * @param content array of bytes used to populate target file contents with
     */
    public RegularFile( Path targetPath, byte[] content )
    {
        this( targetPath, content, 0644 );
    }

    /**
     * Create a regular file object, which contents will be populated from a byte array. Target file will have default
     * access mode (0644).
     * 
     * @param targetPath file path, relative to installation root
     * @param content provider of array of bytes used to populate target file contents with
     */
    public RegularFile( Path targetPath, Provider<byte[]> content )
    {
        this( targetPath, content, 0644 );
    }

    /**
     * Create a regular file object, which contents will be populated from a source file. Target file will have
     * specified access mode.
     * 
     * @param targetPath file path, relative to installation root
     * @param sourcePath path to source file which will be copied to target path
     * @param accessMode Unix access mode of the file (must be an integer in range from 0 to 0777)
     */
    public RegularFile( Path targetPath, Path sourcePath, int accessMode )
    {
        super( targetPath, accessMode );

        this.sourcePath = sourcePath;
        this.content = null;
    }

    /**
     * Create a regular file object, which contents will be populated from a byte array. Target file will have specified
     * access mode
     * 
     * @param targetPath file path, relative to installation root
     * @param content array of bytes used to populate target file contents with
     * @param accessMode Unix access mode of the file (must be an integer in range from 0 to 0777)
     */
    public RegularFile( Path targetPath, byte[] content, int accessMode )
    {
        super( targetPath, accessMode );

        this.sourcePath = null;
        this.content = ( ) -> content;
    }

    /**
     * Create a regular file object, which contents will be populated from an input stream. Target file will have
     * specified access mode
     * 
     * @param targetPath file path, relative to installation root
     * @param content provider of array of bytes used to populate target file contents with
     * @param accessMode Unix access mode of the file (must be an integer in range from 0 to 0777)
     */
    public RegularFile( Path targetPath, Provider<byte[]> content, int accessMode )
    {
        super( targetPath, accessMode );

        this.sourcePath = null;
        this.content = content;
    }

    @Override
    protected void installContents( Path targetPath )
        throws IOException
    {
        if ( sourcePath != null )
        {
            Files.copy( sourcePath, targetPath );
        }
        else
        {
            Files.write( targetPath, content.get() );
        }
    }
}
