/*-
 * Copyright (c) 2020-2026 Red Hat, Inc.
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
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.api.Project;
import org.apache.maven.api.Session;
import org.apache.maven.api.SessionData;
import org.apache.maven.api.Toolchain;
import org.apache.maven.api.services.ToolchainManager;
import org.apache.maven.execution.MavenSession;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class XMvnToolchainManager extends AbstractMavenLifecycleParticipant {

    private final ToolchainManager toolchainManager;

    @Inject
    public XMvnToolchainManager(ToolchainManager toolchainManager) {
        this.toolchainManager = toolchainManager;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void afterProjectsRead(MavenSession mavenSession) {
        Session session = mavenSession.getSession();
        SessionData.Key<ConcurrentHashMap<Project, ConcurrentHashMap<String, Object>>>
                toolchainContextKey =
                        (SessionData.Key)
                                SessionData.key(ConcurrentHashMap.class, "toolchain-context");
        for (Toolchain toolchain : toolchainManager.getToolchains(session, "jdk")) {
            if (toolchain.matchesRequirements(Collections.singletonMap("xmvn", "xmvn"))) {
                for (Project project : session.getProjects()) {
                    session.getData()
                            .computeIfAbsent(toolchainContextKey, ConcurrentHashMap::new)
                            .computeIfAbsent(project, p -> new ConcurrentHashMap<>())
                            .put("toolchain-" + toolchain.getType(), toolchain.getModel());
                }
            }
        }
    }
}
