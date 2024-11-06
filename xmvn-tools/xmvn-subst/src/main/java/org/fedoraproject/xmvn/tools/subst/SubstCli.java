/*-
 * Copyright (c) 2013-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;

/**
 * @author Mikolaj Izdebski
 */
public class SubstCli {
    private final Logger logger;

    private final MetadataResolver metadataResolver;

    private final ResolverSettings resolverSettings;

    public SubstCli(Logger logger, Configurator configurator, MetadataResolver metadataResolver) {
        this.logger = logger;
        this.metadataResolver = metadataResolver;
        resolverSettings = configurator.getConfiguration().getResolverSettings();
    }

    private MetadataResult resolveMetadata(List<String> repos) {
        MetadataRequest request = new MetadataRequest(repos);
        request.setIgnoreDuplicates(resolverSettings.isIgnoreDuplicateMetadata());
        return metadataResolver.resolveMetadata(request);
    }

    private int run(SubstCliRequest cliRequest) {
        List<MetadataResult> metadataResults = new ArrayList<>();

        if (cliRequest.getRoot() != null) {
            List<String> metadataRepos = new ArrayList<>();
            Path root = Path.of(cliRequest.getRoot());

            for (String configuredRepo : resolverSettings.getMetadataRepositories()) {
                Path repoPath = Path.of(configuredRepo);
                if (repoPath.isAbsolute()) {
                    metadataRepos.add(root.resolve(Path.of("/").relativize(repoPath)).toString());
                }
            }

            metadataResults.add(resolveMetadata(metadataRepos));
        }

        metadataResults.add(resolveMetadata(resolverSettings.getMetadataRepositories()));

        ArtifactVisitor visitor = new ArtifactVisitor(logger, metadataResults);

        visitor.setTypes(cliRequest.getTypes());
        visitor.setFollowSymlinks(cliRequest.isFollowSymlinks());
        visitor.setDryRun(cliRequest.isDryRun());

        try {
            for (String path : cliRequest.getParameters()) {
                Files.walkFileTree(Path.of(path), visitor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cliRequest.isStrict() && visitor.getFailureCount() > 0 ? 1 : 0;
    }

    public static int doMain(String[] args) {
        try {
            SubstCliRequest cliRequest = SubstCliRequest.build(args);
            if (cliRequest == null) {
                return 1;
            }
            if (cliRequest.printUsage()) {
                return 0;
            }
            if (cliRequest.isDebug()) {
                System.setProperty("xmvn.debug", "true");
            }

            ServiceLocator locator = new ServiceLocatorFactory().createServiceLocator();
            Logger logger = locator.getService(Logger.class);
            Configurator configurator = locator.getService(Configurator.class);
            MetadataResolver metadataResolver = locator.getService(MetadataResolver.class);

            SubstCli cli = new SubstCli(logger, configurator, metadataResolver);

            return cli.run(cliRequest);
        } catch (Throwable e) {
            System.err.println("Unhandled exception");
            e.printStackTrace();
            return 2;
        }
    }

    public static void main(String[] args) {
        System.exit(doMain(args));
    }
}
