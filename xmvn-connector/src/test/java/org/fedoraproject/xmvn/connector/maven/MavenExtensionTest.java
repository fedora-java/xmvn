/*-
 * Copyright (c) 2023-2024 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.internal.aether.DefaultRepositorySystemSessionFactory;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.easymock.EasyMock;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResult;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test whether XMvn Connector can be used as Maven extension.
 *
 * @author Mikolaj Izdebski
 */
@SuppressWarnings("deprecation")
public class MavenExtensionTest extends AbstractTest {
    private Resolver resolverMock;

    private RepositorySystemSession session;

    @Override
    public void configure(Binder binder) {
        resolverMock = EasyMock.createMock(org.fedoraproject.xmvn.resolver.Resolver.class);
        binder.bind(WorkspaceReader.class).annotatedWith(Names.named("ide")).to(XMvnWorkspaceReader.class);
        binder.bind(Resolver.class).toInstance(resolverMock);
    }

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws Exception {
        RepositorySystem repoSys = lookup(RepositorySystem.class);
        assertNotNull(repoSys);

        ArtifactRepository repoMock = EasyMock.createMock(ArtifactRepository.class);
        EasyMock.expect(repoMock.getBasedir()).andReturn(tempDir.toString()).anyTimes();
        EasyMock.replay(repoMock);

        MavenExecutionRequest mavenRequest = new DefaultMavenExecutionRequest();
        mavenRequest.setLocalRepository(repoMock);
        DefaultRepositorySystemSessionFactory factory = lookup(DefaultRepositorySystemSessionFactory.class);
        session = factory.newRepositorySession(mavenRequest);
        assertNotNull(session);

        WorkspaceReader workspace = session.getWorkspaceReader();
        assertNotNull(workspace);
        assertInstanceOf(XMvnWorkspaceReader.class, workspace);
    }

    @Test
    public void testVersionResolver() throws Exception {
        ResolutionResult resolutionResult = EasyMock.createMock(ResolutionResult.class);
        EasyMock.expect(resolutionResult.getArtifactPath()).andReturn(Path.of("src/test/resources/dummy.pom"));
        EasyMock.expect(resolutionResult.getCompatVersion()).andReturn("1.2.3-SNAPSHOT");
        EasyMock.replay(resolutionResult);

        EasyMock.expect(resolverMock.resolve(EasyMock.isA(ResolutionRequest.class)))
                .andReturn(resolutionResult)
                .anyTimes();
        EasyMock.replay(resolverMock);

        VersionResolver versionResolver = lookup(VersionResolver.class);
        assertNotNull(versionResolver);

        VersionRequest request = new VersionRequest();
        request.setArtifact(new DefaultArtifact("com.foo:bar:pom:1.2.3-SNAPSHOT"));
        VersionResult result = versionResolver.resolveVersion(session, request);
        String version = result.getVersion();
        assertEquals("1.2.3-SNAPSHOT", version);
        assertTrue(result.getExceptions().isEmpty());

        EasyMock.verify(resolutionResult, resolverMock);
    }

    @Test
    public void testPluginDependenciesResolver() throws Exception {
        PluginDependenciesResolver pluginDepsResolver = lookup(PluginDependenciesResolver.class);
        assertNotNull(pluginDepsResolver);

        Plugin plugin = new Plugin();
        plugin.setGroupId("pgid");
        plugin.setArtifactId("paid");
        plugin.setVersion("pver");

        ResolutionResult resolutionResult = EasyMock.createMock(ResolutionResult.class);
        EasyMock.expect(resolutionResult.getArtifactPath())
                .andReturn(Path.of("src/test/resources/dummy.pom").toAbsolutePath());
        EasyMock.expect(resolutionResult.getArtifactPath())
                .andReturn(Path.of("src/test/resources/dummy.jar").toAbsolutePath());
        EasyMock.replay(resolutionResult);

        EasyMock.expect(resolverMock.resolve(EasyMock.isA(ResolutionRequest.class)))
                .andReturn(resolutionResult)
                .anyTimes();
        EasyMock.replay(resolverMock);

        pluginDepsResolver.resolve(plugin, Collections.emptyList(), session);

        EasyMock.verify(resolutionResult, resolverMock);
    }
}
