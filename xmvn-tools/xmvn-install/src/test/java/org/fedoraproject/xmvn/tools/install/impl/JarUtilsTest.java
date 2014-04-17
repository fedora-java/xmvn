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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class JarUtilsTest
{
    private Path workDir;

    @Before
    public void setUp()
        throws Exception
    {
        workDir = Paths.get( "target/test-work" );
        Files.createDirectories( workDir );
    }

    /**
     * Test JAR if manifest injection works as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjection()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );
        JarUtils.injectManifest( testJar, artifact );

        try (JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ))
        {
            Manifest mf = jis.getManifest();
            assertNotNull( mf );

            Attributes attr = mf.getMainAttributes();
            assertNotNull( attr );

            assertEquals( "org.apache.maven", attr.getValue( "JavaPackages-GroupId" ) );
            assertEquals( "maven-model", attr.getValue( "JavaPackages-ArtifactId" ) );
            assertEquals( "xsd", attr.getValue( "JavaPackages-Extension" ) );
            assertEquals( "model", attr.getValue( "JavaPackages-Classifier" ) );
            assertEquals( "2.2.1", attr.getValue( "JavaPackages-Version" ) );
        }
    }

    /**
     * Test JAR if manifest injection works as expected when some artifact fields have default values.
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjectionDefaults()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest-defaults.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "xpp3", "xpp3_xpath", "jar", "", "SYSTEM" );
        JarUtils.injectManifest( testJar, artifact );

        try (JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ))
        {
            Manifest mf = jis.getManifest();
            assertNotNull( mf );

            Attributes attr = mf.getMainAttributes();
            assertNotNull( attr );

            assertEquals( "xpp3", attr.getValue( "JavaPackages-GroupId" ) );
            assertEquals( "xpp3_xpath", attr.getValue( "JavaPackages-ArtifactId" ) );
            assertEquals( null, attr.getValue( "JavaPackages-Extension" ) );
            assertEquals( null, attr.getValue( "JavaPackages-Classifier" ) );
            assertEquals( null, attr.getValue( "JavaPackages-Version" ) );
        }
    }

    /**
     * Test if native code detection works as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testNativeCodeDetection()
        throws Exception
    {
        Path plainJarPath = Paths.get( "src/test/resources/example.jar" );
        Path nativeCodeJarPath = Paths.get( "src/test/resources/native-code.jar" );
        Path nativeMethodJarPath = Paths.get( "src/test/resources/native-method.jar" );

        assertFalse( JarUtils.usesNativeCode( plainJarPath ) );
        assertFalse( JarUtils.containsNativeCode( plainJarPath ) );

        assertFalse( JarUtils.usesNativeCode( nativeCodeJarPath ) );
        assertTrue( JarUtils.containsNativeCode( nativeCodeJarPath ) );

        assertTrue( JarUtils.usesNativeCode( nativeMethodJarPath ) );
        assertFalse( JarUtils.containsNativeCode( nativeMethodJarPath ) );
    }
}
