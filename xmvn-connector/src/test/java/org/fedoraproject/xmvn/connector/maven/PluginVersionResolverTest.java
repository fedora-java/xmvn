/*-
 * Copyright (c) 2016-2026 Red Hat, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.plugin.version.PluginVersionResult;
import org.easymock.EasyMock;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Roman Vais
 */
class PluginVersionResolverTest {
    private PluginVersionResolver resolver;
    private PluginVersionRequest rq;
    private RepositorySystemSession session;
    private WorkspaceReader reader;
    private WorkspaceRepository repo;

    @BeforeEach
    void setUp() {
        resolver = new XMvnPluginVersionResolver();
        rq = EasyMock.createStrictMock(PluginVersionRequest.class);
        session = EasyMock.createStrictMock(RepositorySystemSession.class);
        reader = EasyMock.createStrictMock(WorkspaceReader.class);
        repo = new WorkspaceRepository();
    }

    @Test
    void injection() throws Exception {
        assertThat(resolver).isExactlyInstanceOf(XMvnPluginVersionResolver.class);
    }

    @Test
    void resolution() throws Exception {
        // test of default version resolution (version list is empty)
        EasyMock.expect(rq.getRepositorySession()).andReturn(session);
        EasyMock.expect(rq.getGroupId()).andReturn("test.example");
        EasyMock.expect(rq.getArtifactId()).andReturn("nonexistent");

        EasyMock.expect(session.getWorkspaceReader()).andReturn(reader);

        EasyMock.expect(reader.findVersions(EasyMock.anyObject(DefaultArtifact.class)))
                .andReturn(List.of());
        EasyMock.expect(reader.getRepository()).andReturn(repo);

        EasyMock.replay(rq, session, reader);

        PluginVersionResult result;

        result = resolver.resolve(rq);
        assertThat(repo).isEqualTo(result.getRepository());
        assertThat(result.getVersion()).isEqualTo(Artifact.DEFAULT_VERSION);

        EasyMock.verify(rq, session, reader);

        // test of arbitrary version resolution (version list contains some version)
        ArrayList<String> list = new ArrayList<>();
        list.add("1.2.3");

        EasyMock.reset(rq, session, reader);
        EasyMock.expect(rq.getRepositorySession()).andReturn(session);
        EasyMock.expect(rq.getGroupId()).andReturn("test.example");
        EasyMock.expect(rq.getArtifactId()).andReturn("nonexistent");

        EasyMock.expect(session.getWorkspaceReader()).andReturn(reader);

        EasyMock.expect(reader.findVersions(EasyMock.anyObject(DefaultArtifact.class)))
                .andReturn(list);
        EasyMock.expect(reader.getRepository()).andReturn(repo);

        EasyMock.replay(rq, session, reader);

        result = resolver.resolve(rq);
        assertThat(repo).isEqualTo(result.getRepository());
        assertThat(result.getVersion()).isEqualTo("1.2.3");

        EasyMock.verify(rq, session, reader);
    }
}
