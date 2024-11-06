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
package org.fedoraproject.xmvn.tools.resolve.xml;

import java.nio.file.Path;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * @author Mikolaj Izdebski
 */
public class ResolutionResultBean {
    private String artifactPath;

    private String provider;

    private String compatVersion;

    private String namespace;

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
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

    /**
     * @author Mikolaj Izdebski
     */
    public static class Adapter {
        public ResolutionResultBean marshal(ResolutionResult result) throws Exception {
            ResolutionResultBean bean = new ResolutionResultBean();

            bean.setArtifactPath(
                    result.getArtifactPath() != null ? result.getArtifactPath().toString() : null);
            bean.setProvider(result.getProvider());
            bean.setCompatVersion(result.getCompatVersion());
            bean.setNamespace(result.getNamespace());

            return bean;
        }

        public ResolutionResult unmarshal(final ResolutionResultBean bean) throws Exception {
            return new ResolutionResult() {
                @Override
                public Path getArtifactPath() {
                    return bean.getArtifactPath() != null ? Path.of(bean.getArtifactPath()) : null;
                }

                @Override
                public String getProvider() {
                    return bean.getProvider();
                }

                @Override
                public String getCompatVersion() {
                    return bean.getCompatVersion();
                }

                @Override
                public String getNamespace() {
                    return bean.getNamespace();
                }
            };
        }
    }
}
