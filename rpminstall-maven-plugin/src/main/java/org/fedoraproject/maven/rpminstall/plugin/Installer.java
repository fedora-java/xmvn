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

import com.google.common.io.Files;

public class Installer
{
    public static final String JAR_DIR = "usr/share/java";

    public static final String JNI_DIR = "usr/lib/java";

    public static final String POM_DIR = "usr/share/maven-poms";

    public static final String DEPMAP_DIR = "usr/share/maven-fragments";

    private final File root;

    public Installer( File root )
    {
        this.root = root;
    }

    public File createDirectory( String path )
    {
        File dir = new File( root, path );
        dir.mkdirs();
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
        Files.copy( source, target );
        return target;
    }
}
