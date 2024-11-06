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

import java.util.ArrayList;
import java.util.List;

/**
 * XMvn settings related to resolution of artifacts.
 *
 * @author Mikolaj Izdebski
 */
public class ResolverSettings {

    /**
     * Whether detailed debugging information about artifact resolution process should be logged.
     */
    private Boolean debug = false;

    /** Field localRepositories. */
    private List<String> localRepositories = new ArrayList<>();

    /** Field metadataRepositories. */
    private List<String> metadataRepositories = new ArrayList<>();

    /**
     * Whether XMvn should refuse to resolve artifact that have more than a single piece of
     * metadata.
     */
    private Boolean ignoreDuplicateMetadata = false;

    /** Field prefixes. */
    private List<String> prefixes = new ArrayList<>();

    /** Field blacklist. */
    private List<Artifact> blacklist = new ArrayList<>();

    /**
     * Method addBlacklist.
     *
     * @param artifact a artifact object.
     */
    public void addBlacklist(Artifact artifact) {
        getBlacklist().add(artifact);
    }

    /**
     * Method addLocalRepository.
     *
     * @param string a string object.
     */
    public void addLocalRepository(String string) {
        getLocalRepositories().add(string);
    }

    /**
     * Method addMetadataRepository.
     *
     * @param string a string object.
     */
    public void addMetadataRepository(String string) {
        getMetadataRepositories().add(string);
    }

    /**
     * Method addPrefix.
     *
     * @param string a string object.
     */
    public void addPrefix(String string) {
        getPrefixes().add(string);
    }

    /**
     * Method getBlacklist.
     *
     * @return List
     */
    public List<Artifact> getBlacklist() {
        return blacklist;
    }

    List<Artifact> getBlacklistOrNull() {
        return blacklist.isEmpty() ? null : blacklist;
    }

    /**
     * Method getLocalRepositories.
     *
     * @return List
     */
    public List<String> getLocalRepositories() {
        return localRepositories;
    }

    List<String> getLocalRepositoriesOrNull() {
        return localRepositories.isEmpty() ? null : localRepositories;
    }

    /**
     * Method getMetadataRepositories.
     *
     * @return List
     */
    public List<String> getMetadataRepositories() {
        return metadataRepositories;
    }

    List<String> getMetadataRepositoriesOrNull() {
        return metadataRepositories.isEmpty() ? null : metadataRepositories;
    }

    /**
     * Method getPrefixes.
     *
     * @return List
     */
    public List<String> getPrefixes() {
        return prefixes;
    }

    List<String> getPrefixesOrNull() {
        return prefixes.isEmpty() ? null : prefixes;
    }

    /**
     * Get whether detailed debugging information about artifact resolution process should be
     * logged.
     *
     * @return Boolean
     */
    public Boolean isDebug() {
        return debug;
    }

    /**
     * Get whether XMvn should refuse to resolve artifact that have more than a single piece of
     * metadata.
     *
     * @return Boolean
     */
    public Boolean isIgnoreDuplicateMetadata() {
        return ignoreDuplicateMetadata;
    }

    /**
     * Method removeBlacklist.
     *
     * @param artifact a artifact object.
     */
    public void removeBlacklist(Artifact artifact) {
        getBlacklist().remove(artifact);
    }

    /**
     * Method removeLocalRepository.
     *
     * @param string a string object.
     */
    public void removeLocalRepository(String string) {
        getLocalRepositories().remove(string);
    }

    /**
     * Method removeMetadataRepository.
     *
     * @param string a string object.
     */
    public void removeMetadataRepository(String string) {
        getMetadataRepositories().remove(string);
    }

    /**
     * Method removePrefix.
     *
     * @param string a string object.
     */
    public void removePrefix(String string) {
        getPrefixes().remove(string);
    }

    /**
     * Set list of blacklisted artifacts which will not be resolved.
     *
     * @param blacklist a blacklist object.
     */
    public void setBlacklist(List<Artifact> blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * Set whether detailed debugging information about artifact resolution process should be
     * logged.
     *
     * @param debug a debug object.
     */
    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    /**
     * Set whether XMvn should refuse to resolve artifact that have more than a single piece of
     * metadata.
     *
     * @param ignoreDuplicateMetadata a ignoreDuplicateMetadata object.
     */
    public void setIgnoreDuplicateMetadata(Boolean ignoreDuplicateMetadata) {
        this.ignoreDuplicateMetadata = ignoreDuplicateMetadata;
    }

    /**
     * Set list of local repositories where XMvn will look for artifacts.
     *
     * @param localRepositories a localRepositories object.
     */
    public void setLocalRepositories(List<String> localRepositories) {
        this.localRepositories = localRepositories;
    }

    /**
     * Set list of repositories where XMvn will look for metadata files.
     *
     * @param metadataRepositories a metadataRepositories object.
     */
    public void setMetadataRepositories(List<String> metadataRepositories) {
        this.metadataRepositories = metadataRepositories;
    }

    /**
     * Set list of prefixes that XMvn resolver will prepend to system repositories.
     *
     * @param prefixes a prefixes object.
     */
    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }
}
