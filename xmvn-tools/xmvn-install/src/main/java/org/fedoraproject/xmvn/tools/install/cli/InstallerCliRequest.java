/*-
 * Copyright (c) 2013-2025 Red Hat, Inc.
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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.Installer;
import org.fedoraproject.xmvn.tools.install.impl.DefaultInstaller;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

/**
 * @author Mikolaj Izdebski
 */
@Command(
        name = "xmvn-install",
        description = "Install artifacts into system repository.",
        mixinStandardHelpOptions = true,
        versionProvider = InstallerCliRequest.class)
final class InstallerCliRequest implements Callable<Integer>, IVersionProvider {
    @Option(
            names = {"-X", "--debug"},
            description = "Display debugging information.")
    private boolean debug;

    @Option(
            names = {"-r", "--relaxed"},
            description = "Skip strict rule checking.")
    private boolean relaxed;

    @Option(
            names = {"-R", "--reactor"},
            description = "Path to reactor descriptor.")
    private String planPath = ".xmvn/reactor.xml";

    @Option(
            names = {"-n", "--name"},
            description = "Base package name.")
    private String packageName = "pkgname";

    @Option(
            names = {"-d", "--destination"},
            description = "Destination directory.")
    private String destDir = ".xmvn/root";

    @Option(
            names = {"-i", "--repository"},
            description = "Installation repository ID.")
    private String repoId = ArtifactInstaller.DEFAULT_REPOSITORY_ID;

    @Option(names = "-D", description = "Define system property.")
    private Map<String, String> defines = new TreeMap<>();

    public String[] getVersion() throws Exception {
        String ver = "UNKNOWN";
        try (InputStream is =
                InstallerCliRequest.class.getResourceAsStream(
                        "/META-INF/maven/org.fedoraproject.xmvn/xmvn-install/pom.properties")) {
            if (is != null) {
                Properties properties = new Properties();
                properties.load(is);
                ver = properties.getProperty("version");
            }
        }
        return new String[] {
            "${COMMAND-FULL-NAME} version " + ver,
            "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
            "OS: ${os.name} ${os.version} ${os.arch}"
        };
    }

    public Integer call() {

        if (debug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
            System.setProperty("xmvn.debug", "true");
        }
        for (String param : defines.keySet()) System.setProperty(param, defines.get(param));

        ServiceLocator locator = new ServiceLocatorFactory().createServiceLocator();
        Configurator configurator = locator.getService(Configurator.class);
        Resolver resolver = locator.getService(Resolver.class);

        Installer installer = new DefaultInstaller(configurator, resolver);
        InstallerCli cli = new InstallerCli(installer);

        try {
            return cli.run(this);
        } catch (Throwable e) {
            System.err.println("Unhandled exception");
            e.printStackTrace();
            return 2;
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isRelaxed() {
        return relaxed;
    }

    public void setRelaxed(boolean relaxed) {
        this.relaxed = relaxed;
    }

    public String getPlanPath() {
        return planPath;
    }

    public void setPlanPath(String planPath) {
        this.planPath = planPath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public Map<String, String> getDefines() {
        return defines;
    }

    public void setDefines(Map<String, String> defines) {
        this.defines = defines;
    }
}
