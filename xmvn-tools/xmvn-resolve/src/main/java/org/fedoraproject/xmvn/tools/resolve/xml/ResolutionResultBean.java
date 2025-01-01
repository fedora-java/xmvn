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
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * A {@link Builder} for {@link ResolutionResult} objects.
 *
 * @author Mikolaj Izdebski
 */
class ResolutionResultBean implements ResolutionResult, Builder<ResolutionResult> {
    private Path artifactPath;
    private String provider;
    private String compatVersion;
    private String namespace;

    public Path getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(Path artifactPath) {
        this.artifactPath = artifactPath;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCompatVersion() {
        return compatVersion;
    }

    public void setCompatVersion(String compatVersion) {
        this.compatVersion = compatVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public ResolutionResult build() {
        return this;
    }
}
