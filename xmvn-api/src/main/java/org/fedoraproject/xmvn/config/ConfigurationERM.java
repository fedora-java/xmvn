/*-
 * Copyright (c) 2024-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config;

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import java.util.ArrayList;
import java.util.List;
import org.fedoraproject.xmvn.xml.IgnoredProperty;
import org.fedoraproject.xmvn.xml.JavaProperties;

/**
 * Entity-Relationship Model for {@link Configuration}.
 *
 * @author Mikolaj Izdebski
 */
interface ConfigurationERM {

    /**
     * An {@link Entity} modeling mutable data type {@link Artifact}. It specifies how data should
     * be read and written as XML {@code <plugin>} elements.
     */
    Entity<Artifact, Artifact> pluginEntity =
            Entity.ofMutable(
                    "plugin",
                    Artifact::new,
                    Attribute.ofOptional(
                            "groupId", Artifact::getGroupIdOrNull, Artifact::setGroupId),
                    Attribute.ofOptional(
                            "artifactId", Artifact::getArtifactIdOrNull, Artifact::setArtifactId),
                    Attribute.ofOptional(
                            "version", Artifact::getVersionOrNull, Artifact::setVersion),
                    Attribute.ofOptional(
                            "classifier", Artifact::getClassifier, Artifact::setClassifier),
                    Attribute.ofOptional(
                            "extension", Artifact::getExtension, Artifact::setExtension));

