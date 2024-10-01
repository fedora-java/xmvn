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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * Default implementation of XMvn {@code Resolver} interface.
 *
 * <p><strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only
 * for technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong>
 * reference it directly.
 *
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultResolver implements Resolver {
    @Inject
    private Logger logger;

    @Inject
    private Configurator configurator;

    @Inject
    private MetadataResolver metadataResolver;

    private MetadataRequest metadataRequest;

    private MetadataResult metadataResult;

    private static final RpmDb RPMDB = new RpmDb();

    private final Resolver localRepoResolver;

    private final EffectivePomGenerator pomGenerator;

    private final CacheManager cacheManager;

    private MockAgent mockAgent;

    public DefaultResolver(ServiceLocator locator) {
        this();

        logger = locator.getService(Logger.class);
        configurator = locator.getService(Configurator.class);
        metadataResolver = locator.getService(MetadataResolver.class);
    }

    public DefaultResolver() {
        localRepoResolver = new LocalRepositoryResolver();
        pomGenerator = new EffectivePomGenerator();
        cacheManager = new CacheManager();
    }

    @Override
    public ResolutionResult resolve(ResolutionRequest request) {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        ResolutionResult localRepoResult = localRepoResolver.resolve(request);
        if (localRepoResult.getArtifactPath() != null) {
            return localRepoResult;
        }

        Artifact artifact = request.getArtifact();
        logger.debug("Trying to resolve artifact {}", artifact);

        if (metadataRequest == null) {
            ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
            metadataRequest = new MetadataRequest(settings.getMetadataRepositories());
            metadataRequest.setIgnoreDuplicates(settings.isIgnoreDuplicateMetadata());
        }
        if (metadataResult == null) {
            metadataResult = metadataResolver.resolveMetadata(metadataRequest);
        }
        ArtifactMetadata metadata = metadataResult.getMetadataFor(artifact);

        String compatVersion;
        if (metadata == null) {
            metadata = metadataResult.getMetadataFor(artifact.setVersion(Artifact.DEFAULT_VERSION));
            compatVersion = null;
        } else {
            compatVersion = artifact.getVersion();
        }

        if (mockAgent == null) {
            mockAgent = new MockAgent(logger);
        }

        if (metadata == null && mockAgent.tryInstallArtifact(artifact)) {
            metadataResult = metadataResolver.resolveMetadata(metadataRequest);
            metadata = metadataResult.getMetadataFor(artifact);

            if (metadata == null) {
                metadata = metadataResult.getMetadataFor(artifact.setVersion(Artifact.DEFAULT_VERSION));
                compatVersion = null;
            } else {
                compatVersion = artifact.getVersion();
            }
        }

        if (metadata == null) {
            logger.debug("Failed to resolve artifact: {}", artifact);
            return new DefaultResolutionResult();
        }

        properties.putAll(metadata.getProperties());

        if (!"true".equals(properties.getProperty("xmvn.resolver.disableEffectivePom"))
                && "pom".equals(metadata.getExtension())
                && (!"pom".equals(properties.getProperty("type")) || metadata.getPath() == null)) {
            try {
                Path pomPath = pomGenerator.generateEffectivePom(metadata, artifact);

                if (request.isPersistentFileNeeded()) {
                    pomPath = cacheManager.cacheFile(pomPath);
                }

                metadata.setPath(pomPath.toString());
            } catch (IOException e) {
                logger.warn("Failed to generate effective POM", e);
                return new DefaultResolutionResult();
            }
        }

        Path artifactPath = Path.of(metadata.getPath());
        try {
            artifactPath = artifactPath.toRealPath();
        } catch (IOException e) {
            // Ignore
        }

        DefaultResolutionResult result = new DefaultResolutionResult(artifactPath);
        result.setNamespace(metadata.getNamespace());
        result.setCompatVersion(compatVersion);
        if (request.isProviderNeeded()) {
            result.setProvider(RPMDB.lookupPath(artifactPath));
        }

        logger.debug("Artifact {} was resolved to {}", artifact, artifactPath);
        return result;
    }
}
