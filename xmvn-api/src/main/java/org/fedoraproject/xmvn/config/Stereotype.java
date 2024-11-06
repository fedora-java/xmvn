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
 * Stereotype of Maven artifact.
 *
 * @author Mikolaj Izdebski
 */
public class Stereotype {

    /** Type ID of the stereotype. */
    private String type;

    /** Extension of the artifact. */
    private String extension;

    /** Classifier of the artifact. */
    private String classifier;

    /**
     * Get classifier of the artifact.
     *
     * @return String
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Get extension of the artifact.
     *
     * @return String
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get type ID of the stereotype.
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Set classifier of the artifact.
     *
     * @param classifier a classifier object.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Set extension of the artifact.
     *
     * @param extension a extension object.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Set type ID of the stereotype.
     *
     * @param type a type object.
     */
    public void setType(String type) {
        this.type = type;
    }
}
