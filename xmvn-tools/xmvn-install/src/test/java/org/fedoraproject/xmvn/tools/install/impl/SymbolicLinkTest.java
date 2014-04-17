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

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Michael Simacek
 */
public class SymbolicLinkTest
    extends AbstractFileTest
{
    @Test
    public void testSymlink()
        throws Exception
    {
        Path jar = getResource( "example.jar" );
        add( new Directory( Paths.get( "usr/share/java" ) ) );
        add( new RegularFile( Paths.get( "usr/share/java/foobar.jar" ), jar ) );
        add( new SymbolicLink( Paths.get( "usr/share/java/link.jar" ), Paths.get( "foobar.jar" ) ) );
        Path root = performInstallation();
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "F /usr/share/java/foobar.jar",
                                  "L /usr/share/java/link.jar" );
        Path link = root.resolve( Paths.get( "usr/share/java/link.jar" ) );
        assertEquals( Files.readSymbolicLink( link ), Paths.get( "foobar.jar" ) );

        assertDescriptorEquals( "%attr(0755,root,root) %dir /usr/share/java",
                                "%attr(0644,root,root) /usr/share/java/foobar.jar",
                                "%attr(0644,root,root) /usr/share/java/link.jar" );
    }
}
