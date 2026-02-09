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

import java.util.ArrayList;
import java.util.List;

/**
 * Basic build settings.
 *
 * @author Mikolaj Izdebski
 */
public class BuildSettings {

    /** Whether detailed debugging information about the build process should be logged. */
    private Boolean debug;

    /** Whether compilation and execution of unit and integration tests should be skipped. */
    private Boolean skipTests;

    /** Field skippedPlugins. */
    private List<Artifact> skippedPlugins = new ArrayList<>();

    /**
     * Method getSkippedPlugins.
     *
     * @return List
     */
    public List<Artifact> getSkippedPlugins() {
        return skippedPlugins;
    }

    List<Artifact> getSkippedPluginsOrNull() {
        return skippedPlugins.isEmpty() ? null : skippedPlugins;
    }

    /**
     * Get whether detailed debugging information about the build process should be logged.
     *
     * @return Boolean
     */
    public Boolean isDebug() {
        return debug;
    }

    /**
     * Get whether compilation and execution of unit and integration tests should be skipped.
     *
     * @return Boolean
     */
    public Boolean isSkipTests() {
        return skipTests;
    }

    /**
     * Set whether detailed debugging information about the build process should be logged.
     *
     * @param debug a debug object.
     */
    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    /**
     * Set whether compilation and execution of unit and integration tests should be skipped.
     *
     * @param skipTests a skipTests object.
     */
    public void setSkipTests(Boolean skipTests) {
        this.skipTests = skipTests;
    }

    /**
     * Set list of plugins which will not be executed during build.
     *
     * @param skippedPlugins a skippedPlugins object.
     */
    public void setSkippedPlugins(List<Artifact> skippedPlugins) {
        this.skippedPlugins = skippedPlugins;
    }
}
