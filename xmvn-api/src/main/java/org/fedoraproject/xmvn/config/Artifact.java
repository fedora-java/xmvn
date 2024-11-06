/*-
 * Copyright (c) 2013-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config;

/**
 * Identifier of Maven artifact.
 *
 * @author Mikolaj Izdebski
 */
public class Artifact {

    /** Group ID of the artifact. */
    private String groupId;

    private static final String groupIdDefault = "";

    /** Artifact ID of the artifact. */
    private String artifactId;

    private static final String artifactIdDefault = "";

    /** Version of the artifact. */
    private String version;

    private static final String versionDefault = "";

    /** Classifier of the artifact. */
    private String classifier;

    private static final String classifierDefault = "";

    /** Maven stereotype of the artifact. */
    private String stereotype;

    private static final String stereotypeDefault = "";

    /** Extension of the artifact. */
    private String extension;

    private static final String extensionDefault = "";

    /**
     * Get artifact ID of the artifact.
     *
     * @return String
     */
    public String getArtifactId() {
        return artifactId != null ? artifactId : artifactIdDefault;
    }

    String getArtifactIdOrNull() {
        return artifactId;
    }

    /**
     * Get classifier of the artifact.
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
     * Get extension of the artifact.
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
     * Get group ID of the artifact.
     *
     * @return String
     */
    public String getGroupId() {
        return groupId != null ? groupId : groupIdDefault;
    }

    String getGroupIdOrNull() {
        return groupId;
    }

    /**
     * Get maven stereotype of the artifact.
     *
     * @return String
     */
    public String getStereotype() {
        return stereotype != null ? stereotype : stereotypeDefault;
    }

    String getStereotypeOrNull() {
        return stereotype;
    }

    /**
     * Get version of the artifact.
     *
     * @return String
     */
    public String getVersion() {
        return version != null ? version : versionDefault;
    }

    String getVersionOrNull() {
        return version;
    }

    /**
     * Set artifact ID of the artifact.
     *
     * @param artifactId a artifactId object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactIdDefault.equals(artifactId) ? null : artifactId;
    }

    /**
     * Set classifier of the artifact.
     *
     * @param classifier a classifier object.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifierDefault.equals(classifier) ? null : classifier;
    }

    /**
     * Set extension of the artifact.
     *
     * @param extension a extension object.
     */
    public void setExtension(String extension) {
        this.extension = extensionDefault.equals(extension) ? null : extension;
    }

    /**
     * Set group ID of the artifact.
     *
     * @param groupId a groupId object.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupIdDefault.equals(groupId) ? null : groupId;
    }

    /**
     * Set maven stereotype of the artifact.
     *
     * @param stereotype a stereotype object.
     */
    public void setStereotype(String stereotype) {
        this.stereotype = stereotypeDefault.equals(stereotype) ? null : stereotype;
    }

    /**
     * Set version of the artifact.
     *
     * @param version a version object.
     */
    public void setVersion(String version) {
        this.version = versionDefault.equals(version) ? null : version;
    }
}
