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
import java.util.Properties;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Information about a single artifact.
 *
 * @author Mikolaj Izdebski
 */
public class ArtifactMetadata {

    /** Group identifier of the artifact. */
    private String groupId;

    /** Identifier of the artifact. */
    private String artifactId;

    /** Extension of artifact file. */
    private String extension;

    private static final String extensionDefault = Artifact.DEFAULT_EXTENSION;

    /** Classifier of the artifact. */
    private String classifier;

    private static final String classifierDefault = "";

    /** Artifact version. This is always upstream version, never compat version nor SYSTEM. */
    private String version;

    /** Absolute path to artifact file stored in the local file system. */
    private String path;

    /**
     * A namespace within which this artifact is stored. This usually is an identifier of software
     * collection.
     */
    private String namespace;

    private static final String namespaceDefault = "";

    /** Field properties. */
    private Properties properties = new Properties();

    /** Field compatVersions. */
    private List<String> compatVersions = new ArrayList<>();

    /** Field aliases. */
    private List<ArtifactAlias> aliases = new ArrayList<>();

    /** Field dependencies. */
    private List<Dependency> dependencies = new ArrayList<>();

    /**
     * Method addCompatVersion.
     *
     * @param string a string object.
     */
    public void addCompatVersion(String string) {
        getCompatVersions().add(string);
    }

    /**
     * Method addDependency.
     *
     * @param dependency a dependency object.
     */
    public void addDependency(Dependency dependency) {
        getDependencies().add(dependency);
    }

    /**
     * Method addProperty.
     *
     * @param key a key object.
     * @param value a value object.
     */
    public void addProperty(String key, String value) {
        getProperties().put(key, value);
    }

    /**
     * Method getAliases.
     *
     * @return List
     */
    public List<ArtifactAlias> getAliases() {
        return aliases;
    }

    List<ArtifactAlias> getAliasesOrNull() {
        return aliases.isEmpty() ? null : aliases;
    }

    /**
     * Get identifier of the artifact.
     *
     * @return String
     */
    public String getArtifactId() {
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
     * Method getCompatVersions.
     *
     * @return List
     */
    public List<String> getCompatVersions() {
        return compatVersions;
    }

    List<String> getCompatVersionsOrNull() {
        return compatVersions.isEmpty() ? null : compatVersions;
    }

    /**
     * Method getDependencies.
     *
     * @return List
     */
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    List<Dependency> getDependenciesOrNull() {
        return dependencies.isEmpty() ? null : dependencies;
    }

    /**
     * Get extension of artifact file.
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
     * Get group identifier of the artifact.
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
     * Get absolute path to artifact file stored in the local file system.
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * Method getProperties.
     *
     * @return Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get artifact version. This is always upstream version, never compat version nor SYSTEM.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Method removeCompatVersion.
     *
     * @param string a string object.
     */
    public void removeCompatVersion(String string) {
        getCompatVersions().remove(string);
    }

    /**
     * Method removeDependency.
     *
     * @param dependency a dependency object.
     */
    public void removeDependency(Dependency dependency) {
        getDependencies().remove(dependency);
    }

    /**
     * Set alternative identifiers of the artifact.
     *
     * @param aliases a aliases object.
     */
    public void setAliases(List<ArtifactAlias> aliases) {
        this.aliases = aliases;
    }

    /**
     * Set identifier of the artifact.
     *
     * @param artifactId a artifactId object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
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
     * Set compatibility versions of this artifact. If the list is empty then this artifact is not
     * considered as compatibility artifact.
     *
     * @param compatVersions a compatVersions object.
     */
    public void setCompatVersions(List<String> compatVersions) {
        this.compatVersions = compatVersions;
    }

    /**
     * Set list of artifact dependencies.
     *
     * @param dependencies a dependencies object.
     */
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Set extension of artifact file.
     *
     * @param extension a extension object.
     */
    public void setExtension(String extension) {
        this.extension = extensionDefault.equals(extension) ? null : extension;
    }

    /**
     * Set group identifier of the artifact.
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
     * Set absolute path to artifact file stored in the local file system.
     *
     * @param path a path object.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set extra properties of this artifact.
     *
     * @param properties a properties object.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Set artifact version. This is always upstream version, never compat version nor SYSTEM.
     *
     * @param version a version object.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Add an alias.
     *
     * @param alias alias to be added
     */
    public void addAlias(ArtifactAlias alias) {
        getAliases().add(alias);
    }

    /**
     * Remove an alias.
     *
     * @param alias alias to be removed
     */
    public void removeAlias(ArtifactAlias alias) {
        getAliases().remove(alias);
    }

    public Artifact toArtifact() {
        return Artifact.of(
                getGroupId(), getArtifactId(), getExtension(), getClassifier(), getVersion());
    }

    public String toString() {
        return toArtifact().toString();
    }
}
