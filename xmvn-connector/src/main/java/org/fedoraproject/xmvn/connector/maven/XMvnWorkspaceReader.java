/*-
 * Copyright (c) 2012-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
@Named("ide")
@Singleton
public class XMvnWorkspaceReader implements WorkspaceReader {
    @Inject private Resolver resolver;

    private static final WorkspaceRepository REPOSITORY = new WorkspaceRepository();

    private final List<ResolutionListener> listeners = new ArrayList<>();

    public void addResolutionListener(ResolutionListener listener) {
        listeners.add(listener);
    }

    private ResolutionResult resolve(Artifact artifact) {
        org.fedoraproject.xmvn.artifact.Artifact xmvnArtifact =
                new org.fedoraproject.xmvn.artifact.DefaultArtifact(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getExtension(),
                        artifact.getClassifier(),
                        artifact.getVersion());
        ResolutionRequest request = new ResolutionRequest(xmvnArtifact);

        for (ResolutionListener listener : listeners) listener.resolutionRequested(request);

        ResolutionResult result = resolver.resolve(request);

        for (ResolutionListener listener : listeners) listener.resolutionCompleted(request, result);

        return result;
    }

    @Override
    public Path findArtifactPath(Artifact artifact) {
        ResolutionResult result = resolve(artifact);

        Path artifactPath = result.getArtifactPath();
        return artifactPath;
    }

    @Override
    public File findArtifact(Artifact artifact) {
        Path artifactPath = findArtifactPath(artifact);
        return artifactPath != null ? artifactPath.toFile() : null;
    }

    @Override
    public List<String> findVersions(Artifact artifact) {
        ResolutionResult result = resolve(artifact);

        if (result.getArtifactPath() == null) {
            return List.of();
        }

        String version = result.getCompatVersion();
        if (version == null) {
            version = org.fedoraproject.xmvn.artifact.Artifact.DEFAULT_VERSION;
        }

        return List.of(version);
    }

    @Override
    public WorkspaceRepository getRepository() {
        return REPOSITORY;
    }
}
