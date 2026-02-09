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
package org.fedoraproject.xmvn.metadata.impl;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * @author Mikolaj Izdebski
 */
class DefaultMetadataResult implements MetadataResult {
    private final Logger logger;

    private final Map<Path, PackageMetadata> packageMetadataMap;

    private final Map<Artifact, ArtifactMetadata> artifactMap = new LinkedHashMap<>();

    public DefaultMetadataResult(
            Logger logger,
            Map<Path, PackageMetadata> packageMetadataMap,
            boolean ignoreDuplicates) {
        this.logger = logger;
        this.packageMetadataMap = packageMetadataMap;

        for (PackageMetadata metadata : packageMetadataMap.values()) {
            for (ArtifactMetadata installedArtifact : metadata.getArtifacts()) {
                processArtifactMetadata(installedArtifact, ignoreDuplicates);
            }
        }
    }

    private void processArtifactMetadata(ArtifactMetadata metadata, boolean ignoreDuplicates) {
        Artifact baseArtifact = metadata.toArtifact();

        List<String> versions = metadata.getCompatVersions();
        if (versions.isEmpty()) {
            versions = List.of(Artifact.DEFAULT_VERSION);
        }

        Set<Artifact> artifactSet = new LinkedHashSet<>();

        for (String version : versions) {
            artifactSet.add(baseArtifact.withVersion(version));
        }

        for (ArtifactAlias alias : metadata.getAliases()) {
            Artifact aliasArtifact =
                    Artifact.of(
                            alias.getGroupId(),
                            alias.getArtifactId(),
                            alias.getExtension(),
                            alias.getClassifier(),
                            metadata.getVersion());

            for (String version : versions) {
                artifactSet.add(aliasArtifact.withVersion(version));
            }
        }

        Set<Artifact> duplicateArtifacts = new LinkedHashSet<>();

        for (Artifact artifact : artifactSet) {
            if (duplicateArtifacts.contains(artifact)) {
                logger.debug(
                        "Ignoring metadata for artifact {} as it was already excluded", artifact);
                continue;
            }

            ArtifactMetadata otherMetadata = artifactMap.get(artifact);

            if (otherMetadata == null) {
                artifactMap.put(artifact, metadata);
                continue;
            }

            duplicateArtifacts.add(artifact);

            if (ignoreDuplicates) {
                artifactMap.remove(artifact);
                logger.warn(
                        "Ignoring metadata for artifact {} as it has duplicate metadata", artifact);
                continue;
            }

            logger.warn("Duplicate metadata for artifact {}", artifact);

            if (otherMetadata.getNamespace().isEmpty() || !metadata.getNamespace().isEmpty()) {
                artifactMap.put(artifact, metadata);
            }
        }
    }

    @Override
    public ArtifactMetadata getMetadataFor(Artifact artifact) {
        return artifactMap.get(artifact);
    }

    @Override
    public Map<Path, PackageMetadata> getPackageMetadataMap() {
        return Collections.unmodifiableMap(packageMetadataMap);
    }
}
