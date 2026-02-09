/*-
 * Copyright (c) 2013-2026 Red Hat, Inc.
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

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Root element of the configuration file.
 *
 * @author Mikolaj Izdebski
 */
public class Configuration {

    public static Configuration fromXML(String xml) throws XMLException {
        return ConfigurationERM.configurationEntity.fromXML(xml);
    }

    public static Configuration readFromXML(Reader reader) throws XMLException {
        return ConfigurationERM.configurationEntity.readFromXML(reader);
    }

    public static Configuration readFromXML(Path path) throws XMLException, IOException {
        return ConfigurationERM.configurationEntity.readFromXML(path);
    }

    public String toXML() throws XMLException {
        return ConfigurationERM.configurationEntity.toXML(this);
    }

    public void writeToXML(Writer writer) throws XMLException {
        ConfigurationERM.configurationEntity.writeToXML(writer, this);
    }

    public void writeToXML(Path path) throws IOException, XMLException {
        ConfigurationERM.configurationEntity.writeToXML(path, this);
    }

    /** Field properties. */
    private Properties properties = new Properties();

    /** Field repositories. */
    private List<Repository> repositories = new ArrayList<>();

    /** This element contains basic XMvn settings. */
    private BuildSettings buildSettings;

    /** Field artifactManagement. */
    private List<PackagingRule> artifactManagement = new ArrayList<>();

    /** This element contains configuration of XMvn resolver. */
    private ResolverSettings resolverSettings;

    /** This element contains configuration of XMvn installer. */
    private InstallerSettings installerSettings;

    /**
     * Method addArtifactManagement.
     *
     * @param packagingRule a packagingRule object.
     */
    public void addArtifactManagement(PackagingRule packagingRule) {
        getArtifactManagement().add(packagingRule);
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
     * Method addRepository.
     *
     * @param repository a repository object.
     */
    public void addRepository(Repository repository) {
        getRepositories().add(repository);
    }

    /**
     * Method getArtifactManagement.
     *
     * @return List
     */
    public List<PackagingRule> getArtifactManagement() {
        return artifactManagement;
    }

    List<PackagingRule> getArtifactManagementOrNull() {
        return artifactManagement.isEmpty() ? null : artifactManagement;
    }

    /**
     * Get this element contains basic XMvn settings.
     *
     * @return BuildSettings
     */
    public BuildSettings getBuildSettings() {
        return buildSettings;
    }

    /**
     * Get this element contains configuration of XMvn installer.
     *
     * @return InstallerSettings
     */
    public InstallerSettings getInstallerSettings() {
        return installerSettings;
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
     * Method getRepositories.
     *
     * @return List
     */
    public List<Repository> getRepositories() {
        return repositories;
    }

    List<Repository> getRepositoriesOrNull() {
        return repositories.isEmpty() ? null : repositories;
    }

    /**
     * Get this element contains configuration of XMvn resolver.
     *
     * @return ResolverSettings
     */
    public ResolverSettings getResolverSettings() {
        return resolverSettings;
    }

    /**
     * Method removeArtifactManagement.
     *
     * @param packagingRule a packagingRule object.
     */
    public void removeArtifactManagement(PackagingRule packagingRule) {
        getArtifactManagement().remove(packagingRule);
    }

    /**
     * Method removeRepository.
     *
     * @param repository a repository object.
     */
    public void removeRepository(Repository repository) {
        getRepositories().remove(repository);
    }

    /**
     * Set this element configures how artifacts should be assigned to individual packages.
     *
     * @param artifactManagement a artifactManagement object.
     */
    public void setArtifactManagement(List<PackagingRule> artifactManagement) {
        this.artifactManagement = artifactManagement;
    }

    /**
     * Set this element contains basic XMvn settings.
     *
     * @param buildSettings a buildSettings object.
     */
    public void setBuildSettings(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }

    /**
     * Set this element contains configuration of XMvn installer.
     *
     * @param installerSettings a installerSettings object.
     */
    public void setInstallerSettings(InstallerSettings installerSettings) {
        this.installerSettings = installerSettings;
    }

    /**
     * Set this element lists system Java properties that should be set before XMvn build is
     * started.
     *
     * @param properties a properties object.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Set list of repositories. Repositories can be used by resolvers as source of artifacts, by
     * installers as target where artifacts should be installed, or by any other component.
     *
     * @param repositories a repositories object.
     */
    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    /**
     * Set this element contains configuration of XMvn resolver.
     *
     * @param resolverSettings a resolverSettings object.
     */
    public void setResolverSettings(ResolverSettings resolverSettings) {
        this.resolverSettings = resolverSettings;
    }
}
