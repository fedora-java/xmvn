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
package org.fedoraproject.xmvn.metadata;

import java.util.ArrayList;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Description of dependency artifact.
 *
 * @author Mikolaj Izdebski
 */
public class Dependency {

    /** Group ID of the dependency artifact. */
    private String groupId;

    /** Artifact ID of the dependency artifact. */
    private String artifactId;

    /** Extension of the dependency artifact. */
    private String extension;

    private static final String extensionDefault = Artifact.DEFAULT_EXTENSION;

    /** Classifier of the dependency artifact. */
    private String classifier;

    private static final String classifierDefault = "";

    /**
     * Version of the dependency artifact as defined in the main artifact descriptor. This may be a
     * version range as supported by Maven.
     */
    private String requestedVersion;

    private static final String requestedVersionDefault = Artifact.DEFAULT_VERSION;

    /**
     * Version of the dependency artifact, as resolved during build. Absence of this field indicates
     * a dependency on default artifact version.
     */
    private String resolvedVersion;

    private static final String resolvedVersionDefault = Artifact.DEFAULT_VERSION;

    /**
     * A namespace within which this artifact is stored. This usually is an identifier of software
     * collection.
     */
    private String namespace;

    private static final String namespaceDefault = "";

    /** Specifies whether given dependency is optional or not. */
    private Boolean optional;

    private static final Boolean optionalDefault = false;

    /** Field exclusions. */
    private List<DependencyExclusion> exclusions = new ArrayList<>();

    /**
     * Method addExclusion.
     *
     * @param dependencyExclusion a dependencyExclusion object.
     */
    public void addExclusion(DependencyExclusion dependencyExclusion) {
        getExclusions().add(dependencyExclusion);
    }

    /**
     * Get artifact ID of the dependency artifact.
     *
     * @return String
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get classifier of the dependency artifact.
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
     * Method getExclusions.
     *
     * @return List
     */
    public List<DependencyExclusion> getExclusions() {
        return exclusions;
    }

    List<DependencyExclusion> getExclusionsOrNull() {
        return exclusions.isEmpty() ? null : exclusions;
    }

    /**
     * Get extension of the dependency artifact.
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
     * Get group ID of the dependency artifact.
     *
     * @return String
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get a namespace within which this artifact is stored. This usually is an identifier of
     * software collection.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace != null ? namespace : namespaceDefault;
    }

    String getNamespaceOrNull() {
        return namespace;
    }

    /**
     * Get version of the dependency artifact as defined in the main artifact descriptor. This may
     * be a version range as supported by Maven.
     *
     * @return String
     */
    public String getRequestedVersion() {
        return requestedVersion != null ? requestedVersion : requestedVersionDefault;
    }

    String getRequestedVersionOrNull() {
        return requestedVersion;
    }

    /**
     * Get version of the dependency artifact, as resolved during build. Absence of this field
     * indicates a dependency on default artifact version.
     *
     * @return String
     */
    public String getResolvedVersion() {
        return resolvedVersion != null ? resolvedVersion : resolvedVersionDefault;
    }

    String getResolvedVersionOrNull() {
        return resolvedVersion;
    }

    /**
     * Get specifies whether given dependency is optional or not.
     *
     * @return boolean
     */
    public Boolean isOptional() {
        return optional != null ? optional : optionalDefault;
    }

    Boolean getOptionalOrNull() {
        return optional;
    }

    /**
     * Method removeExclusion.
     *
     * @param dependencyExclusion a dependencyExclusion object.
     */
    public void removeExclusion(DependencyExclusion dependencyExclusion) {
        getExclusions().remove(dependencyExclusion);
    }

    /**
     * Set artifact ID of the dependency artifact.
     *
     * @param artifactId a artifactId object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set classifier of the dependency artifact.
     *
     * @param classifier a classifier object.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifierDefault.equals(classifier) ? null : classifier;
    }

    /**
     * Set list of dependency exclusions.
     *
     * @param exclusions a exclusions object.
     */
    public void setExclusions(List<DependencyExclusion> exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Set extension of the dependency artifact.
     *
     * @param extension a extension object.
     */
    public void setExtension(String extension) {
        this.extension = extensionDefault.equals(extension) ? null : extension;
    }

    /**
     * Set group ID of the dependency artifact.
     *
     * @param groupId a groupId object.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Set a namespace within which this artifact is stored. This usually is an identifier of
     * software collection.
     *
     * @param namespace a namespace object.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespaceDefault.equals(namespace) ? null : namespace;
    }

    /**
     * Set specifies whether given dependency is optional or not.
     *
     * @param optional a optional object.
     */
    public void setOptional(Boolean optional) {
        this.optional = optionalDefault.equals(optional) ? null : optional;
    }

    /**
     * Set version of the dependency artifact as defined in the main artifact descriptor. This may
     * be a version range as supported by Maven.
     *
     * @param requestedVersion a requestedVersion object.
     */
    public void setRequestedVersion(String requestedVersion) {
        this.requestedVersion =
                requestedVersionDefault.equals(requestedVersion) ? null : requestedVersion;
    }

    /**
     * Set version of the dependency artifact, as resolved during build. Absence of this field
     * indicates a dependency on default artifact version.
     *
     * @param resolvedVersion a resolvedVersion object.
     */
    public void setResolvedVersion(String resolvedVersion) {
        this.resolvedVersion =
                resolvedVersionDefault.equals(resolvedVersion) ? null : resolvedVersion;
    }

    public Artifact toArtifact() {
        return Artifact.of(
                getGroupId(),
                getArtifactId(),
                getExtension(),
                getClassifier(),
                getRequestedVersion());
    }

    public String toString() {
        return toArtifact().toString();
    }
}
