/*-
 * Copyright (c) 2014-2021 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class JarUtilsTest
{
    private Path workDir;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        workDir = Paths.get( "target/test-work" );
        Files.createDirectories( workDir );
    }

    private void testManifestInjectionInto( String testJarName )
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( testJarName );
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
     * Test JAR if manifest injection works as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjection()
        throws Exception
    {
        testManifestInjectionInto( "manifest.jar" );
    }

    /**
     * Test injecting manifest into a file without ".jar" suffix
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjectionNoJarSuffix()
        throws Exception
    {
        testManifestInjectionInto( "foo" );
    }

    /**
     * Test injecting manifest into a hidden file, i. e. starting with a "."
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjectionHiddenFilename()
        throws Exception
    {
        testManifestInjectionInto( ".f" );
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
     * Regression test for a jar which contains an entry that can recompress with a different size, which caused a
     * mismatch in sizes.
     * 
     * @throws Exception
     */
    @Test
    public void testManifestInjectionRecompressionCausesSizeMismatch()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/recompression-size.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact =
            new DefaultArtifact( "org.eclipse.osgi", "osgi.compatibility.state", "1.1.0.v20180409-1212" );
        JarUtils.injectManifest( testJar, artifact );

        try ( JarInputStream jis = new JarInputStream( Files.newInputStream( testJar ) ) )
        {
            Manifest mf = jis.getManifest();
            assertNotNull( mf );

            Attributes attr = mf.getMainAttributes();
            assertNotNull( attr );

            assertEquals( "org.eclipse.osgi", attr.getValue( "JavaPackages-GroupId" ) );
            assertEquals( "osgi.compatibility.state", attr.getValue( "JavaPackages-ArtifactId" ) );
            assertEquals( "1.1.0.v20180409-1212", attr.getValue( "JavaPackages-Version" ) );
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
     * Test JAR if manifest injection preserves sane file perms.
     *
     * @throws Exception
     */
    @Test
    public void testManifestInjectionSanePermissions()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        assumeTrue( Files.getPosixFilePermissions( testJar ).contains( PosixFilePermission.OTHERS_READ ),
                    "sane umask" );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );
        JarUtils.injectManifest( testJar, artifact );

        assertTrue( Files.getPosixFilePermissions( testJar ).contains( PosixFilePermission.OTHERS_READ ) );
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

    /**
     * Test that the manifest file retains the same i-node after being injected into
     * 
     * @throws Exception
     */
    @Test
    public void testSameINode()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        long oldInode = (Long) Files.getAttribute( testJar, "unix:ino" );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );

        JarUtils.injectManifest( testJar, artifact );

        long newInode = (Long) Files.getAttribute( testJar, "unix:ino" );

        assertEquals( oldInode, newInode, "Different manifest I-node after injection" );
    }

    /**
     * Test that the backup file created during injectManifest was deleted after a successful operation
     * 
     * @throws Exception
     */
    @Test
    public void testBackupDeletion()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );

        Path backupPath = JarUtils.getBackupNameOf( testJar );
        Files.deleteIfExists( backupPath );
        JarUtils.injectManifest( testJar, artifact );
        assertFalse( Files.exists( backupPath ) );
    }

    /**
     * Test that the backup file created during injectManifest remains after an unsuccessful operation and its content
     * is identical to the original file
     * 
     * @throws Exception
     */
    @Test
    public void testBackupOnFailure()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getGroupId() ).andThrow( new RuntimeException( "boom" ) );
        EasyMock.replay( artifact );

        Path backupPath = JarUtils.getBackupNameOf( testJar );
        Files.deleteIfExists( backupPath );

        byte[] content = Files.readAllBytes( testJar );

        Exception ex = assertThrows( Exception.class, () -> JarUtils.injectManifest( testJar, artifact ) );

        assertTrue( ex.getMessage().contains( backupPath.toString() ),
                    "An exception thrown when injecting manifest does not mention stored backup file" );
        assertTrue( Files.exists( backupPath ) );

        byte[] backupContent = Files.readAllBytes( backupPath );

        assertArrayEquals( content, backupContent,
                           "Content of the backup file is different from the content of the original file" );

        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );
        try ( FileOutputStream os = new FileOutputStream( testJar.toFile(), true ) )
        {
            /// Append garbage to the original file to check if the content of the backup will be retained
            os.write( 0 );
        }

        assertThrows( Exception.class, () -> JarUtils.injectManifest( testJar, artifact ) );

        assertArrayEquals( backupContent, Files.readAllBytes( backupPath ),
                           "Backup file content was overwritten after an unsuccessful injection" );

        EasyMock.verify( artifact );

        Files.delete( backupPath );
    }

    /**
     * Test that injectManifest fails if the backup file already exists
     * 
     * @throws Exception
     */
    @Test
    public void testFailWhenBachupPresent()
        throws Exception
    {
        Path testResource = Paths.get( "src/test/resources/example.jar" );
        Path testJar = workDir.resolve( "manifest.jar" );
        Files.copy( testResource, testJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );

        Artifact artifact = new DefaultArtifact( "org.apache.maven", "maven-model", "xsd", "model", "2.2.1" );

        Path backupPath = JarUtils.getBackupNameOf( testJar );
        Files.deleteIfExists( backupPath );
        Files.createFile( backupPath );

        try
        {
            assertThrows( Exception.class, () -> JarUtils.injectManifest( testJar, artifact ),
                          "Expected failure because the the backup file already exists" );
        }
        finally
        {
            Files.delete( backupPath );
        }
    }
}
