/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.RegularFile;

/**
 * @author Mikolaj Izdebski
 */
public class DirectoryTest
    extends AbstractFileTest
{
    /**
     * Basic test for directory installation
     * 
     * @throws Exception
     */
    @Test
    public void testSingleDirectoryInstallation()
        throws Exception
    {
        add( new Directory( Paths.get( "usr/src/sys/kern" ) ) );
        performInstallation();

        assertDirectoryStructure( "D /usr", "D /usr/src", "D /usr/src/sys", "D /usr/src/sys/kern" );

        assertDescriptorEquals( "%attr(0755,root,root) %dir /usr/src/sys/kern" );
    }

    /**
     * Test if installing directory which already exists doesn't cause I/O (or other) errors
     * 
     * @throws Exception
     */
    @Test
    public void testExistentDirectoryInstallation()
        throws Exception
    {
        add( new Directory( Paths.get( "etc/sysconfig/java" ) ) );
        add( new Directory( Paths.get( "etc/xmvn" ) ) );
        add( new Directory( Paths.get( "etc" ) ) );
        performInstallation();

        assertDirectoryStructure( "D /etc", "D /etc/sysconfig", "D /etc/sysconfig/java", "D /etc/xmvn" );

        assertDescriptorEquals( "%attr(0755,root,root) %dir /etc", "%attr(0755,root,root) %dir /etc/sysconfig/java",
                                "%attr(0755,root,root) %dir /etc/xmvn" );
    }

    /**
     * Test if directory installation fails if target already exists and is not a directory.
     */
    @Test
    public void testExistentTargetFile()
        throws Exception
    {
        add( new RegularFile( Paths.get( "foo/bar" ), new byte[0] ) );
        add( new Directory( Paths.get( "foo/bar" ) ) );
        assertThrows( IOException.class, this::performInstallation );
    }

    /**
     * Test if directory installation fails if component of target directory target already exists and is not a
     * directory.
     */
    @Test
    public void testDirectoryCOmponentIsAFile()
        throws Exception
    {
        add( new RegularFile( Paths.get( "a/b/c/d" ), new byte[0] ) );
        add( new Directory( Paths.get( "a/b/c/d/e/f/g/h" ) ) );
        assertThrows( IOException.class, this::performInstallation );
    }

    /**
     * Test if custom access mode can be specified.
     */
    @Test
    public void testAccessMode()
        throws Exception
    {
        add( new Directory( Paths.get( "foo" ), 320 ) );
        performInstallation();

        assertDirectoryStructure( "D /foo" );

        assertDescriptorEquals( "%attr(0500,root,root) %dir /foo" );
    }
}
