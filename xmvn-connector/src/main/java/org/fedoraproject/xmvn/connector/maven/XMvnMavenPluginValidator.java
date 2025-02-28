/*-
 * Copyright (c) 2025 Red Hat, Inc.
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

import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.internal.MavenPluginValidator;
import org.eclipse.sisu.Priority;

/**
 * This is a simple Maven plugin validator that pretends that all plugins have valid descriptors.
 *
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
@Priority(100)
public class XMvnMavenPluginValidator implements MavenPluginValidator {
    @Override
    public void validate(
            Artifact pluginArtifact, PluginDescriptor pluginDescriptor, List<String> errors) {
        if (pluginDescriptor.getVersion() == null) {
            pluginDescriptor.setVersion(org.fedoraproject.xmvn.artifact.Artifact.DEFAULT_VERSION);
        }
    }
}
