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

/**
 * Description of artifact excluded from dependency tree.
 *
 * @author Mikolaj Izdebski
 */
public class DependencyExclusion {

    /** Group ID of the excluded artifact. */
    private String groupId;

    /** Artifact ID of the excluded artifact. */
    private String artifactId;

    /**
     * Get artifact ID of the excluded artifact.
     *
     * @return String
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get group ID of the excluded artifact.
     *
     * @return String
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set artifact ID of the excluded artifact.
     *
     * @param artifactId a artifactId object.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set group ID of the excluded artifact.
     *
     * @param groupId a groupId object.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
