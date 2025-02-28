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
package org.fedoraproject.xmvn.resolver.impl;

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import io.kojan.xml.XMLException;
import java.util.List;
import java.util.function.Predicate;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;

/**
 * Generates effective POM files from package metadata.
 *
 * @author Mikolaj Izdebski
 */
class EffectivePomGenerator {

    private static <T> T getDefault(T value, Predicate<T> isDefault) {
        if (isDefault.test(value)) {
            return null;
        }
        return value;
    }

    public String generateEffectivePom(ArtifactMetadata metadata, Artifact artifact)
            throws XMLException {

        List<Dependency> dependencies = metadata.getDependencies();

        var exclusionEntity =
                Entity.of(
                        "exclusion",
                        null,
                        Attribute.of("groupId", DependencyExclusion::getGroupId, null),
                        Attribute.of("artifactId", DependencyExclusion::getArtifactId, null));

        var dependencyEntity =
                Entity.of(
                        "dependency",
                        null,
                        Attribute.of("groupId", Dependency::getGroupId, null),
                        Attribute.of("artifactId", Dependency::getArtifactId, null),
                        Attribute.of(
                                "type",
                                dep ->
                                        getDefault(
                                                dep.getExtension(),
                                                Artifact.DEFAULT_EXTENSION::equals),
                                null),
                        Attribute.of(
                                "classifier",
                                dep -> getDefault(dep.getClassifier(), ""::equals),
                                null),
                        Attribute.of("version", Dependency::getRequestedVersion, null),
                        Attribute.of(
                                "optional",
                                dep -> getDefault(dep.isOptional(), Boolean.FALSE::equals),
                                null,
                                Object::toString,
                                null),
                        Relationship.ofSingular(
                                Entity.of(
                                        "exclusions",
                                        null,
                                        Relationship.of(exclusionEntity, x -> x, null)),
                                dep -> getDefault(dep.getExclusions(), List::isEmpty),
                                null));

        var projectEntity =
                Entity.of(
                        "project",
                        null,
                        Attribute.of("modelVersion", x -> "4.0.0", null),
                        Attribute.of("groupId", x -> artifact.getGroupId(), null),
                        Attribute.of("artifactId", x -> artifact.getArtifactId(), null),
                        Attribute.of("version", x -> artifact.getVersion(), null),
                        Relationship.ofSingular(
                                Entity.of(
                                        "dependencies",
                                        null,
                                        Relationship.of(dependencyEntity, x -> x, null)),
                                x -> getDefault(dependencies, List::isEmpty),
                                null));

        return projectEntity.toXML(null);
    }
}
