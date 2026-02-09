/*-
 * Copyright (c) 2014-2026 Red Hat, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class JarUtilsTest {
    private Path workDir;

    @BeforeEach
    void setUp() throws Exception {
        workDir = Path.of("target/test-work");
        Files.createDirectories(workDir);
    }

    private void testManifestInjectionInto(String testJarName) throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve(testJarName);
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");
        JarUtils.injectManifest(testJar, artifact);

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(testJar))) {
            Manifest mf = jis.getManifest();
            assertThat(mf).isNotNull();

            Attributes attr = mf.getMainAttributes();
            assertThat(attr).isNotNull();

            assertThat(attr.getValue("JavaPackages-GroupId")).isEqualTo("org.apache.maven");
            assertThat(attr.getValue("JavaPackages-ArtifactId")).isEqualTo("maven-model");
            assertThat(attr.getValue("JavaPackages-Extension")).isEqualTo("xsd");
            assertThat(attr.getValue("JavaPackages-Classifier")).isEqualTo("model");
            assertThat(attr.getValue("JavaPackages-Version")).isEqualTo("2.2.1");
        }
    }

    /**
     * Test JAR if manifest injection works as expected.
     *
     * @throws Exception
     */
    @Test
    void manifestInjection() throws Exception {
        testManifestInjectionInto("manifest.jar");
    }

    /**
     * Test injecting manifest into a file without ".jar" suffix
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionNoJarSuffix() throws Exception {
        testManifestInjectionInto("foo");
    }

    /**
     * Test injecting manifest into a hidden file, i. e. starting with a "."
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionHiddenFilename() throws Exception {
        testManifestInjectionInto(".f");
    }

    /**
     * Test JAR if manifest injection works when MANIFEST.MF file appears later in the file (for
     * example produced by adding manifest to existing jar with plain zip)
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionLateManifest() throws Exception {
        Path testResource = Path.of("src/test/resources/late-manifest.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");
        JarUtils.injectManifest(testJar, artifact);

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(testJar))) {
            Manifest mf = jis.getManifest();
            assertThat(mf).isNotNull();

            Attributes attr = mf.getMainAttributes();
            assertThat(attr).isNotNull();

            assertThat(attr.getValue("JavaPackages-GroupId")).isEqualTo("org.apache.maven");
            assertThat(attr.getValue("JavaPackages-ArtifactId")).isEqualTo("maven-model");
            assertThat(attr.getValue("JavaPackages-Extension")).isEqualTo("xsd");
            assertThat(attr.getValue("JavaPackages-Classifier")).isEqualTo("model");
            assertThat(attr.getValue("JavaPackages-Version")).isEqualTo("2.2.1");
        }
    }

    /**
     * Regression test for a jar which contains an entry that can recompress with a different size,
     * which caused a mismatch in sizes.
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionRecompressionCausesSizeMismatch() throws Exception {
        Path testResource = Path.of("src/test/resources/recompression-size.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact =
                Artifact.of("org.eclipse.osgi", "osgi.compatibility.state", "1.1.0.v20180409-1212");
        JarUtils.injectManifest(testJar, artifact);

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(testJar))) {
            Manifest mf = jis.getManifest();
            assertThat(mf).isNotNull();

            Attributes attr = mf.getMainAttributes();
            assertThat(attr).isNotNull();

            assertThat(attr.getValue("JavaPackages-GroupId")).isEqualTo("org.eclipse.osgi");
            assertThat(attr.getValue("JavaPackages-ArtifactId"))
                    .isEqualTo("osgi.compatibility.state");
            assertThat(attr.getValue("JavaPackages-Version")).isEqualTo("1.1.0.v20180409-1212");
        }
    }

    /**
     * Test JAR if manifest injection works when MANIFEST.MF entry is duplicated
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionDuplicateManifest() throws Exception {
        Path testResource = Path.of("src/test/resources/duplicate-manifest.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");
        JarUtils.injectManifest(testJar, artifact);

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(testJar))) {
            Manifest mf = jis.getManifest();
            assertThat(mf).isNotNull();

            Attributes attr = mf.getMainAttributes();
            assertThat(attr).isNotNull();

            assertThat(attr.getValue("JavaPackages-GroupId")).isEqualTo("org.apache.maven");
            assertThat(attr.getValue("JavaPackages-ArtifactId")).isEqualTo("maven-model");
            assertThat(attr.getValue("JavaPackages-Extension")).isEqualTo("xsd");
            assertThat(attr.getValue("JavaPackages-Classifier")).isEqualTo("model");
            assertThat(attr.getValue("JavaPackages-Version")).isEqualTo("2.2.1");
        }
    }

    /**
     * Test JAR if manifest injection works as expected when some artifact fields have default
     * values.
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionDefaults() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest-defaults.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("xpp3", "xpp3_xpath", "jar", "", "SYSTEM");
        JarUtils.injectManifest(testJar, artifact);

        try (JarInputStream jis = new JarInputStream(Files.newInputStream(testJar))) {
            Manifest mf = jis.getManifest();
            assertThat(mf).isNotNull();

            Attributes attr = mf.getMainAttributes();
            assertThat(attr).isNotNull();

            assertThat(attr.getValue("JavaPackages-GroupId")).isEqualTo("xpp3");
            assertThat(attr.getValue("JavaPackages-ArtifactId")).isEqualTo("xpp3_xpath");
            assertThat(attr.getValue("JavaPackages-Extension")).isNull();
            assertThat(attr.getValue("JavaPackages-Classifier")).isNull();
            assertThat(attr.getValue("JavaPackages-Version")).isNull();
        }
    }

    /**
     * Test JAR if manifest injection preserves sane file perms.
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionSanePermissions() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        assumeThat(Files.getPosixFilePermissions(testJar))
                .contains(PosixFilePermission.OTHERS_READ);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");
        JarUtils.injectManifest(testJar, artifact);

        assertThat(Files.getPosixFilePermissions(testJar))
                .contains(PosixFilePermission.OTHERS_READ);
    }

    /**
     * Test if native code detection works as expected.
     *
     * @throws Exception
     */
    @Test
    void nativeCodeDetection() throws Exception {
        Path plainJarPath = Path.of("src/test/resources/example.jar");
        Path nativeCodeJarPath = Path.of("src/test/resources/native-code.jar");
        Path nativeMethodJarPath = Path.of("src/test/resources/native-method.jar");

        assertThat(JarUtils.usesNativeCode(plainJarPath)).isFalse();
        assertThat(JarUtils.containsNativeCode(plainJarPath)).isFalse();

        assertThat(JarUtils.usesNativeCode(nativeCodeJarPath)).isFalse();
        assertThat(JarUtils.containsNativeCode(nativeCodeJarPath)).isTrue();

        assertThat(JarUtils.usesNativeCode(nativeMethodJarPath)).isTrue();
        assertThat(JarUtils.containsNativeCode(nativeMethodJarPath)).isFalse();
    }

    /**
     * Test if any of utility functions throws exception when trying to access invalid JAR file.
     *
     * @throws Exception
     */
    @Test
    void invalidJar() throws Exception {
        Path testResource = Path.of("src/test/resources/invalid.jar");
        Path testJar = workDir.resolve("invalid.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("foo", "bar");
        JarUtils.injectManifest(testJar, artifact);

        assertThat(testJar).hasSameBinaryContentAs(testResource);

        assertThat(JarUtils.usesNativeCode(testResource)).isFalse();
        assertThat(JarUtils.containsNativeCode(testResource)).isFalse();
    }

    /**
     * Test that the manifest file retains the same i-node after being injected into
     *
     * @throws Exception
     */
    @Test
    void sameINode() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        long oldInode = (Long) Files.getAttribute(testJar, "unix:ino");

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");

        JarUtils.injectManifest(testJar, artifact);

        long newInode = (Long) Files.getAttribute(testJar, "unix:ino");

        assertThat(newInode).as("Different manifest I-node after injection").isEqualTo(oldInode);
    }

    /**
     * Test that the backup file created during injectManifest was deleted after a successful
     * operation
     *
     * @throws Exception
     */
    @Test
    void backupDeletion() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");

        Path backupPath = JarUtils.getBackupNameOf(testJar);
        Files.deleteIfExists(backupPath);
        JarUtils.injectManifest(testJar, artifact);
        assertThat(backupPath).doesNotExist();
    }

    /**
     * Test that the backup file created during injectManifest remains after an unsuccessful
     * operation and its content is identical to the original file
     *
     * @throws Exception
     */
    @Test
    void backupOnFailure() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = EasyMock.createMock(Artifact.class);
        EasyMock.expect(artifact.getGroupId()).andThrow(new RuntimeException("boom"));
        EasyMock.replay(artifact);

        Path backupPath = JarUtils.getBackupNameOf(testJar);
        Files.deleteIfExists(backupPath);

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> JarUtils.injectManifest(testJar, artifact))
                .withMessageContaining(backupPath.toString());
        assertThat(backupPath).hasSameBinaryContentAs(testResource);

        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);
        try (FileOutputStream os = new FileOutputStream(testJar.toFile(), true)) {
            /// Append garbage to the original file to check if the content of the backup will be
            // retained
            os.write(0);
        }

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> JarUtils.injectManifest(testJar, artifact));

        assertThat(backupPath).hasSameBinaryContentAs(testResource);

        EasyMock.verify(artifact);

        Files.delete(backupPath);
    }

    /**
     * Test that injectManifest fails if the backup file already exists
     *
     * @throws Exception
     */
    @Test
    void failWhenBachupPresent() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar = workDir.resolve("manifest.jar");
        Files.copy(
                testResource,
                testJar,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");

        Path backupPath = JarUtils.getBackupNameOf(testJar);
        Files.deleteIfExists(backupPath);
        Files.createFile(backupPath);

        try {
            assertThatExceptionOfType(Exception.class)
                    .isThrownBy(() -> JarUtils.injectManifest(testJar, artifact));
        } finally {
            Files.delete(backupPath);
        }
    }

    /**
     * Test JAR if manifest injection is reproducible, i.e. always leads to creation of
     * bit-identical JARs.
     *
     * @throws Exception
     */
    @Test
    void manifestInjectionReproducible() throws Exception {
        Path testResource = Path.of("src/test/resources/example.jar");
        Path testJar1 = workDir.resolve("reproducible1.jar");
        Path testJar2 = workDir.resolve("reproducible2.jar");
        Files.copy(
                testResource,
                testJar1,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                testResource,
                testJar2,
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);

        Artifact artifact = Artifact.of("org.apache.maven", "maven-model", "xsd", "model", "2.2.1");

        JarUtils.injectManifest(testJar1, artifact);
        Thread.sleep(3000); // ZIP time granularity is 2 seconds
        JarUtils.injectManifest(testJar2, artifact);

        assertThat(testJar1).hasSameBinaryContentAs(testJar2);
    }
}