    /**
     * An {@link Entity} modeling {@link List} of {@link Artifact}s. It specifies how data should be
     * read and written as XML {@code <skippedPlugins>} elements.
     */
    Entity<List<Artifact>, List<Artifact>> skippedPluginsEntity =
            Entity.ofMutable(
                    "skippedPlugins",
                    ArrayList<Artifact>::new,
                    Relationship.of(pluginEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link BuildSettings}. It specifies how data
     * should be read and written as XML {@code <buildSettings>} elements.
     */
    Entity<BuildSettings, BuildSettings> buildSettingsEntity =
            Entity.ofMutable(
                    "buildSettings",
                    BuildSettings::new,
                    Attribute.ofOptional(
                            "debug",
                            BuildSettings::isDebug,
                            BuildSettings::setDebug,
                            Object::toString,
                            Boolean::valueOf),
                    Attribute.ofOptional(
                            "skipTests",
                            BuildSettings::isSkipTests,
                            BuildSettings::setSkipTests,
                            Object::toString,
                            Boolean::valueOf),
                    Relationship.ofSingular(
                            skippedPluginsEntity,
                            BuildSettings::getSkippedPluginsOrNull,
                            BuildSettings::setSkippedPlugins));

    /**
     * An {@link Entity} modeling mutable data type {@link Artifact}. It specifies how data should
     * be read and written as XML {@code <artifactGlob>} elements.
     */
    Entity<Artifact, Artifact> artifactGlobEntity =
            Entity.ofMutable(
                    "artifactGlob",
                    Artifact::new,
                    Attribute.ofOptional(
                            "groupId", Artifact::getGroupIdOrNull, Artifact::setGroupId),
                    Attribute.ofOptional(
                            "artifactId", Artifact::getArtifactIdOrNull, Artifact::setArtifactId),
                    Attribute.ofOptional(
                            "version", Artifact::getVersionOrNull, Artifact::setVersion),
                    Attribute.ofOptional(
                            "classifier", Artifact::getClassifier, Artifact::setClassifier),
                    Attribute.ofOptional(
                            "extension", Artifact::getExtension, Artifact::setExtension));

    /**
     * An {@link Entity} modeling mutable data type {@link Repository}. It specifies how data should
     * be read and written as XML {@code <repository>} elements.
     */
    Entity<Repository, Repository> repositoryEntity =
            Entity.ofMutable(
                    "repository",
                    Repository::new,
                    Attribute.of("id", Repository::getId, Repository::setId),
                    Attribute.of("type", Repository::getType, Repository::setType),
                    IgnoredProperty.of("stereotypes"),
                    JavaProperties.of(
                            "properties", Repository::getProperties, Repository::setProperties),
                    DOM.of(
                            "configuration",
                            Repository::getConfiguration,
                            Repository::setConfiguration),
                    DOM.of("filter", Repository::getFilter, Repository::setFilter));

    /**
     * An {@link Entity} modeling {@link List} of {@link Repository}s. It specifies how data should
     * be read and written as XML {@code <repositories>} elements.
     */
    Entity<List<Repository>, List<Repository>> repositoriesEntity =
            Entity.ofMutable(
                    "repositories",
                    ArrayList<Repository>::new,
                    Relationship.of(repositoryEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <files>} elements.
     */
    Entity<List<String>, List<String>> filesEntity =
            Entity.ofMutable(
                    "files", ArrayList<String>::new, Attribute.ofMulti("file", x -> x, List::add));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <versions>} elements.
     */
    Entity<List<String>, List<String>> versionsEntity =
            Entity.ofMutable(
                    "versions",
                    ArrayList<String>::new,
                    Attribute.ofMulti("version", x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link Artifact}. It specifies how data should
     * be read and written as XML {@code <alias>} elements.
     */
    Entity<Artifact, Artifact> aliasEntity =
            Entity.ofMutable(
                    "alias",
                    Artifact::new,
                    Attribute.ofOptional(
                            "groupId", Artifact::getGroupIdOrNull, Artifact::setGroupId),
                    Attribute.ofOptional(
                            "artifactId", Artifact::getArtifactIdOrNull, Artifact::setArtifactId),
                    Attribute.ofOptional(
                            "version", Artifact::getVersionOrNull, Artifact::setVersion),
                    Attribute.ofOptional(
                            "classifier", Artifact::getClassifier, Artifact::setClassifier),
                    Attribute.ofOptional(
                            "extension", Artifact::getExtension, Artifact::setExtension));

    /**
     * An {@link Entity} modeling {@link List} of {@link Artifact}s. It specifies how data should be
     * read and written as XML {@code <aliases>} elements.
     */
    Entity<List<Artifact>, List<Artifact>> aliasesEntity =
            Entity.ofMutable(
                    "aliases",
                    ArrayList<Artifact>::new,
                    Relationship.of(aliasEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link PackagingRule}. It specifies how data
     * should be read and written as XML {@code <rule>} elements.
     */
    Entity<PackagingRule, PackagingRule> ruleEntity =
            Entity.ofMutable(
                    "rule",
                    PackagingRule::new,
                    Relationship.ofSingular(
                            artifactGlobEntity,
                            PackagingRule::getArtifactGlob,
                            PackagingRule::setArtifactGlob),
                    Attribute.ofOptional(
                            "targetPackage",
                            PackagingRule::getTargetPackage,
                            PackagingRule::setTargetPackage),
                    Attribute.ofOptional(
                            "targetRepository",
                            PackagingRule::getTargetRepository,
                            PackagingRule::setTargetRepository),
                    Relationship.ofSingular(
                            filesEntity, PackagingRule::getFilesOrNull, PackagingRule::setFiles),
                    Relationship.ofSingular(
                            versionsEntity,
                            PackagingRule::getVersionsOrNull,
                            PackagingRule::setVersions),
                    Relationship.ofSingular(
                            aliasesEntity,
                            PackagingRule::getAliasesOrNull,
                            PackagingRule::setAliases),
                    Attribute.ofOptional(
                            "optional",
                            PackagingRule::getOptionalOrNull,
                            PackagingRule::setOptional,
                            Object::toString,
                            Boolean::valueOf));

    /**
     * An {@link Entity} modeling {@link List} of {@link PackagingRule}s. It specifies how data
     * should be read and written as XML {@code <artifactManagement>} elements.
     */
    Entity<List<PackagingRule>, List<PackagingRule>> artifactManagementEntity =
            Entity.ofMutable(
                    "artifactManagement",
                    ArrayList<PackagingRule>::new,
                    Relationship.of(ruleEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link InstallerSettings}. It specifies how data
     * should be read and written as XML {@code <installerSettings>} elements.
     */
    Entity<InstallerSettings, InstallerSettings> installerSettingsEntity =
            Entity.ofMutable(
                    "installerSettings",
                    InstallerSettings::new,
                    Attribute.ofOptional(
                            "debug",
                            InstallerSettings::isDebug,
                            InstallerSettings::setDebug,
                            Object::toString,
                            Boolean::valueOf),
                    Attribute.ofOptional(
                            "metadataDir",
                            InstallerSettings::getMetadataDir,
                            InstallerSettings::setMetadataDir));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <localRepositories>} elements.
     */
    Entity<List<String>, List<String>> localRepositoriesEntity =
            Entity.ofMutable(
                    "localRepositories",
                    ArrayList<String>::new,
                    Attribute.ofMulti("repository", x -> x, List::add));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <metadataRepositories>} elements.
     */
    Entity<List<String>, List<String>> metadataRepositoriesEntity =
            Entity.ofMutable(
                    "metadataRepositories",
                    ArrayList<String>::new,
                    Attribute.ofMulti("repository", x -> x, List::add));

    /**
     * An {@link Entity} modeling {@link List} of {@link String}s. It specifies how data should be
     * read and written as XML {@code <prefixes>} elements.
     */
    Entity<List<String>, List<String>> prefixesEntity =
            Entity.ofMutable(
                    "prefixes",
                    ArrayList<String>::new,
                    Attribute.ofMulti("prefix", x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link Artifact}. It specifies how data should
     * be read and written as XML {@code <artifact>} elements.
     */
    Entity<Artifact, Artifact> artifactEntity =
            Entity.ofMutable(
                    "artifact",
                    Artifact::new,
                    Attribute.ofOptional(
                            "groupId", Artifact::getGroupIdOrNull, Artifact::setGroupId),
                    Attribute.ofOptional(
                            "artifactId", Artifact::getArtifactIdOrNull, Artifact::setArtifactId),
                    Attribute.ofOptional(
                            "version", Artifact::getVersionOrNull, Artifact::setVersion),
                    Attribute.ofOptional(
                            "classifier", Artifact::getClassifier, Artifact::setClassifier),
                    Attribute.ofOptional(
                            "extension", Artifact::getExtension, Artifact::setExtension));

    /**
     * An {@link Entity} modeling {@link List} of {@link Artifact}s. It specifies how data should be
     * read and written as XML {@code <blacklist>} elements.
     */
    Entity<List<Artifact>, List<Artifact>> blacklistEntity =
            Entity.ofMutable(
                    "blacklist",
                    ArrayList<Artifact>::new,
                    Relationship.of(artifactEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link ResolverSettings}. It specifies how data
     * should be read and written as XML {@code <resolverSettings>} elements.
     */
    Entity<ResolverSettings, ResolverSettings> resolverSettingsEntity =
            Entity.ofMutable(
                    "resolverSettings",
                    ResolverSettings::new,
                    Attribute.ofOptional(
                            "debug",
                            ResolverSettings::isDebug,
                            ResolverSettings::setDebug,
                            Object::toString,
                            Boolean::valueOf),
                    Relationship.ofSingular(
                            localRepositoriesEntity,
                            ResolverSettings::getLocalRepositoriesOrNull,
                            ResolverSettings::setLocalRepositories),
                    Relationship.ofSingular(
                            metadataRepositoriesEntity,
                            ResolverSettings::getMetadataRepositoriesOrNull,
                            ResolverSettings::setMetadataRepositories),
                    Attribute.ofOptional(
                            "ignoreDuplicateMetadata",
                            ResolverSettings::isIgnoreDuplicateMetadata,
                            ResolverSettings::setIgnoreDuplicateMetadata,
                            Object::toString,
                            Boolean::valueOf),
                    Relationship.ofSingular(
                            prefixesEntity,
                            ResolverSettings::getPrefixesOrNull,
                            ResolverSettings::setPrefixes),
                    Relationship.ofSingular(
                            blacklistEntity,
                            ResolverSettings::getBlacklistOrNull,
                            ResolverSettings::setBlacklist));

    /**
     * An {@link Entity} modeling mutable data type {@link Configuration}. It specifies how data
     * should be read and written as XML {@code <configuration>} elements.
     */
    Entity<Configuration, Configuration> configurationEntity =
            Entity.ofMutable(
                    "configuration",
                    Configuration::new,
                    JavaProperties.of(
                            "properties",
                            Configuration::getProperties,
                            Configuration::setProperties),
                    Relationship.ofSingular(
                            repositoriesEntity,
                            Configuration::getRepositoriesOrNull,
                            Configuration::setRepositories),
                    Relationship.ofSingular(
                            buildSettingsEntity,
                            Configuration::getBuildSettings,
                            Configuration::setBuildSettings),
                    Relationship.ofSingular(
                            artifactManagementEntity,
                            Configuration::getArtifactManagementOrNull,
                            Configuration::setArtifactManagement),
                    Relationship.ofSingular(
                            resolverSettingsEntity,
                            Configuration::getResolverSettings,
                            Configuration::setResolverSettings),
                    Relationship.ofSingular(
                            installerSettingsEntity,
                            Configuration::getInstallerSettings,
                            Configuration::setInstallerSettings));
}
