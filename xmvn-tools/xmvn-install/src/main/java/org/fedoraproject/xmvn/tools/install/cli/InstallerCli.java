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
package org.fedoraproject.xmvn.tools.install.cli;

import java.io.IOException;
import java.nio.file.Path;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.Installer;
import org.fedoraproject.xmvn.tools.install.impl.DefaultInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
public class InstallerCli {
    private final Logger logger = LoggerFactory.getLogger(InstallerCli.class);

    private final Installer installer;

    public InstallerCli(Installer installer) {
        this.installer = installer;
    }

    private int run(InstallerCliRequest cliRequest) {
        InstallationRequest request = new InstallationRequest();
        request.setCheckForUnmatchedRules(!cliRequest.isRelaxed());
        request.setBasePackageName(cliRequest.getPackageName());
        request.setInstallRoot(Path.of(cliRequest.getDestDir()));
        request.setInstallationPlan(Path.of(cliRequest.getPlanPath()));
        request.setRepositoryId(cliRequest.getRepoId());

        try {
            installer.install(request);
            return 0;
        } catch (ArtifactInstallationException | IOException e) {
            logger.error("Artifact installation failed", e);
            return 1;
        }
    }

    public static int doMain(String[] args) {
        try {
            InstallerCliRequest cliRequest = InstallerCliRequest.build(args);
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
            Configurator configurator = locator.getService(Configurator.class);
            Resolver resolver = locator.getService(Resolver.class);

            Installer installer = new DefaultInstaller(configurator, resolver);
            InstallerCli cli = new InstallerCli(installer);

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
