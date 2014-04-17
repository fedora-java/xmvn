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

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 * @author msimacek
 */
public class PackageTest
    extends AbstractFileTest
{
    private Path jar = getResource( "example.jar" );

    @Test
    public void testSimplePackage()
        throws Exception
    {
        File jarfile = new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar );
        Package pkg = new Package( "my-id" );
        assertEquals( "my-id", pkg.getId() );
        pkg.addFile( jarfile );

        performInstallation( pkg );
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar" );
        assertDescriptorEquals( pkg, "%attr(0644,root,root) /usr/share/java/foobar.jar" );
    }

    @Test
    public void testMoreFiles()
        throws Exception
    {
        File dir = new Directory( Paths.get( "usr/share/java" ) );
        File jarfile = new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar, 0600 );
        File link = new SymbolicLink( Paths.get( "usr/share/java/link.jar" ), Paths.get( "foobar.jar" ) );
        Package pkg = new Package( "my-id" );
        assertEquals( "my-id", pkg.getId() );
        pkg.addFile( dir );
        pkg.addFile( jarfile );
        pkg.addFile( link );

        performInstallation( pkg );
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar",
                                  "L /usr/share/java/link.jar" );
        assertDescriptorEquals( pkg, "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0600,root,root) /usr/share/java/foobar.jar",
                                "%attr(0644,root,root) /usr/share/java/link.jar" );
    }

    @Test
    public void testEmpty()
            throws Exception
    {
        Package pkg = new Package( "my-id" );

        performInstallation( pkg );
        assertDirectoryStructure();
        assertDescriptorEquals( pkg );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testSameFileTwice()
            throws Exception
    {
        File jarfile = new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar );
        Package pkg = new Package( "my-id" );
        assertEquals( "my-id", pkg.getId() );
        pkg.addFile( jarfile );
        pkg.addFile( jarfile );
    }

    @Test
    public void testEquality()
            throws Exception
    {
        Package pkg = new Package( "my-id" );
        Package same_pkg = new Package( "my-id" );
        Package another_pkg = new Package( "other-id" );

        assertEquals( same_pkg, pkg );
        assertNotEquals( pkg, another_pkg );
        assertNotEquals( same_pkg, another_pkg );
    }
}
