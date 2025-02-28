/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve.xml;

import io.kojan.xml.Builder;
import java.nio.file.Path;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * A {@link Builder} for {@link Artifact} objects.
 *
 * @author Mikolaj Izdebski
 */
class ArtifactBean implements Builder<Artifact> {
    private String groupId;
    private String artifactId;
    private String extension;
    private String classifier;
    private String version;
    private Path path;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Artifact build() {
        return Artifact.of(groupId, artifactId, extension, classifier, version).setPath(path);
    }
}
