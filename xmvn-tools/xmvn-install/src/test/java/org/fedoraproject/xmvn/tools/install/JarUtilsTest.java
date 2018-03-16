/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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

        try ( JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ) )
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
     * Test JAR if manifest injection works when MANIFEST.MF file appears later in the file (for example produced by
     * adding manifest to existing jar with plain zip)
     *
     * @throws Exception
     */
    @Test
    public void testManifestInjectionLateManifest()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/late-manifest.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );
        JarUtils.injectManifest( testJar, artifact );

        try ( JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ) )
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
     * Test JAR if manifest injection works when MANIFEST.MF entry is duplicated
     *
     * @throws Exception
     */
    @Test
    public void testManifestInjectionDuplicateManifest()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/duplicate-manifest.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );
        JarUtils.injectManifest( testJar, artifact );

        try ( JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ) )
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

        try ( JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ) )
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

    /**
     * Test if any of utility functions throws exception when trying to access invalid JAR file.
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidJar()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/invalid.jar" );
        Path testJar = workDir.resolve( "invalid.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "foo", "bar" );
        JarUtils.injectManifest( testJar, artifact );

        byte[] testJarContent = Files.readAllBytes( testJar );
        byte[] testResourceContent = Files.readAllBytes( testResource );
        assertTrue( Arrays.equals( testJarContent, testResourceContent ) );

        assertFalse( JarUtils.usesNativeCode( testResource ) );
        assertFalse( JarUtils.containsNativeCode( testResource ) );
    }
}
