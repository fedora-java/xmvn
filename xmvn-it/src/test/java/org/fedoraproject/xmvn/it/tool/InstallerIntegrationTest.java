/*-
 * Copyright (c) 2017-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Test;

/**
 * Integration tests for XMvn Installer tool.
 * 
 * @author Mikolaj Izdebski
 */
public class InstallerIntegrationTest
    extends AbstractToolIntegrationTest
{
    @Test
    public void testInstallerHelp()
        throws Exception
    {
        assertEquals( 0, invokeTool( "xmvn-install", "--help" ) );
        assertFalse( getStderr().findAny().isPresent() );
        assertTrue( getStdout().anyMatch( line -> line.startsWith( "Usage: xmvn-install" ) ) );
    }

    @Test
    public void testInstallJar()
        throws Exception
    {
        expandBaseDirInPlace( "install-plan.xml" );

        assertEquals( 0, invokeTool( "xmvn-install", "-n", "xyzzy", "-R", "install-plan.xml", "-d", "dest", "-X", "-i",
                                     "custom-install" ) );
        assertFalse( getStdout().findAny().isPresent() );
        assertTrue( getStderr().anyMatch( line -> line.equals( "[INFO] Installation successful" ) ) );

        Path pomPath = Paths.get( "dest/usr/share/maven-poms/xyzzy/junit.pom" );
        assertTrue( Files.isRegularFile( pomPath, LinkOption.NOFOLLOW_LINKS ) );
        assertEquals( "NOT A VALID XML <XMvn should not parse this...>",
                      Files.readAllLines( pomPath ).iterator().next() );

        Path mdPath = Paths.get( "dest/usr/share/maven-metadata/xyzzy.xml" );
        assertTrue( Files.isRegularFile( mdPath, LinkOption.NOFOLLOW_LINKS ) );
        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", Files.readAllLines( mdPath ).iterator().next() );

        Path jarPath = Paths.get( "dest/usr/share/java/xyzzy/junit.jar" );
        assertTrue( Files.isRegularFile( jarPath, LinkOption.NOFOLLOW_LINKS ) );
        Attributes mf = new Manifest( new URL( "jar:file:" + jarPath.toAbsolutePath()
            + "!/META-INF/MANIFEST.MF" ).openStream() ).getMainAttributes();
        assertEquals( "junit", mf.getValue( "JavaPackages-GroupId" ) );
        assertEquals( "junit", mf.getValue( "JavaPackages-ArtifactId" ) );
        assertEquals( "4.12", mf.getValue( "JavaPackages-Version" ) );
        assertEquals( "42", mf.getValue( "X-Test1" ) );
    }
}
