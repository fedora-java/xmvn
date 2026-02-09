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

/**
 * XMvn settings related to installation of artifacts.
 *
 * @author Mikolaj Izdebski
 */
public class InstallerSettings {

    /** Whether additional debugging information about artifact nstallation should be printed. */
    private Boolean debug = false;

    /** Directory into which XMvn metadata files are be installed. */
    private String metadataDir;

    /**
     * Get directory into which XMvn metadata files are be installed.
     *
     * @return String
     */
    public String getMetadataDir() {
        return metadataDir;
    }

    /**
     * Get whether additional debugging information about artifact nstallation should be printed.
     *
     * @return Boolean
     */
    public Boolean isDebug() {
        return debug;
    }

    /**
     * Set whether additional debugging information about artifact nstallation should be printed.
     *
     * @param debug a debug object.
     */
    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    /**
     * Set directory into which XMvn metadata files are be installed.
     *
     * @param metadataDir a metadataDir object.
     */
    public void setMetadataDir(String metadataDir) {
        this.metadataDir = metadataDir;
    }
}
