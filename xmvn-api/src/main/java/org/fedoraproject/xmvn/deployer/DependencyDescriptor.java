/*-
 * Copyright (c) 2015-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.deployer;

import java.util.Collections;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class DependencyDescriptor {
    private final Artifact dependencyArtifact;

    private final boolean optional;

    private final List<Artifact> exclusions;

    public DependencyDescriptor(
            Artifact dependencyArtifact, boolean optional, List<Artifact> exclusions) {
        this.dependencyArtifact = dependencyArtifact;
        this.optional = optional;
        this.exclusions = Collections.unmodifiableList(exclusions);
    }

    public Artifact getDependencyArtifact() {
        return dependencyArtifact;
    }

    public boolean isOptional() {
        return optional;
    }

    public List<Artifact> getExclusions() {
        return exclusions;
    }
}
