/*-
 * Copyright (c) 2012-2024 Red Hat, Inc.
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
package org.apache.maven.plugin;

import java.util.List;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * This is a simple Maven plugin validator that pretends that all plugins have valid descriptors.
 *
 * <p>This is XMvn-specific class and it resides in {@code org.apache.maven} namespace only because it needs to override
 * Maven class. If this was a Plexus component then there would be no need to override Maven class, setting default
 * component class in {@code plexus.xml} would be enough.
 *
 * <p>TODO: Try convince Maven upstream to convert this to Plexus component.
 *
 * @author Mikolaj Izdebski
 */
public class MavenPluginValidator {
    // This constructor must be provided for compatibility with Maven
    @SuppressWarnings("unused")
    public MavenPluginValidator(org.apache.maven.artifact.Artifact pluginArtifact) {}

    public void validate(PluginDescriptor pluginDescriptor) {
        if (pluginDescriptor.getVersion() == null) {
            pluginDescriptor.setVersion(Artifact.DEFAULT_VERSION);
        }
    }

    public boolean hasErrors() {
        return false;
    }

    public List<String> getErrors() {
        return List.of();
    }
}
