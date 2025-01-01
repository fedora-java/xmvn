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
package org.fedoraproject.xmvn.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Identification of Maven Artifact.
 *
 * @author Mikolaj Izdebski
 */
public class PackagingRule {

    /** Pattern specifying one or more Maven artifacts. */
    private Artifact artifactGlob;

    /** Name of binary package into which artifacts are assigned. */
    private String targetPackage;

    /** ID of repository into which artifacts are installed. */
    private String targetRepository;

    /** Field files. */
    private List<String> files = new ArrayList<>();

    /** Field versions. */
    private List<String> versions = new ArrayList<>();

    /** Field aliases. */
    private List<Artifact> aliases = new ArrayList<>();

    /**
     * Whether this rule is optional. Non-optional rules cause uild failure if they are not matched.
     */
    private Boolean optional;

    private static final Boolean optionalDefault = false;

    /**
     * Whether any reactor artifact matches artifact glob pattern or this rule. Non-optional rules
     * cause build failure if hey are not matched.
     */
    private Boolean matched = false;

    /**
     * Method addFile.
     *
     * @param string a string object.
     */
    public void addFile(String string) {
        getFiles().add(string);
    }

    /**
     * Method addVersion.
     *
     * @param string a string object.
     */
    public void addVersion(String string) {
        getVersions().add(string);
    }

    /**
     * Method getAliases.
     *
     * @return List
     */
    public List<Artifact> getAliases() {
        return aliases;
    }

    List<Artifact> getAliasesOrNull() {
        return aliases.isEmpty() ? null : aliases;
    }

    /**
     * Get pattern specifying one or more Maven artifacts.
     *
     * @return Artifact
     */
    public Artifact getArtifactGlob() {
        return artifactGlob;
    }

    /**
     * Method getFiles.
     *
     * @return List
     */
    public List<String> getFiles() {
        return files;
    }

    List<String> getFilesOrNull() {
        return files.isEmpty() ? null : files;
    }

    /**
     * Get name of binary package into which artifacts are assigned.
     *
     * @return String
     */
    public String getTargetPackage() {
        return targetPackage;
    }

    /**
     * Get iD of repository into which artifacts are installed.
     *
     * @return String
     */
    public String getTargetRepository() {
        return targetRepository;
    }

    /**
     * Method getVersions.
     *
     * @return List
     */
    public List<String> getVersions() {
        return versions;
    }

    List<String> getVersionsOrNull() {
        return versions.isEmpty() ? null : versions;
    }

    /**
     * Get whether any reactor artifact matches artifact glob pattern or this rule. Non-optional
     * rules cause build failure if hey are not matched.
     *
     * @return Boolean
     */
    public Boolean isMatched() {
        return matched;
    }

    /**
     * Get whether this rule is optional. Non-optional rules cause uild failure if they are not
     * matched.
     *
     * @return Boolean
     */
    public Boolean isOptional() {
        return optional != null ? optional : optionalDefault;
    }

    Boolean getOptionalOrNull() {
        return optional;
    }

    /**
     * Method removeFile.
     *
     * @param string a string object.
     */
    public void removeFile(String string) {
        getFiles().remove(string);
    }

    /**
     * Method removeVersion.
     *
     * @param string a string object.
     */
    public void removeVersion(String string) {
        getVersions().remove(string);
    }

    /**
     * Set alternative identifiers of artifacts.
     *
     * @param aliases a aliases object.
     */
    public void setAliases(List<Artifact> aliases) {
        this.aliases = aliases;
    }

    /**
     * Set pattern specifying one or more Maven artifacts.
     *
     * @param artifactGlob a artifactGlob object.
     */
    public void setArtifactGlob(Artifact artifactGlob) {
        this.artifactGlob = artifactGlob;
    }

    /**
     * Set files holding the artifact.
     *
     * @param files a files object.
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }

    /**
     * Set whether any reactor artifact matches artifact glob pattern or this rule. Non-optional
     * rules cause build failure if hey are not matched.
     *
     * @param matched a matched object.
     */
    public void setMatched(Boolean matched) {
        this.matched = matched;
    }

    /**
     * Set whether this rule is optional. Non-optional rules cause uild failure if they are not
     * matched.
     *
     * @param optional a optional object.
     */
    public void setOptional(Boolean optional) {
        this.optional = optionalDefault.equals(optional) ? null : optional;
    }

    /**
     * Set name of binary package into which artifacts are assigned.
     *
     * @param targetPackage a targetPackage object.
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Set iD of repository into which artifacts are installed.
     *
     * @param targetRepository a targetRepository object.
     */
    public void setTargetRepository(String targetRepository) {
        this.targetRepository = targetRepository;
    }

    /**
     * Set compatibility versions of the artifact.
     *
     * @param versions a versions object.
     */
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    /**
     * Add an alias.
     *
     * @param artifact alias to be added
     */
    public void addAlias(Artifact artifact) {
        getAliases().add(artifact);
    }

    /**
     * Remove an alias.
     *
     * @param artifact alias to be removed
     */
    public void removeAlias(Artifact artifact) {
        getAliases().remove(artifact);
    }
}
