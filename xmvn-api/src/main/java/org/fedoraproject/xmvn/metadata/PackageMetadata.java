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

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Root element of the metadata file.
 *
 * @author Mikolaj Izdebski
 */
public class PackageMetadata {

    public static PackageMetadata fromXML(String xml) throws XMLException {
        return PackageMetadataERM.metadataEntity.fromXML(xml);
    }

    public static PackageMetadata readFromXML(Reader reader) throws XMLException {
        return PackageMetadataERM.metadataEntity.readFromXML(reader);
    }

    public static PackageMetadata readFromXML(Path path) throws XMLException, IOException {
        return PackageMetadataERM.metadataEntity.readFromXML(path);
    }

    public String toXML() throws XMLException {
        return PackageMetadataERM.metadataEntity.toXML(this);
    }

    public void writeToXML(Writer writer) throws XMLException {
        PackageMetadataERM.metadataEntity.writeToXML(writer, this);
    }

    public void writeToXML(Path path) throws IOException, XMLException {
        PackageMetadataERM.metadataEntity.writeToXML(path, this);
    }

    /** Deprecated, unused. */
    private String uuid;

    private static final String uuidDefault = "";

    /** Field properties. */
    private Properties properties = new Properties();

    /** Field artifacts. */
    private List<ArtifactMetadata> artifacts = new ArrayList<>();

    /** Field skippedArtifacts. */
    private List<SkippedArtifactMetadata> skippedArtifacts = new ArrayList<>();

    /**
     * Method addArtifact.
     *
     * @param artifactMetadata a artifactMetadata object.
     */
    public void addArtifact(ArtifactMetadata artifactMetadata) {
        getArtifacts().add(artifactMetadata);
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
     * Method addSkippedArtifact.
     *
     * @param skippedArtifactMetadata a skippedArtifactMetadata object.
     */
    public void addSkippedArtifact(SkippedArtifactMetadata skippedArtifactMetadata) {
        getSkippedArtifacts().add(skippedArtifactMetadata);
    }

    /**
     * Method getArtifacts.
     *
     * @return List
     */
    public List<ArtifactMetadata> getArtifacts() {
        return artifacts;
    }

    List<ArtifactMetadata> getArtifactsOrNull() {
        return artifacts.isEmpty() ? null : artifacts;
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
     * Method getSkippedArtifacts.
     *
     * @return List
     */
    public List<SkippedArtifactMetadata> getSkippedArtifacts() {
        return skippedArtifacts;
    }

    List<SkippedArtifactMetadata> getSkippedArtifactsOrNull() {
        return skippedArtifacts.isEmpty() ? null : skippedArtifacts;
    }

    /**
     * Get deprecated, unused.
     *
     * @return String
     */
    public String getUuid() {
        return uuid != null ? uuid : uuidDefault;
    }

    String getUuidOrNull() {
        return uuid;
    }

    /**
     * Method removeArtifact.
     *
     * @param artifactMetadata a artifactMetadata object.
     */
    public void removeArtifact(ArtifactMetadata artifactMetadata) {
        getArtifacts().remove(artifactMetadata);
    }

    /**
     * Method removeSkippedArtifact.
     *
     * @param skippedArtifactMetadata a skippedArtifactMetadata object.
     */
    public void removeSkippedArtifact(SkippedArtifactMetadata skippedArtifactMetadata) {
        getSkippedArtifacts().remove(skippedArtifactMetadata);
    }

    /**
     * Set list of installed artifacts described by this piece of metadata.
     *
     * @param artifacts a artifacts object.
     */
    public void setArtifacts(List<ArtifactMetadata> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * Set properties of this piece of metadata.
     *
     * @param properties a properties object.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Set list of artifacts built but not installed in any package. Useful for detecting broken
     * package dependencies.
     *
     * @param skippedArtifacts a skippedArtifacts object.
     */
    public void setSkippedArtifacts(List<SkippedArtifactMetadata> skippedArtifacts) {
        this.skippedArtifacts = skippedArtifacts;
    }

    /**
     * Set deprecated, unused.
     *
     * @param uuid a uuid object.
     */
    public void setUuid(String uuid) {
        this.uuid = uuidDefault.equals(uuid) ? null : uuid;
    }
}
