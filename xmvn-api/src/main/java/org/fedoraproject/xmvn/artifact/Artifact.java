/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.artifact;

import java.nio.file.Path;

/**
 * Represents an abstract software artifact uniquely identified by its coordinates, which include
 * the group identifier, artifact identifier, extension, classifier, and version. An artifact may
 * optionally be associated with a local file path.
 *
 * <p>Artifact instances are immutable. Any modification results in a new object, leaving the
 * original unchanged.
 *
 * <p>This interface defines methods for retrieving artifact metadata and creating modified copies
 * with updated properties.
 *
 * @author Mikolaj Izdebski
 */
public interface Artifact {
    /** Default extension used when no explicit extension is specified. */
    String DEFAULT_EXTENSION = "jar";

    /** Default version used when no explicit version is specified. */
    String DEFAULT_VERSION = "SYSTEM";

    /** Manifest key for storing the group identifier. */
    String MF_KEY_GROUPID = "JavaPackages-GroupId";

    /** Manifest key for storing the artifact identifier. */
    String MF_KEY_ARTIFACTID = "JavaPackages-ArtifactId";

    /** Manifest key for storing the artifact extension. */
    String MF_KEY_EXTENSION = "JavaPackages-Extension";

    /** Manifest key for storing the artifact classifier. */
    String MF_KEY_CLASSIFIER = "JavaPackages-Classifier";

    /** Manifest key for storing the artifact version. */
    String MF_KEY_VERSION = "JavaPackages-Version";

    /**
     * Returns the group identifier of this artifact.
     *
     * @return the group identifier, never {@code null}
     */
    String getGroupId();

    /**
     * Returns the artifact identifier of this artifact.
     *
     * @return the artifact identifier, never {@code null}
     */
    String getArtifactId();

    /**
     * Returns the extension of this artifact.
     *
     * @return the artifact extension, never {@code null}
     */
    String getExtension();

    /**
     * Returns the classifier of this artifact.
     *
     * @return the artifact classifier, never {@code null}
     */
    String getClassifier();

    /**
     * Returns the version of this artifact.
     *
     * @return the artifact version, never {@code null}
     */
    String getVersion();

    /**
     * Returns the local file path of the artifact, or {@code null} if the artifact is unresolved
     * (i.e., has no associated file).
     *
     * @return the artifact file path, or {@code null} if not resolved
     */
    Path getPath();

    /**
     * Creates a new artifact instance with the specified version.
     *
     * @param version the new version to set
     * @return a new artifact instance with the updated version
     */
    Artifact withVersion(String version);

    /**
     * @deprecated Use {@link #withVersion(String)} instead.
     */
    @Deprecated
    default Artifact setVersion(String version) {
        return withVersion(version);
    }

    /**
     * Creates a new artifact instance with the specified file path.
     *
     * @param path the new file path to set
     * @return a new artifact instance with the updated path
     */
    Artifact withPath(Path path);

    /**
     * @deprecated Use {@link #withPath(Path)} instead.
     */
    @Deprecated
    default Artifact setPath(Path path) {
        return withPath(path);
    }

    /**
     * Factory method to create an artifact instance from a coordinate string.
     *
     * @param coords the artifact coordinates
     * @return a new artifact instance
     */
    static Artifact of(String coords) {
        return new ArtifactImpl(coords);
    }

    /**
     * Factory method to create an artifact with a group ID and artifact ID.
     *
     * @param groupId the group identifier
     * @param artifactId the artifact identifier
     * @return a new artifact instance
     */
    static Artifact of(String groupId, String artifactId) {
        return new ArtifactImpl(groupId, artifactId);
    }

    /**
     * Factory method to create an artifact with a group ID, artifact ID, and version.
     *
     * @param groupId the group identifier
     * @param artifactId the artifact identifier
     * @param version the artifact version
     * @return a new artifact instance
     */
    static Artifact of(String groupId, String artifactId, String version) {
        return new ArtifactImpl(groupId, artifactId, version);
    }

    /**
     * Factory method to create an artifact with a group ID, artifact ID, extension, and version.
     *
     * @param groupId the group identifier
     * @param artifactId the artifact identifier
     * @param extension the artifact extension
     * @param version the artifact version
     * @return a new artifact instance
     */
    static Artifact of(String groupId, String artifactId, String extension, String version) {
        return new ArtifactImpl(groupId, artifactId, extension, version);
    }

    /**
     * Factory method to create an artifact with full coordinates.
     *
     * @param groupId the group identifier
     * @param artifactId the artifact identifier
     * @param extension the artifact extension
     * @param classifier the artifact classifier
     * @param version the artifact version
     * @return a new artifact instance
     */
    static Artifact of(
            String groupId,
            String artifactId,
            String extension,
            String classifier,
            String version) {
        return new ArtifactImpl(groupId, artifactId, extension, classifier, version);
    }

    /**
     * Factory method to create an artifact with full coordinates and a local file path.
     *
     * @param groupId the group identifier
     * @param artifactId the artifact identifier
     * @param extension the artifact extension
     * @param classifier the artifact classifier
     * @param version the artifact version
     * @param path the local file path
     * @return a new artifact instance
     */
    static Artifact of(
            String groupId,
            String artifactId,
            String extension,
            String classifier,
            String version,
            Path path) {
        return new ArtifactImpl(groupId, artifactId, extension, classifier, version, path);
    }
}
