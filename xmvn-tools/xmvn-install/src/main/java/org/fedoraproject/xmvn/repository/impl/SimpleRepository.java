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
package org.fedoraproject.xmvn.repository.impl;

import java.nio.file.Path;
import java.util.Set;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.tools.install.condition.Condition;
import org.w3c.dom.Element;

/**
 * @author Mikolaj Izdebski
 */
abstract class SimpleRepository extends AbstractRepository {
    private final Path root;

    private final Condition condition;

    public SimpleRepository(String namespace, Path root, Element filter) {
        super(namespace);
        this.root = root;
        condition = new Condition(filter);
    }

    protected abstract Path getArtifactPath(
            String pattern,
            String groupId,
            String artifactId,
            String extension,
            String classifier,
            String version);

    @Override
    public Path getPrimaryArtifactPath(Artifact artifact, ArtifactContext context, String pattern) {
        if (!condition.getValue(context)) {
            return null;
        }

        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        if (version.equals(Artifact.DEFAULT_VERSION)) {
            version = null;
        }

        Path path = getArtifactPath(pattern, groupId, artifactId, extension, classifier, version);
        if (path == null) {
            return null;
        }

        if (root != null) {
            path = root.resolve(path);
        }

        return path;
    }

    @Override
    public Set<Path> getRootPaths() {
        return Set.of(root);
    }
}
