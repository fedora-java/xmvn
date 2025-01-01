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
package org.fedoraproject.xmvn.tools.resolve.xml;

import io.kojan.xml.Attribute;
import io.kojan.xml.Entity;
import io.kojan.xml.Relationship;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Entity-Relationship Model for {@link List}s of {@link ResolutionRequest}s and {@link
 * ResolutionResult}s.
 *
 * @author Mikolaj Izdebski
 */
interface ResolverERM {

    /**
     * An {@link Entity} modeling immutable data type {@link Artifact}. It specifies how data should
     * be read and written as XML {@code <artifact>} elements.
     */
    Entity<Artifact, ArtifactBean> artifactEntity =
            Entity.of(
                    "artifact",
                    ArtifactBean::new,
                    Attribute.of("groupId", Artifact::getGroupId, ArtifactBean::setGroupId),
                    Attribute.of(
                            "artifactId", Artifact::getArtifactId, ArtifactBean::setArtifactId),
                    Attribute.ofOptional(
                            "extension", Artifact::getExtension, ArtifactBean::setExtension),
                    Attribute.ofOptional(
                            "classifier", Artifact::getClassifier, ArtifactBean::setClassifier),
                    Attribute.ofOptional("version", Artifact::getVersion, ArtifactBean::setVersion),
                    Attribute.ofOptional(
                            "path",
                            Artifact::getPath,
                            ArtifactBean::setPath,
                            Object::toString,
                            Path::of));

    /**
     * An {@link Entity} modeling mutable data type {@link ResolutionRequest}. It specifies how data
     * should be read and written as XML {@code <request>} elements.
     */
    Entity<ResolutionRequest, ResolutionRequest> requestEntity =
            Entity.ofMutable(
                    "request",
                    ResolutionRequest::new,
                    Relationship.ofSingular(
                            artifactEntity,
                            ResolutionRequest::getArtifact,
                            ResolutionRequest::setArtifact),
                    Attribute.ofOptional(
                            "providerNeeded",
                            ResolutionRequest::isProviderNeeded,
                            ResolutionRequest::setProviderNeeded,
                            Object::toString,
                            Boolean::valueOf),
                    Attribute.ofOptional(
                            "persistentFileNeeded",
                            ResolutionRequest::isPersistentFileNeeded,
                            ResolutionRequest::setPersistentFileNeeded,
                            Object::toString,
                            Boolean::valueOf));

    /**
     * An {@link Entity} modeling {@link List} of {@link ResolutionRequest}s. It specifies how data
     * should be read and written as XML {@code <requests>} elements.
     */
    Entity<List<ResolutionRequest>, List<ResolutionRequest>> requestsEntity =
            Entity.ofMutable(
                    "requests",
                    ArrayList<ResolutionRequest>::new,
                    Relationship.of(requestEntity, x -> x, List::add));

    /**
     * An {@link Entity} modeling mutable data type {@link ResolutionResult}. It specifies how data
     * should be read and written as XML {@code <result>} elements.
     */
    Entity<ResolutionResult, ResolutionResultBean> resultEntity =
            Entity.of(
                    "result",
                    ResolutionResultBean::new,
                    Attribute.of(
                            "artifactPath",
                            ResolutionResult::getArtifactPath,
                            ResolutionResultBean::setArtifactPath,
                            Object::toString,
                            Path::of),
                    Attribute.of(
                            "provider",
                            ResolutionResult::getProvider,
                            ResolutionResultBean::setProvider),
                    Attribute.of(
                            "compatVersion",
                            ResolutionResult::getCompatVersion,
                            ResolutionResultBean::setCompatVersion),
                    Attribute.of(
                            "namespace",
                            ResolutionResult::getNamespace,
                            ResolutionResultBean::setNamespace));

    /**
     * An {@link Entity} modeling {@link List} of {@link ResolutionResult}s. It specifies how data
     * should be read and written as XML {@code <results>} elements.
     */
    Entity<List<ResolutionResult>, List<ResolutionResult>> resultsEntity =
            Entity.ofMutable(
                    "results",
                    ArrayList<ResolutionResult>::new,
                    Relationship.of(resultEntity, x -> x, List::add));
}
