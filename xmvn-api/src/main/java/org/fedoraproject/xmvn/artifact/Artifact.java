/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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
 * An abstract entity uniquely identified by its coordinates &ndash; group identifier, artifact identifier, extension,
 * classifier and version, with optionally associated artifact file.
 * <p>
 * Artifact objects are immutable &ndash; all methods which modify artifact return a new object and keep the original
 * unmodified.
 * 
 * @author Mikolaj Izdebski
 */
public interface Artifact
{
    /**
     * Default artifact extension, used if no explicit extension is specified.
     */
    static final String DEFAULT_EXTENSION = "jar";

    /**
     * Default artifact version, used if no explicit version is specified.
     */
    static final String DEFAULT_VERSION = "SYSTEM";

    static final String MF_KEY_GROUPID = "JavaPackages-GroupId";

    static final String MF_KEY_ARTIFACTID = "JavaPackages-ArtifactId";

    static final String MF_KEY_EXTENSION = "JavaPackages-Extension";

    static final String MF_KEY_CLASSIFIER = "JavaPackages-Classifier";

    static final String MF_KEY_VERSION = "JavaPackages-Version";

    /**
     * Get group identifier of this artifact.
     * 
     * @return artifact group identifier, never {@code null}.
     */
    String getGroupId();

    /**
     * Get artifact identifier of this artifact.
     * 
     * @return artifact identifier, never {@code null}.
     */
    String getArtifactId();

    /**
     * Get extension of this artifact.
     * 
     * @return artifact extension, never {@code null}.
     */
    String getExtension();

    /**
     * Get classifier of this artifact.
     * 
     * @return artifact classifier, never {@code null}.
     */
    String getClassifier();

    /**
     * Get version of this artifact.
     * 
     * @return artifact version, never {@code null}.
     */
    String getVersion();

    /**
     * Get local path of artifact file or {@code null} if artifact is not resolved (doesn't have associated artifact
     * file).
     * 
     * @return artifact file path, can be {@code null}.
     */
    Path getPath();

    /**
     * Set artifact version.
     * <p>
     * Since artifacts are immutable, this method returns a new object and leaves the original unmodified.
     * 
     * @param version the new artifact version to set
     * @return copy of artifact with the new version set
     */
    Artifact setVersion( String version );

    /**
     * Set artifact path.
     * <p>
     * Since artifacts are immutable, this method returns a new object and leaves the original unmodified.
     * 
     * @param path the new artifact path to set
     * @return copy of artifact with the new path set
     */
    Artifact setPath( Path path );
}
