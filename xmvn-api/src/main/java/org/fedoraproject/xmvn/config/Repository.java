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

import java.util.Properties;
import org.w3c.dom.Element;

/**
 * Artifact repository.
 *
 * @author Mikolaj Izdebski
 */
public class Repository {

    /** Unique string identifying this repository. */
    private String id;

    /** Role hint of Plexus component implementing the repository. */
    private String type;

    /** Field properties. */
    private Properties properties = new Properties();

    /**
     * Arbitrary XML used to configure structure of the repository. Configuration can be freely used
     * by the implementation, so their exact meaning depends on repository type. See documentation
     * for repository types to see the format of supported configuration (if any).
     */
    private Element configuration;

    /** A boolean expression describing which artifacts can be stored in this repository. */
    private Element filter;

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
     * Get arbitrary XML used to configure structure of the repository. Configuration can be freely
     * used by the implementation, so their exact meaning depends on repository type. See
     * documentation for repository types to see the format of supported configuration (if any).
     *
     * @return Object
     */
    public Element getConfiguration() {
        return configuration;
    }

    /**
     * Get a boolean expression describing which artifacts can be stored in this repository.
     *
     * @return Object
     */
    public Element getFilter() {
        return filter;
    }

    /**
     * Get unique string identifying this repository.
     *
     * @return String
     */
    public String getId() {
        return id;
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
     * Get role hint of Plexus component implementing the repository.
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Set arbitrary XML used to configure structure of the repository. Configuration can be freely
     * used by the implementation, so their exact meaning depends on repository type. See
     * documentation for repository types to see the format of supported configuration (if any).
     *
     * @param configuration a configuration object.
     */
    public void setConfiguration(Element configuration) {
        this.configuration = configuration;
    }

    /**
     * Set a boolean expression describing which artifacts can be stored in this repository.
     *
     * @param filter a filter object.
     */
    public void setFilter(Element filter) {
        this.filter = filter;
    }

    /**
     * Set unique string identifying this repository.
     *
     * @param id a id object.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set properties associated with this repository. Properties can be freely used by the
     * implementation, so their exact meaning depends on repository type. See documentation for
     * repository types to see list of supported properties.
     *
     * @param properties a properties object.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Set role hint of Plexus component implementing the repository.
     *
     * @param type a type object.
     */
    public void setType(String type) {
        this.type = type;
    }
}
