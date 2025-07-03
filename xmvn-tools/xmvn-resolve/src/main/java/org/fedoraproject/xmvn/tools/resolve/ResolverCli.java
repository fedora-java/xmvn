/*-
 * Copyright (c) 2012-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve;

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.resolve.xml.ResolverDAO;
import picocli.CommandLine;

/**
 * XMvn Resolver is a very simple commald-line tool to resolve Maven artifacts from system
 * repositories. Basically it's just an interface to artifact resolution mechanism implemented by
 * XMvn Core. The primary intended use case of XMvn Resolver is debugging local artifact
 * repositories.
 *
 * <p>Returns 0 when all artifacts are successfully resolved, 1 on failure to resolve one or more
 * artifacts and 2 when some other error occurs. In the last case a stack trace is printed too.
 *
 * @author Mikolaj Izdebski
 */
public class ResolverCli {
    private final Logger logger;
    private final Resolver resolver;
    private final Configurator configurator;
    private final MetadataResolver metadataResolver;

    public ResolverCli(
            Logger logger,
            Resolver resolver,
            Configurator configurator,
            MetadataResolver metadataResolver) {
        this.logger = logger;
        this.resolver = resolver;
        this.configurator = configurator;
        this.metadataResolver = metadataResolver;
    }

    private List<ResolutionRequest> parseRequests(ResolverCliRequest cli)
            throws IOException, XMLException {
        if (cli.isRaw()) {
            return ResolverDAO.unmarshalRequests(System.in);
        }

        List<ResolutionRequest> requests = new ArrayList<>();

        for (String s : cli.getParameters()) {
            if (s.indexOf(':') > 0 && s.indexOf(':') == s.lastIndexOf(':')) {
                s += ":";
            }
            if (s.endsWith(":")) {
                s += "SYSTEM";
            }

            Artifact artifact = Artifact.of(s);
            ResolutionRequest request = new ResolutionRequest(artifact);
            request.setPersistentFileNeeded(true);
            requests.add(request);
        }

        return requests;
    }

    private void printResults(ResolverCliRequest cli, List<ResolutionResult> results)
            throws IOException, XMLException {
        if (cli.isRaw()) {
            ResolverDAO.marshalResults(System.out, results);
        } else if (cli.isClasspath()) {
            System.out.println(
                    results.stream()
                            .map(r -> r.getArtifactPath().toString())
                            .collect(Collectors.joining(":")));
        } else {
            results.forEach(r -> System.out.println(r.getArtifactPath()));
        }
    }

    public int run(ResolverCliRequest cliRequest) throws IOException, XMLException {
        try {
            boolean error = false;

            Queue<ResolutionRequest> requestsQueue = new ArrayDeque<>(parseRequests(cliRequest));
            List<ResolutionResult> results = new ArrayList<>();

            Set<Artifact> requestedArtifacts = new LinkedHashSet<>();
            for (ResolutionRequest request : requestsQueue) {
                requestedArtifacts.add(request.getArtifact());
            }

            MetadataResult metadataResult = null;
            if (cliRequest.isRecursive()) {
                Configuration configuration = configurator.getConfiguration();
                ResolverSettings resolverSettings = configuration.getResolverSettings();
                List<String> metadataRepositories = resolverSettings.getMetadataRepositories();
                MetadataRequest metadataRequest = new MetadataRequest(metadataRepositories);
                metadataResult = metadataResolver.resolveMetadata(metadataRequest);
            }

            while (!requestsQueue.isEmpty()) {
                ResolutionRequest request = requestsQueue.remove();
                ResolutionResult result = resolver.resolve(request);
                results.add(result);

                if (result.getArtifactPath() == null) {
                    error = true;
                    logger.error("Unable to resolve artifact {}", request.getArtifact());
                    continue;
                }

                if (cliRequest.isRecursive()) {
                    Artifact requestedArtifact = request.getArtifact();
                    String resolvedVersion = result.getCompatVersion();
                    Artifact resolvedArtifact = requestedArtifact.withVersion(resolvedVersion);
                    ArtifactMetadata artifactMetadata =
                            metadataResult.getMetadataFor(resolvedArtifact);
                    for (Dependency dependency : artifactMetadata.getDependencies()) {
                        Artifact dependencyArtifact =
                                Artifact.of(
                                        dependency.getGroupId(),
                                        dependency.getArtifactId(),
                                        dependency.getExtension(),
                                        dependency.getClassifier(),
                                        dependency.getResolvedVersion());
                        logger.debug(
                                "Also resolving artifact {} as a dependency of {}",
                                dependencyArtifact,
                                requestedArtifact);
                        if (requestedArtifacts.add(dependencyArtifact)) {
                            ResolutionRequest dependencyRequest =
                                    new ResolutionRequest(dependencyArtifact);
                            dependencyRequest.setPersistentFileNeeded(true);
                            requestsQueue.add(dependencyRequest);
                        }
                    }
                }
            }

            if (error && !cliRequest.isRaw()) {
                return 1;
            }

            printResults(cliRequest, results);
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

    public static int doMain(String[] args) {
        return new CommandLine(new ResolverCliRequest()).execute(args);
    }

    public static void main(String[] args) {
        System.exit(doMain(args));
    }
}
