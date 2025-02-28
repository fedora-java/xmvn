/*-
 * Copyright (c) 2013-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataResult;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactVisitor implements FileVisitor<Path> {
    private final Logger logger;

    private final Set<String> types = new LinkedHashSet<>();

    private final List<MetadataResult> metadata;

    private boolean followSymlinks;

    private boolean dryRun;

    private int failureCount;

    public ArtifactVisitor(Logger logger, List<MetadataResult> metadata) {
        this.logger = logger;
        this.metadata = metadata;
    }

    public void setTypes(Collection<String> types) {
        this.types.addAll(types);
    }

    public void setFollowSymlinks(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public int getFailureCount() {
        return failureCount;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
            throws IOException {
        if (Files.isSymbolicLink(path) && !followSymlinks) {
            logger.debug("Skipping symlink to directory: {}", path);
            return FileVisitResult.SKIP_SUBTREE;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (!Files.isRegularFile(path)) {
            logger.debug("Skipping {}: not a regular file", path);
            return FileVisitResult.CONTINUE;
        }

        String fileName = path.getFileName().toString();

        for (String type : types) {
            if (fileName.endsWith("." + type)) {
                substituteArtifact(path, type);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        logger.warn("Failed to access file {}", path);
        return FileVisitResult.CONTINUE;
    }

    private Artifact getArtifactFromManifest(Path path) throws IOException {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            Manifest mf = jarFile.getManifest();
            if (mf == null) {
                return null;
            }

            String groupId = mf.getMainAttributes().getValue(Artifact.MF_KEY_GROUPID);
            String artifactId = mf.getMainAttributes().getValue(Artifact.MF_KEY_ARTIFACTID);
            String extension = mf.getMainAttributes().getValue(Artifact.MF_KEY_EXTENSION);
            String classifier = mf.getMainAttributes().getValue(Artifact.MF_KEY_CLASSIFIER);
            String version = mf.getMainAttributes().getValue(Artifact.MF_KEY_VERSION);

            if (groupId == null || artifactId == null) {
                return null;
            }

            return Artifact.of(groupId, artifactId, extension, classifier, version);
        }
    }

    private Artifact getArtifactFromPomProperties(Path path, String extension) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties")) {
                    Properties properties = new Properties();
                    properties.load(zis);

                    String groupId = properties.getProperty("groupId");
                    String artifactId = properties.getProperty("artifactId");
                    String version = properties.getProperty("version");
                    return Artifact.of(groupId, artifactId, extension, version);
                }
            }

            return null;
        }
    }

    private Artifact readArtifactDefinition(Path path, String extension) {
        try {
            Artifact artifact = getArtifactFromManifest(path);
            if (artifact != null) {
                return artifact;
            }

            artifact = getArtifactFromPomProperties(path, extension);
            if (artifact != null) {
                return artifact;
            }

            return null;
        } catch (IOException e) {
            logger.error("Failed to get artifact definition from file {}", path, e);
            return null;
        }
    }

    private void substituteArtifact(Path path, String type) throws IOException {
        Artifact artifact = readArtifactDefinition(path, type);
        if (artifact == null) {
            logger.info("Skipping file {}: No artifact definition found", path);
            failureCount++;
            return;
        }

        ArtifactMetadata metadata = resolveMetadata(artifact);
        if (metadata == null) {
            logger.warn("Skipping file {}: Artifact {} not found in repository", path, artifact);
            failureCount++;
            return;
        }

        Path artifactPath = Path.of(metadata.getPath());

        if (!dryRun) {
            Files.delete(path);
            Files.createSymbolicLink(path, artifactPath);
        }

        logger.info("Linked {} to {}", path, artifactPath);
    }

    private ArtifactMetadata resolveMetadata(Artifact artifact) {
        List<Artifact> versionedArtifacts = Arrays.asList(artifact, artifact.setVersion(null));

        for (MetadataResult metadataResult : metadata) {
            for (Artifact versionedArtifact : versionedArtifacts) {
                ArtifactMetadata metadata = metadataResult.getMetadataFor(versionedArtifact);
                if (metadata != null) {
                    return metadata;
                }
            }
        }

        return null;
    }
}
