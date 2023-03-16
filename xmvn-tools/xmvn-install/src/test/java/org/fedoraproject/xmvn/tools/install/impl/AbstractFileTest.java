/*-
 * Copyright (c) 2014-2023 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fedoraproject.xmvn.tools.install.File;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractFileTest
    extends AbstractInstallerTest
{
    private final List<File> files = new ArrayList<>();

    protected void add( File file )
        throws Exception
    {
        files.add( file );
    }

    protected Path performInstallation()
        throws Exception
    {
        try
        {
            for ( File file : files )
                file.install( installRoot );

            for ( File file : files )
                descriptors.add( file.getDescriptor() );

            return installRoot;
        }
        finally
        {
            files.clear();
        }
    }

    void assertFilesEqual( Path expected, Path actual )
        throws IOException
    {
        byte[] expectedContent = Files.readAllBytes( expected );
        byte[] actualContent = Files.readAllBytes( actual );
        assertTrue( Arrays.equals( expectedContent, actualContent ) );
    }
}
