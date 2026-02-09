/*-
 * Copyright (c) 2013-2026 Red Hat, Inc.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author Mikolaj Izdebski
 */
@Command(
        name = "xmvn-subst",
        description = "substitute Maven artifact files with symbolic links.",
        mixinStandardHelpOptions = true,
        versionProvider = SubstCliRequest.class)
final class SubstCliRequest implements Callable<Integer>, IVersionProvider {
    @Parameters private List<String> parameters = new LinkedList<>();

    @Option(
            names = {"-X", "--debug"},
            description = "Display debugging information.")
    private boolean debug;

    @Option(
            names = {"-s", "--strict"},
            description = "Fail if any artifact cannot be symlinked.")
    private boolean strict;

    @Option(
            names = {"-d", "--dry-run"},
            description = "Do not symlink anything but report what would have been symlinked.")
    private boolean dryRun;

    @Option(
            names = {"-L", "--follow-symlinks"},
            description = "Follow symbolic links when traversing directory structure.")
    private boolean followSymlinks;

    @Option(
            names = {"-t", "--type"},
            description = "Consider artifacts with given type.")
    private List<String> types = new ArrayList<>(Arrays.asList("jar", "war"));

    @Option(
            names = {"-R", "--root"},
            description = "Consider another root when looking for artifacts.")
    private String root;

    @Option(names = "-D", description = "Define system property.")
    private Map<String, String> defines = new TreeMap<>();

    public String[] getVersion() throws Exception {
        String ver = "UNKNOWN";
        try (InputStream is =
                SubstCliRequest.class.getResourceAsStream(
                        "/META-INF/maven/org.fedoraproject.xmvn/xmvn-subst/pom.properties")) {
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
        if (isDebug()) {
            System.setProperty("xmvn.debug", "true");
        }
        for (String param : defines.keySet()) System.setProperty(param, defines.get(param));

        ServiceLocator locator = new ServiceLocatorFactory().createServiceLocator();
        Logger logger = locator.getService(Logger.class);
        Configurator configurator = locator.getService(Configurator.class);
        MetadataResolver metadataResolver = locator.getService(MetadataResolver.class);

        SubstCli cli = new SubstCli(logger, configurator, metadataResolver);

        try {
            return cli.run(this);
        } catch (Throwable e) {
            System.err.println("Unhandled exception");
            e.printStackTrace();
            return 2;
        }
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isFollowSymlinks() {
        return followSymlinks;
    }

    public void setFollowSymlinks(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Map<String, String> getDefines() {
        return defines;
    }

    public void setDefines(Map<String, String> defines) {
        this.defines = defines;
    }
}
