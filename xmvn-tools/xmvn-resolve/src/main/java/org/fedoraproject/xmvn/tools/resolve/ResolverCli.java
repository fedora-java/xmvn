/*-
 * Copyright (c) 2012-2024 Red Hat, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.resolve.xml.ResolutionRequestListUnmarshaller;
import org.fedoraproject.xmvn.tools.resolve.xml.ResolutionResultListMarshaller;

/**
 * Resolve artifacts given on command line.
 *
 * <p>Return 0 when all artifacts are successfully resolved, 1 on failure to resolve one or more
 * artifacts and 2 when some other error occurs. In the last case a stack trace is printed too.
 *
 * @author Mikolaj Izdebski
 */
public class ResolverCli {
    private final Logger logger;

    private final Resolver resolver;

    public ResolverCli(Logger logger, Resolver resolver) {
        this.logger = logger;
        this.resolver = resolver;
    }

    private List<ResolutionRequest> parseRequests(ResolverCliRequest cli)
            throws IOException, XMLStreamException {
        if (cli.isRaw()) {
            List<ResolutionRequest> requests =
                    new ResolutionRequestListUnmarshaller(System.in).unmarshal();
            return requests != null ? requests : List.of();
        }

        List<ResolutionRequest> requests = new ArrayList<>();

        for (String s : cli.getParameters()) {
            if (s.indexOf(':') > 0 && s.indexOf(':') == s.lastIndexOf(':')) {
                s += ":";
            }
            if (s.endsWith(":")) {
                s += "SYSTEM";
            }

            Artifact artifact = new DefaultArtifact(s);
            ResolutionRequest request = new ResolutionRequest(artifact);
            request.setPersistentFileNeeded(true);
            requests.add(request);
        }

        return requests;
    }

    private void printResults(ResolverCliRequest cli, List<ResolutionResult> results)
            throws IOException, XMLStreamException {
        if (cli.isRaw()) {
            new ResolutionResultListMarshaller(results).marshal(System.out);
        } else if (cli.isClasspath()) {
            System.out.println(
                    results.stream()
                            .map(r -> r.getArtifactPath().toString())
                            .collect(Collectors.joining(":")));
        } else {
            results.forEach(r -> System.out.println(r.getArtifactPath()));
        }
    }

    private int run(ResolverCliRequest cliRequest) throws IOException, XMLStreamException {
        try {
            boolean error = false;

            List<ResolutionRequest> requests = parseRequests(cliRequest);
            List<ResolutionResult> results = new ArrayList<>();

            for (ResolutionRequest request : requests) {
                ResolutionResult result = resolver.resolve(request);
                results.add(result);

                if (result.getArtifactPath() == null) {
                    error = true;
                    logger.error("Unable to resolve artifact {}", request.getArtifact());
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
        try {
            ResolverCliRequest cliRequest = ResolverCliRequest.build(args);
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
            Resolver resolver = locator.getService(Resolver.class);

            ResolverCli cli = new ResolverCli(logger, resolver);

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
