/*-
 * Copyright (c) 2014-2021 Red Hat, Inc.
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
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * A directory installed as part of a package.
 * <p>
 * While package doesn't have to own all directories it creates, directories represented by instances of this class are
 * assumed to be owned by the package they belong to.
 * 
 * @author Mikolaj Izdebski
 */
public class Directory
    extends File
{
    /**
     * Create a directory with specified path and default permissions (0755).
     * 
     * @param targetPath directory path, relative to installation root
     */
    public Directory( Path targetPath )
    {
        this( targetPath, DIRECTORY_MODE );
    }

    /**
     * Create a directory with specified path and permissions.
     * 
     * @param targetPath directory path, relative to installation root
     * @param accessMode Unix access mode of the file (must be an integer in range from 0 to 0777)
     */
    public Directory( Path targetPath, int accessMode )
    {
        super( targetPath, accessMode );
    }

    @Override
    protected void installContents( Path targetAbsolutePath )
        throws IOException
    {
        if ( !Files.isDirectory( targetAbsolutePath, LinkOption.NOFOLLOW_LINKS ) )
            Files.createDirectory( targetAbsolutePath );
    }

    @Override
    protected String getDescriptorExtra()
    {
        return "%dir";
    }
}
