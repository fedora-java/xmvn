/*-
 * Copyright (c) 2014-2019 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.Package;
import org.fedoraproject.xmvn.tools.install.RegularFile;
import org.fedoraproject.xmvn.tools.install.SymbolicLink;

/**
 * @author msimacek
 */
public class PackageTest
    extends AbstractFileTest
{
    private final Path jar = getResource( "example.jar" );

    @Test
    public void testSimplePackage()
        throws Exception
    {
        File jarfile = new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar );
        Package pkg = new Package( "my-id" );
        assertEquals( "my-id", pkg.getId() );
        pkg.addFile( jarfile );

        pkg.install( installRoot );
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

        pkg.install( installRoot );
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar",
                                  "L /usr/share/java/link.jar" );
        assertDescriptorEquals( pkg, "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0600,root,root) /usr/share/java/foobar.jar", "/usr/share/java/link.jar" );
    }

    @Test
    public void testEmpty()
        throws Exception
    {
        Package pkg = new Package( "my-id" );

        pkg.install( installRoot );
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
        Package samePkg = new Package( "my-id" );
        Package anotherPkg = new Package( "other-id" );

        assertEquals( samePkg, pkg );
        assertNotEquals( pkg, anotherPkg );
        assertNotEquals( samePkg, anotherPkg );
    }
}
