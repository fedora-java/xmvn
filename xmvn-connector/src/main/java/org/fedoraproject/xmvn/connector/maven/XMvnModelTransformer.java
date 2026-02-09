/*-
 * Copyright (c) 2012-2026 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Extension;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.api.spi.ModelTransformer;
import org.apache.maven.api.spi.ModelTransformerException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.logging.Logger;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnModelTransformer implements ModelTransformer {

    private final Logger logger;
    private final Configurator configurator;

    @Inject
    public XMvnModelTransformer(Logger logger, Configurator configurator) {
        this.logger = logger;
        this.configurator = configurator;
    }

    @Override
    public Model transformEffectiveModel(Model model) throws ModelTransformerException {
        List<Dependency> dependencies = new ArrayList<>(model.getDependencies());
        dependencies.removeIf(this::isSkippedDependency);
        dependencies =
                dependencies.stream()
                        .map(
                                d ->
                                        d.withVersion(
                                                replaceVersion(
                                                        d.getGroupId(),
                                                        d.getArtifactId(),
                                                        d.getVersion())))
                        .toList();
        model = model.withDependencies(dependencies);

        Build build = model.getBuild();
        if (build != null) {
            List<Extension> extensions = new ArrayList<>(build.getExtensions());
            extensions =
                    extensions.stream()
                            .map(
                                    e ->
                                            e.withVersion(
                                                    replaceVersion(
                                                            e.getGroupId(),
                                                            e.getArtifactId(),
                                                            e.getVersion())))
                            .toList();
            build = build.withExtensions(extensions);
            List<Plugin> plugins = new ArrayList<>(build.getPlugins());
            plugins.removeIf(this::isSkippedPlugin);
            plugins =
                    plugins.stream()
                            .map(
                                    p ->
                                            p.withVersion(
                                                    replaceVersion(
                                                            p.getGroupId(),
                                                            p.getArtifactId(),
                                                            p.getVersion())))
                            .toList();
            build = build.withPlugins(plugins);
            model = model.withBuild(build);
        }
        return model;
    }

    private boolean matches(String field, String pattern) {
        return pattern == null || pattern.isEmpty() || Objects.equals(field, pattern);
    }

    private boolean isSkippedDependency(Dependency d) {
        return matches(d.getScope(), "test")
                && configurator.getConfiguration().getBuildSettings().isSkipTests();
    }

    private boolean isSkippedPlugin(Plugin p) {
        return configurator.getConfiguration().getBuildSettings().getSkippedPlugins().stream()
                .anyMatch(
                        sp ->
                                matches(p.getGroupId(), sp.getGroupId())
                                        && matches(p.getArtifactId(), sp.getArtifactId())
                                        && (sp.getExtension() == null
                                                || sp.getExtension().isEmpty())
                                        && (sp.getClassifier() == null
                                                || sp.getClassifier().isEmpty())
                                        && matches(p.getVersion(), sp.getVersion()));
    }

    private String replaceVersion(String groupId, String artifactId, String version) {
        String id = groupId + ":" + artifactId;

        if (version == null || version.isEmpty()) {
            logger.debug(
                    "Missing version of dependency {}, using {}.", id, Artifact.DEFAULT_VERSION);
            return Artifact.DEFAULT_VERSION;
        }

        try {
            if (VersionRange.createFromVersionSpec(version).getRecommendedVersion() == null) {
                logger.debug(
                        "Dependency {} has no recommended version, falling back to {}.",
                        id,
                        Artifact.DEFAULT_VERSION);
                return Artifact.DEFAULT_VERSION;
            }
        } catch (InvalidVersionSpecificationException e) {
            logger.debug(
                    "Dependency {} is using invalid version range, falling back to {}.",
                    id,
                    Artifact.DEFAULT_VERSION);
            return Artifact.DEFAULT_VERSION;
        }

        return version;
    }
}
