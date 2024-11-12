/*-
 * Copyright (c) 2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata;

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import java.util.ArrayList;
import java.util.List;
import org.fedoraproject.xmvn.xml.IgnoredProperty;
import org.fedoraproject.xmvn.xml.JavaProperties;

/**
 * Entity-Relationship Model for {@link PackageMetadata}.
 *
 * @author Mikolaj Izdebski
 */
interface PackageMetadataERM {
    /**
     * An {@link Entity} modeling mutable data type {@link SkippedArtifactMetadata}. It specifies
     * how data should be read and written as XML {@code <skippedArtifact>} elements.
     */
    Entity<SkippedArtifactMetadata, SkippedArtifactMetadata> skippedArtifactEntity =
            Entity.ofMutable(
                    "skippedArtifact",
                    SkippedArtifactMetadata::new,
                    Attribute.of(
                            "groupId",
                            SkippedArtifactMetadata::getGroupId,
                            SkippedArtifactMetadata::setGroupId),
                    Attribute.of(
                            "artifactId",
                            SkippedArtifactMetadata::getArtifactId,
                            SkippedArtifactMetadata::setArtifactId),
                    Attribute.ofOptional(
                            "extension",
                            SkippedArtifactMetadata::getExtensionOrNull,
                            SkippedArtifactMetadata::setExtension),
                    Attribute.ofOptional(
                            "classifier",
                            SkippedArtifactMetadata::getClassifierOrNull,
                            SkippedArtifactMetadata::setClassifier));

