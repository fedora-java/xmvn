/*-
 * Copyright (c) 2016-2025 Red Hat, Inc.
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

import com.google.inject.Binder;
import java.nio.file.Path;
import java.util.List;
import org.easymock.EasyMock;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class WorkspaceReaderTest extends AbstractTest {
    private WorkspaceReader workspace;
    private Resolver resolver;

    @Override
    public void configure(Binder binder) {
        // Nothing to do
    }

    @BeforeEach
    void setUp() throws Exception {
        resolver = EasyMock.createMock(Resolver.class);
        getContainer().addComponent(resolver, Resolver.class, "default");
        workspace = lookup(WorkspaceReader.class, "ide");
    }

    @Test
    void dependencyInjection() throws Exception {
        assertThat(workspace).isExactlyInstanceOf(XMvnWorkspaceReader.class);
    }

    @Test
    void findArtifact() throws Exception {
        ResolutionRequest request = new ResolutionRequest(Artifact.of("foo:bar:1.2.3"));
        ResolutionResult result = EasyMock.createMock(ResolutionResult.class);

        EasyMock.expect(resolver.resolve(request)).andReturn(result);
        EasyMock.expect(result.getArtifactPath()).andReturn(Path.of("/foo/bar"));
        EasyMock.replay(resolver, result);

        Path path =
                workspace.findArtifactPath(
                        new org.eclipse.aether.artifact.DefaultArtifact("foo:bar:1.2.3"));
        EasyMock.verify(resolver, result);

        assertThat(path).isEqualTo(Path.of("/foo/bar"));
    }

    @Test
    void artifactNotFound() throws Exception {
        ResolutionRequest request = new ResolutionRequest(Artifact.of("foo:bar:1.2.3"));
        ResolutionResult result = EasyMock.createMock(ResolutionResult.class);

        EasyMock.expect(resolver.resolve(request)).andReturn(result);
        EasyMock.expect(result.getArtifactPath()).andReturn(null);
        EasyMock.replay(resolver, result);

        Path path =
                workspace.findArtifactPath(
                        new org.eclipse.aether.artifact.DefaultArtifact("foo:bar:1.2.3"));
        EasyMock.verify(resolver, result);

        assertThat(path).isNull();
    }

    @Test
    void findVersionsSystem() throws Exception {
        ResolutionRequest request = new ResolutionRequest(Artifact.of("foo:bar:1.2.3"));
        ResolutionResult result = EasyMock.createMock(ResolutionResult.class);

        EasyMock.expect(resolver.resolve(request)).andReturn(result);
        EasyMock.expect(result.getArtifactPath()).andReturn(Path.of("/foo/bar"));
        EasyMock.expect(result.getCompatVersion()).andReturn(null);
        EasyMock.replay(resolver, result);

        List<String> versions =
                workspace.findVersions(
                        new org.eclipse.aether.artifact.DefaultArtifact("foo:bar:1.2.3"));
        EasyMock.verify(resolver, result);

        assertThat(versions).hasSize(1);
        assertThat(versions.get(0)).isEqualTo("SYSTEM");
    }

    @Test
    void findVersionsCompat() throws Exception {
        ResolutionRequest request = new ResolutionRequest(Artifact.of("foo:bar:1.2.3"));
        ResolutionResult result = EasyMock.createMock(ResolutionResult.class);

        EasyMock.expect(resolver.resolve(request)).andReturn(result);
        EasyMock.expect(result.getArtifactPath()).andReturn(Path.of("/foo/bar"));
        EasyMock.expect(result.getCompatVersion()).andReturn("4.5.6");
        EasyMock.replay(resolver, result);

        List<String> versions =
                workspace.findVersions(
                        new org.eclipse.aether.artifact.DefaultArtifact("foo:bar:1.2.3"));
        EasyMock.verify(resolver, result);

        assertThat(versions).hasSize(1);
        assertThat(versions.get(0)).isEqualTo("4.5.6");
    }

    @Test
    void findVersionsNotFound() throws Exception {
        ResolutionRequest request = new ResolutionRequest(Artifact.of("foo:bar:1.2.3"));
        ResolutionResult result = EasyMock.createMock(ResolutionResult.class);

        EasyMock.expect(resolver.resolve(request)).andReturn(result);
        EasyMock.expect(result.getArtifactPath()).andReturn(null);
        EasyMock.replay(resolver, result);

        List<String> versions =
                workspace.findVersions(
                        new org.eclipse.aether.artifact.DefaultArtifact("foo:bar:1.2.3"));
        EasyMock.verify(resolver, result);

        assertThat(versions).isEmpty();
    }

    @Test
    void getRepository() throws Exception {
        WorkspaceRepository repository = workspace.getRepository();
        assertThat(repository).isNotNull();
    }
}
