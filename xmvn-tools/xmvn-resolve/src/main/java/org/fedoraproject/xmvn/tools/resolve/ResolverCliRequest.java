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

import java.io.InputStream;
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
import org.fedoraproject.xmvn.resolver.Resolver;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author Mikolaj Izdebski
 */
@Command(
        name = "xmvn-resolve",
        description = "Resolve Maven artifacts from system repositories.",
        mixinStandardHelpOptions = true,
        versionProvider = ResolverCliRequest.class)
final class ResolverCliRequest implements Callable<Integer>, IVersionProvider {
    @Parameters(paramLabel = "artifacts", description = "Artifact coordinates to resolve.")
    private List<String> parameters = new LinkedList<>();

    @Option(
            names = {"-X", "--debug"},
            description = "Display debugging information.")
    private boolean debug;

    @Option(
            names = {"-c", "--classpath"},
            description = "Use colon instead of new line to separate resolved artifacts.")
    private boolean classpath;

    @Option(
            names = {"-r", "--recursive"},
            description =
                    "Also include all runtime dependencies of specified artifacts, recursively.")
    private boolean recursive;

    @Option(
            names = {"--raw-request"},
            description =
                    "Read a list of raw XMvn XML requests from standard input and print the results on standard output.")
    private boolean raw;

    @Option(names = "-D", description = "Define system property.")
    private Map<String, String> defines = new TreeMap<>();

    public String[] getVersion() throws Exception {
        String ver = "UNKNOWN";
        try (InputStream is =
                ResolverCliRequest.class.getResourceAsStream(
                        "/META-INF/maven/org.fedoraproject.xmvn/xmvn-resolve/pom.properties")) {
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

        if (raw && (classpath || !parameters.isEmpty() || recursive)) {
            throw new IllegalArgumentException("--raw-request must be used alone");
        }

        for (String param : defines.keySet()) {
            System.setProperty(param, defines.get(param));
        }

        ServiceLocator locator = new ServiceLocatorFactory().createServiceLocator();
        Logger logger = locator.getService(Logger.class);
        Resolver resolver = locator.getService(Resolver.class);
        Configurator configurator = locator.getService(Configurator.class);
        MetadataResolver metadataResolver = locator.getService(MetadataResolver.class);

        ResolverCli cli = new ResolverCli(logger, resolver, configurator, metadataResolver);

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

    public boolean isClasspath() {
        return classpath;
    }

    public void setClasspath(boolean classpath) {
        this.classpath = classpath;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public Map<String, String> getDefines() {
        return defines;
    }

    public void setDefines(Map<String, String> defines) {
        this.defines = defines;
    }
}
