/*-
 * Copyright (c) 2020-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.api.Session;
import org.apache.maven.api.services.ToolchainManager;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnToolchainManager {
    @Inject private ToolchainManager toolchainManager;

    public void activate(Session session) throws MavenExecutionException {
        for (var toolchain : toolchainManager.getToolchainsForType(session, "jdk")) {
            if (toolchain.matchesRequirements(Collections.singletonMap("xmvn", "xmvn"))) {
                toolchainManager.storeToolchainToBuildContext(session, toolchain);
            }
        }
    }
}
