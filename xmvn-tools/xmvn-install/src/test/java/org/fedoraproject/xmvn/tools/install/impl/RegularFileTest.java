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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.RegularFile;

/**
 * @author Michael Simacek
 */
public class RegularFileTest
    extends AbstractFileTest
{
    @Test
    public void testFileInstallationFromArray()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        byte[] content = Files.readAllBytes( jar );
        add( new Directory( Paths.get( "usr/share/java" ) ) );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), content ) );
        Path root = performInstallation();
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar" );
        assertFilesEqual( jar, root.resolve( Paths.get( "usr/share/java/foobar.jar" ) ) );

        assertDescriptorEquals( "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0644,root,root) /usr/share/java/foobar.jar" );
    }

    @Test
    public void testFileInstallationFromFile()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        add( new Directory( Paths.get( "usr/share/java" ) ) );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar ) );
        Path root = performInstallation();
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar" );
        assertFilesEqual( jar, root.resolve( Paths.get( "usr/share/java/foobar.jar" ) ) );
        assertDescriptorEquals( "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0644,root,root) /usr/share/java/foobar.jar" );
    }

    @Test
    public void testCreateParentDirectory()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar ) );
        Path root = performInstallation();
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar" );
        assertFilesEqual( jar, root.resolve( Paths.get( "usr/share/java/foobar.jar" ) ) );
        assertDescriptorEquals( "%attr(0644,root,root) /usr/share/java/foobar.jar" );
    }

    @Test
    public void testNonexistentFile()
        throws Exception
    {
        add( new Directory( Paths.get( "usr/share/java" ) ) );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), Paths.get( "not-here" ) ) );
        assertThrows( IOException.class, this::performInstallation );
    }

    @Test
    public void testAccessMode()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        add( new Directory( Paths.get( "usr/share/java" ) ) );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar, 0666 ) );
        Path root = performInstallation();
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar" );
        assertFilesEqual( jar, root.resolve( Paths.get( "usr/share/java/foobar.jar" ) ) );

        assertDescriptorEquals( "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0666,root,root) /usr/share/java/foobar.jar" );
    }

    @Test
    public void testIncorrectMode()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        assertThrows( IllegalArgumentException.class, //
                      () -> add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar, 01000 ) ) );
    }

    @Test
    public void testNegativeMode()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        assertThrows( IllegalArgumentException.class, //
                      () -> add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar, -0644 ) ) );
    }

    @Test
    public void testAbsoluteTarget()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        assertThrows( IllegalArgumentException.class, //
                      () -> add( new RegularFile( Paths.get( "/usr/share/java/foobar.jar" ), jar, 01000 ) ) );
    }
}
