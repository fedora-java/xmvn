/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Information about artifact which was built, but not installed into any package.
 *
 * @author Mikolaj Izdebski
 */
public class SkippedArtifactMetadata {

    /** Group ID of skipped artifact. */
    private String groupId;

    /** Artifact ID of skipped artifact. */
    private String artifactId;

    /** Extension of skipped artifact. */
    private String extension;

    private static final String extensionDefault = Artifact.DEFAULT_EXTENSION;

    /** Classifier of skipped artifact. */
    private String classifier;

    private static final String classifierDefault = "";

    /**
     * Get artifact ID of skipped artifact.
     *
     * @return String
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get classifier of skipped artifact.
     *
     * @return String
     */
    public String getClassifier() {
        return classifier != null ? classifier : classifierDefault;
    }

    String getClassifierOrNull() {
        return classifier;
    }

    /**
     * Get extension of skipped artifact.
     *
     * @return String
     */
    public String getExtension() {
        return extension != null ? extension : extensionDefault;
    }

    String getExtensionOrNull() {
        return extension;
    }

    /**
     * Get group ID of skipped artifact.
     *
     * @return String
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set artifact ID of skipped artifact.
     *
     * @param artifactId a artifactId object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set classifier of skipped artifact.
     *
     * @param classifier a classifier object.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifierDefault.equals(classifier) ? null : classifier;
    }

    /**
     * Set extension of skipped artifact.
     *
     * @param extension a extension object.
     */
    public void setExtension(String extension) {
        this.extension = extensionDefault.equals(extension) ? null : extension;
    }

    /**
     * Set group ID of skipped artifact.
     *
     * @param groupId a groupId object.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
