/*-
 * Copyright (c) 2012-2025 Red Hat, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
public final class JarUtils {
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private static final Logger LOGGER = LoggerFactory.getLogger(JarUtils.class);

    // From /usr/include/linux/elf.h
    private static final int ELFMAG0 = 0x7F;

    private static final int ELFMAG1 = 'E';

    private static final int ELFMAG2 = 'L';

    private static final int ELFMAG3 = 'F';

    private JarUtils() {
        // Avoid generating default public constructor
    }

    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file contains native
     * (architecture-dependent) code.
     *
     * <p>Currently this code only checks only for ELF binaries, but that behavior can change in
     * future.
     *
     * @return {@code true} if native code was found inside given JAR
     */
    public static boolean containsNativeCode(Path jarPath) {
        try (ZipFile jar = ZipFile.builder().setPath(jarPath).get()) {
            Iterator<ZipArchiveEntry> entries = jar.getEntries().asIterator();
            while (entries.hasNext()) {
                ZipArchiveEntry entry = entries.next();
                if (entry.isDirectory()) {
                    continue;
                }
                try (InputStream jis = jar.getInputStream(entry)) {
                    if (jis.read() == ELFMAG0
                            && jis.read() == ELFMAG1
                            && jis.read() == ELFMAG2
                            && jis.read() == ELFMAG3) {
                        LOGGER.debug("Native code found inside {}: {}", jarPath, entry.getName());
                        return true;
                    }
                }
            }

            LOGGER.trace("Native code not found inside {}", jarPath);
            return false;
        } catch (IOException e) {
            LOGGER.debug(
                    "I/O exception caught when trying to determine whether JAR contains native code: {}",
                    jarPath,
                    e);
            return false;
        }
    }

    static class NativeMethodFound extends RuntimeException {
        @Serial private static final long serialVersionUID = 1;

        final String className;

        final String methodName;

        final String methodSignature;

        NativeMethodFound(String className, String methodName, String methodSignature) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
        }
    }

    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file is using native
     * (architecture-dependent) code.
     *
     * <p>Currently this code only checks if any class file declares Java native methods, but that
     * behavior can change in future.
     *
     * @return {@code true} given JAR as found inside to use native code
     */
    public static boolean usesNativeCode(Path jarPath) {
        try (ZipFile jar = ZipFile.builder().setPath(jarPath).get()) {
            Iterator<ZipArchiveEntry> entries = jar.getEntries().asIterator();
            while (entries.hasNext()) {
                ZipArchiveEntry entry = entries.next();
                final String entryName = entry.getName();
                if (entry.isDirectory() || !entryName.endsWith(".class")) {
                    continue;
                }

                try (InputStream jis = jar.getInputStream(entry)) {
                    new ClassReader(jis)
                            .accept(
                                    new ClassVisitor(Opcodes.ASM4) {
                                        @Override
                                        public MethodVisitor visitMethod(
                                                int flags,
                                                String name,
                                                String desc,
                                                String sig,
                                                String[] exc) {
                                            if ((flags & Opcodes.ACC_NATIVE) != 0) {
                                                throw new NativeMethodFound(entryName, name, sig);
                                            }

                                            return super.visitMethod(flags, name, desc, sig, exc);
                                        }
                                    },
                                    ClassReader.SKIP_CODE);
                }
            }

            return false;
        } catch (NativeMethodFound e) {
            LOGGER.debug(
                    "Native method {}({}) found in {}: {}",
                    e.methodName,
                    e.methodSignature,
                    jarPath,
                    e.className);
            return true;
        } catch (IOException e) {
            LOGGER.debug(
                    "I/O exception caught when trying to determine whether JAR uses native code: {}",
                    jarPath,
                    e);
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static void putAttribute(
            Manifest manifest, String key, String value, String defaultValue) {
        if (defaultValue == null || !value.equals(defaultValue)) {
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue(key, value);
            LOGGER.trace("Injected field {}: {}", key, value);
        } else {
            LOGGER.trace("Not injecting field {} (it has default value \"{}\")", key, defaultValue);
        }
    }

    private static void updateManifest(Artifact artifact, Manifest mf) {
        putAttribute(mf, Artifact.MF_KEY_GROUPID, artifact.getGroupId(), null);
        putAttribute(mf, Artifact.MF_KEY_ARTIFACTID, artifact.getArtifactId(), null);
        putAttribute(
                mf, Artifact.MF_KEY_EXTENSION, artifact.getExtension(), Artifact.DEFAULT_EXTENSION);
        putAttribute(mf, Artifact.MF_KEY_CLASSIFIER, artifact.getClassifier(), "");
        putAttribute(mf, Artifact.MF_KEY_VERSION, artifact.getVersion(), Artifact.DEFAULT_VERSION);
    }

    static Path getBackupNameOf(Path p) {
        return p.getParent()
                .resolve((p.getFileName() + "-backup").replaceAll("\\.jar-backup$", "-backup.jar"));
    }

    /**
     * Inject artifact coordinates into manifest of specified JAR (or WAR, EAR, ...) file. The file
     * is modified in-place.
     *
     * @param targetJar
     * @param artifact
     */
    public static void injectManifest(Path targetJar, Artifact artifact) {
        LOGGER.trace("Trying to inject manifest to {}", artifact);
        try (ZipFile jar = ZipFile.builder().setPath(targetJar).get()) {
            if (jar.getEntry(MANIFEST_PATH) == null) {
                LOGGER.trace(
                        "Manifest injection skipped: no pre-existing manifest found to update");
                return;
            }
        } catch (IOException e) {
            LOGGER.debug("I/O exception caught when trying to read JAR: {}", targetJar);
            return;
        }
        Path backupPath = getBackupNameOf(targetJar);

        try {
            Files.copy(targetJar, backupPath, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to inject manifest: I/O error when creating backup file: " + backupPath,
                    e);
        }
        LOGGER.trace("Created backup file {}", backupPath);

        try (ZipFile jar = ZipFile.builder().setPath(backupPath).get();
                ZipArchiveOutputStream os = new ZipArchiveOutputStream(targetJar.toFile())) {
            ZipArchiveEntry jarEntry = jar.getEntry(MANIFEST_PATH);
            try (InputStream mfIs = jar.getInputStream(jarEntry)) {
                Manifest mf = new Manifest(mfIs);
                updateManifest(artifact, mf);
                // write manifest
                ZipArchiveEntry newManifestEntry = new ZipArchiveEntry(MANIFEST_PATH);
                if (jarEntry != null) {
                    newManifestEntry.setTime(jarEntry.getTime());
                }
                os.putArchiveEntry(newManifestEntry);
                mf.write(os);
                os.closeArchiveEntry();
            }
            // copy the rest of content
            jar.copyRawEntries(os, entry -> !entry.equals(jar.getEntry(MANIFEST_PATH)));
        } catch (Exception e) {
            // Re-throw exceptions that occur when processing JAR file after reading header and
            // manifest.
            throw new RuntimeException(
                    "Failed to inject manifest; backup file is available at " + backupPath, e);
        }
        LOGGER.trace("Manifest injected successfully");

        try {
            Files.delete(backupPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete backup file " + backupPath, e);
        }
        LOGGER.trace("Deleted backup file {}", backupPath);
    }
}
