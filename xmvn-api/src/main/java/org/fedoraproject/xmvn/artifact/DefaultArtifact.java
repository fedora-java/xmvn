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
package org.fedoraproject.xmvn.artifact;

import java.nio.file.Path;

/**
 * @author Mikolaj Izdebski
 */
@Deprecated
public final class DefaultArtifact implements Artifact {

    private final Artifact delegate;

    public DefaultArtifact(String coords) {
        delegate = Artifact.of(coords);
    }

    public DefaultArtifact(String groupId, String artifactId) {
        delegate = Artifact.of(groupId, artifactId);
    }

    public DefaultArtifact(String groupId, String artifactId, String version) {
        delegate = Artifact.of(groupId, artifactId, version);
    }

    public DefaultArtifact(String groupId, String artifactId, String extension, String version) {
        delegate = Artifact.of(groupId, artifactId, extension, version);
    }

    public DefaultArtifact(
            String groupId,
            String artifactId,
            String extension,
            String classifier,
            String version) {
        delegate = Artifact.of(groupId, artifactId, extension, classifier, version);
    }

    public DefaultArtifact(
            String groupId,
            String artifactId,
            String extension,
            String classifier,
            String version,
            Path path) {
        delegate = Artifact.of(groupId, artifactId, extension, classifier, version, path);
    }

    @Override
    public String getGroupId() {
        return delegate.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return delegate.getArtifactId();
    }

    @Override
    public String getExtension() {
        return delegate.getExtension();
    }

    @Override
    public String getClassifier() {
        return delegate.getClassifier();
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public Path getPath() {
        return delegate.getPath();
    }

    @Override
    public Artifact setVersion(String version) {
        return delegate.setVersion(version);
    }

    @Override
    public Artifact withPath(Path path) {
        return delegate.withPath(path);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object rhs) {
        return delegate.equals(rhs);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