    /**
     * An {@link Entity} modeling {@link List} of {@link SkippedArtifactMetadata}s. It specifies how
     * data should be read and written as XML {@code <skippedArtifacts>} elements.
     */
    Entity<List<SkippedArtifactMetadata>, List<SkippedArtifactMetadata>> skippedArtifactsEntity =
            Entity.ofMutable(
                    "skippedArtifacts",
                    ArrayList<SkippedArtifactMetadata>::new,
                    Relationship.of(skippedArtifactEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link ArtifactAlias}. It specifies how data
     * should be read and written as XML {@code <alias>} elements.
     */
    Entity<ArtifactAlias, ArtifactAlias> aliasEntity =
            Entity.ofMutable(
                    "alias",
                    ArtifactAlias::new,
                    Attribute.of("groupId", ArtifactAlias::getGroupId, ArtifactAlias::setGroupId),
                    Attribute.of(
                            "artifactId",
                            ArtifactAlias::getArtifactId,
                            ArtifactAlias::setArtifactId),
                    Attribute.ofOptional(
                            "extension",
                            ArtifactAlias::getExtensionOrNull,
                            ArtifactAlias::setExtension),
                    Attribute.ofOptional(
                            "classifier",
                            ArtifactAlias::getClassifierOrNull,
                            ArtifactAlias::setClassifier));

    /**
     * An {@link Entity} modeling {@link List} of {@link ArtifactAlias}s. It specifies how data
     * should be read and written as XML {@code <aliases>} elements.
     */
    Entity<List<ArtifactAlias>, List<ArtifactAlias>> aliasesEntity =
            Entity.ofMutable(
                    "aliases",
                    ArrayList<ArtifactAlias>::new,
                    Relationship.of(aliasEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link DependencyExclusion}. It specifies how
     * data should be read and written as XML {@code <exclusion>} elements.
     */
    Entity<DependencyExclusion, DependencyExclusion> exclusionEntity =
            Entity.ofMutable(
                    "exclusion",
                    DependencyExclusion::new,
                    Attribute.of(
                            "groupId",
                            DependencyExclusion::getGroupId,
                            DependencyExclusion::setGroupId),
                    Attribute.of(
                            "artifactId",
                            DependencyExclusion::getArtifactId,
                            DependencyExclusion::setArtifactId));

    /**
     * An {@link Entity} modeling {@link List} of {@link DependencyExclusion}s. It specifies how
     * data should be read and written as XML {@code <exclusions>} elements.
     */
    Entity<List<DependencyExclusion>, List<DependencyExclusion>> exclusionsEntity =
            Entity.ofMutable(
                    "exclusions",
                    ArrayList<DependencyExclusion>::new,
                    Relationship.of(exclusionEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link Dependency}. It specifies how data should
     * be read and written as XML {@code <dependency>} elements.
     */
    Entity<Dependency, Dependency> dependencyEntity =
            Entity.ofMutable(
                    "dependency",
                    Dependency::new,
                    Attribute.of("groupId", Dependency::getGroupId, Dependency::setGroupId),
                    Attribute.of(
                            "artifactId", Dependency::getArtifactId, Dependency::setArtifactId),
                    Attribute.ofOptional(
                            "extension", Dependency::getExtensionOrNull, Dependency::setExtension),
                    Attribute.ofOptional(
                            "classifier",
                            Dependency::getClassifierOrNull,
                            Dependency::setClassifier),
                    Attribute.ofOptional(
                            "requestedVersion",
                            Dependency::getRequestedVersionOrNull,
                            Dependency::setRequestedVersion),
                    Attribute.ofOptional(
                            "resolvedVersion",
                            Dependency::getResolvedVersionOrNull,
                            Dependency::setResolvedVersion),
                    Attribute.ofOptional(
                            "namespace", Dependency::getNamespaceOrNull, Dependency::setNamespace),
                    Attribute.ofOptional(
                            "optional",
                            Dependency::getOptionalOrNull,
                            Dependency::setOptional,
                            Object::toString,
                            Boolean::valueOf),
                    Relationship.ofSingular(
                            exclusionsEntity,
                            Dependency::getExclusionsOrNull,
                            Dependency::setExclusions));

    /**
     * An {@link Entity} modeling {@link List} of {@link Dependency}s. It specifies how data should
     * be read and written as XML {@code <dependencies>} elements.
     */
    Entity<List<Dependency>, List<Dependency>> dependenciesEntity =
            Entity.ofMutable(
                    "dependencies",
                    ArrayList<Dependency>::new,
                    Relationship.of(dependencyEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <compatVersions>} elements.
     */
    Entity<List<String>, List<String>> compatVersionsEntity =
            Entity.ofMutable(
                    "compatVersions",
                    ArrayList<String>::new,
                    Attribute.ofMulti("version", x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link ArtifactMetadata}. It specifies how data
     * should be read and written as XML {@code <artifact>} elements.
     */
    Entity<ArtifactMetadata, ArtifactMetadata> artifactEntity =
            Entity.ofMutable(
                    "artifact",
                    ArtifactMetadata::new,
                    Attribute.of(
                            "groupId", ArtifactMetadata::getGroupId, ArtifactMetadata::setGroupId),
                    Attribute.of(
                            "artifactId",
                            ArtifactMetadata::getArtifactId,
                            ArtifactMetadata::setArtifactId),
                    Attribute.ofOptional(
                            "extension",
                            ArtifactMetadata::getExtensionOrNull,
                            ArtifactMetadata::setExtension),
                    Attribute.ofOptional(
                            "classifier",
                            ArtifactMetadata::getClassifierOrNull,
                            ArtifactMetadata::setClassifier),
                    Attribute.of(
                            "version", ArtifactMetadata::getVersion, ArtifactMetadata::setVersion),
                    Attribute.of("path", ArtifactMetadata::getPath, ArtifactMetadata::setPath),
                    Attribute.ofOptional(
                            "namespace",
                            ArtifactMetadata::getNamespaceOrNull,
                            ArtifactMetadata::setNamespace),
                    IgnoredProperty.of("uuid"),
                    JavaProperties.of(
                            "properties",
                            ArtifactMetadata::getProperties,
                            ArtifactMetadata::setProperties),
                    Relationship.ofSingular(
                            compatVersionsEntity,
                            ArtifactMetadata::getCompatVersionsOrNull,
                            ArtifactMetadata::setCompatVersions),
                    Relationship.ofSingular(
                            aliasesEntity,
                            ArtifactMetadata::getAliasesOrNull,
                            ArtifactMetadata::setAliases),
                    Relationship.ofSingular(
                            dependenciesEntity,
                            ArtifactMetadata::getDependenciesOrNull,
                            ArtifactMetadata::setDependencies));

    /**
     * An {@link Entity} modeling {@link List} of {@link ArtifactMetadata}s. It specifies how data
     * should be read and written as XML {@code <artifacts>} elements.
     */
    Entity<List<ArtifactMetadata>, List<ArtifactMetadata>> artifactsEntity =
            Entity.ofMutable(
                    "artifacts",
                    ArrayList<ArtifactMetadata>::new,
                    Relationship.of(artifactEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link PackageMetadata}. It specifies how data
     * should be read and written as XML {@code <metadata>} elements.
     */
    Entity<PackageMetadata, PackageMetadata> metadataEntity =
            Entity.ofMutable(
                    "metadata",
                    PackageMetadata::new,
                    IgnoredProperty.of("uuid"),
                    JavaProperties.of(
                            "properties",
                            PackageMetadata::getProperties,
                            PackageMetadata::setProperties),
                    Relationship.ofSingular(
                            artifactsEntity,
                            PackageMetadata::getArtifactsOrNull,
                            PackageMetadata::setArtifacts),
                    Relationship.ofSingular(
                            skippedArtifactsEntity,
                            PackageMetadata::getSkippedArtifactsOrNull,
                            PackageMetadata::setSkippedArtifacts));
}
