/*-
 * Copyright (c) 2016-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata;

import java.nio.file.Path;
import java.util.Map;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Provides access to results of metadata resolution.
 *
 * @author Mikolaj Izdebski
 */
public interface MetadataResult {
    /**
     * Lookup metadata for specified artifact.
     *
     * @param artifact artifact to lookup metadata for
     * @return metadata for specified artifact, or {@code null} if this result doesn't contain
     *     metadata for specified artifact
     */
    ArtifactMetadata getMetadataFor(Artifact artifact);

    /**
     * Return a read-only {@link Map} that contains every found metadata file {@link Path}s and a
     * corresponding {@link PackageMetadata}.
     *
     * @return package metadata map, never {@code null}
     */
    Map<Path, PackageMetadata> getPackageMetadataMap();
}
