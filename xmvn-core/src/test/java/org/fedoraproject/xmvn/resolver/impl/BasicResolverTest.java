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
package org.fedoraproject.xmvn.resolver.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.test.AbstractTest;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * @author Mikolaj Izdebski
 */
public class BasicResolverTest extends AbstractTest {
    /**
     * Test if Plexus can load resolver component.
     *
     * @throws Exception
     */
    @Test
    public void testComponentLookup() throws Exception {
        Resolver resolver = getService(Resolver.class);
        assertNotNull(resolver);
    }

    /**
     * Test if resolver configuration is present and sane.
     *
     * @throws Exception
     */
    @Test
    public void testConfigurationExistance() throws Exception {
        Configurator configurator = getService(Configurator.class);
        assertNotNull(configurator);

        Configuration configuration = configurator.getDefaultConfiguration();
        assertNotNull(configuration);

        ResolverSettings settings = configuration.getResolverSettings();
        assertNotNull(settings);
    }

    /**
     * Test if resolver correctly fails to resolve nonexistent artifact.
     *
     * @throws Exception
     */
    @Test
    public void testResolutionFailure() throws Exception {
        Resolver resolver = getService(Resolver.class);
        ResolutionRequest request =
                new ResolutionRequest(
                        new DefaultArtifact("some", "nonexistent", "pom", "artifact"));
        ResolutionResult result = resolver.resolve(request);
        assertNotNull(result);
        assertNull(result.getArtifactPath());
    }

    @Test
    public void testResolveBasic() throws Exception {
        Artifact artifact = new DefaultArtifact("gid", "aid", "ext", "cla", "ver");
        ArtifactMetadata md = new ArtifactMetadata();
        md.setPath("/foo/bar");

        MetadataResult mockMdResult = EasyMock.createMock(MetadataResult.class);
        MetadataResolver mockMdResolver = EasyMock.createMock(MetadataResolver.class);
        ServiceLocator mockServiceLocator = EasyMock.createMock(ServiceLocator.class);
        EasyMock.expect(mockServiceLocator.getService(Logger.class))
                .andReturn(getService(Logger.class));
        EasyMock.expect(mockServiceLocator.getService(Configurator.class))
                .andReturn(getService(Configurator.class));
        EasyMock.expect(mockServiceLocator.getService(MetadataResolver.class))
                .andReturn(mockMdResolver);
        EasyMock.expect(mockMdResolver.resolveMetadata(EasyMock.anyObject(MetadataRequest.class)))
                .andReturn(mockMdResult);
        EasyMock.expect(mockMdResult.getMetadataFor(artifact)).andReturn(md);
        EasyMock.replay(mockMdResult, mockMdResolver, mockServiceLocator);

        Resolver resolver = new DefaultResolver(mockServiceLocator);
        ResolutionRequest request = new ResolutionRequest(artifact);
        ResolutionResult result = resolver.resolve(request);
        assertNotNull(result);
        assertNotNull(result.getArtifactPath());

        EasyMock.verify(mockMdResult, mockMdResolver, mockServiceLocator);
    }

    @Test
    public void testResolveEmptyPom() throws Exception {
        Artifact artifact = new DefaultArtifact("gid", "aid", "pom", "cla", "ver");
        ArtifactMetadata md = new ArtifactMetadata();
        md.setExtension("pom");

        MetadataResult mockMdResult = EasyMock.createMock(MetadataResult.class);
        MetadataResolver mockMdResolver = EasyMock.createMock(MetadataResolver.class);
        ServiceLocator mockServiceLocator = EasyMock.createMock(ServiceLocator.class);
        EasyMock.expect(mockServiceLocator.getService(Logger.class))
                .andReturn(getService(Logger.class));
        EasyMock.expect(mockServiceLocator.getService(Configurator.class))
                .andReturn(getService(Configurator.class));
        EasyMock.expect(mockServiceLocator.getService(MetadataResolver.class))
                .andReturn(mockMdResolver);
        EasyMock.expect(mockMdResolver.resolveMetadata(EasyMock.anyObject(MetadataRequest.class)))
                .andReturn(mockMdResult);
        EasyMock.expect(mockMdResult.getMetadataFor(artifact)).andReturn(md);
        EasyMock.replay(mockMdResult, mockMdResolver, mockServiceLocator);

        Resolver resolver = new DefaultResolver(mockServiceLocator);
        ResolutionRequest request = new ResolutionRequest(artifact);
        request.setPersistentFileNeeded(true);
        ResolutionResult result = resolver.resolve(request);
        assertNotNull(result);
        assertNotNull(result.getArtifactPath());
        assertTrue(Files.isRegularFile(result.getArtifactPath()));

        EasyMock.verify(mockMdResult, mockMdResolver, mockServiceLocator);

        XmlAssert.assertThat(
                        """
                        <project>
                          <modelVersion>4.0.0</modelVersion>
                          <groupId>gid</groupId>
                          <artifactId>aid</artifactId>
                          <version>ver</version>
                        </project>
                        """)
                .and(result.getArtifactPath().toFile())
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();
    }

    @Test
    public void testResolvePomWithDep() throws Exception {
        Artifact artifact = new DefaultArtifact("gid", "aid", "pom", "cla", "ver");
        ArtifactMetadata md = new ArtifactMetadata();
        md.setExtension("pom");
        Dependency dep = new Dependency();
        dep.setGroupId("dgid");
        dep.setArtifactId("daid");
        dep.setRequestedVersion("drqv");
        dep.setResolvedVersion("drsv");
        DependencyExclusion excl = new DependencyExclusion();
        excl.setGroupId("egid");
        excl.setArtifactId("eaid");
        dep.addExclusion(excl);
        md.addDependency(dep);

        MetadataResult mockMdResult = EasyMock.createMock(MetadataResult.class);
        MetadataResolver mockMdResolver = EasyMock.createMock(MetadataResolver.class);
        ServiceLocator mockServiceLocator = EasyMock.createMock(ServiceLocator.class);
        EasyMock.expect(mockServiceLocator.getService(Logger.class))
                .andReturn(getService(Logger.class));
        EasyMock.expect(mockServiceLocator.getService(Configurator.class))
                .andReturn(getService(Configurator.class));
        EasyMock.expect(mockServiceLocator.getService(MetadataResolver.class))
                .andReturn(mockMdResolver);
        EasyMock.expect(mockMdResolver.resolveMetadata(EasyMock.anyObject(MetadataRequest.class)))
                .andReturn(mockMdResult);
        EasyMock.expect(mockMdResult.getMetadataFor(artifact)).andReturn(md);
        EasyMock.replay(mockMdResult, mockMdResolver, mockServiceLocator);

        Resolver resolver = new DefaultResolver(mockServiceLocator);
        ResolutionRequest request = new ResolutionRequest(artifact);
        request.setPersistentFileNeeded(true);
        ResolutionResult result = resolver.resolve(request);
        assertNotNull(result);
        assertNotNull(result.getArtifactPath());
        assertTrue(Files.isRegularFile(result.getArtifactPath()));

        EasyMock.verify(mockMdResult, mockMdResolver, mockServiceLocator);

        XmlAssert.assertThat(
                        """
                        <project>
                          <modelVersion>4.0.0</modelVersion>
                          <groupId>gid</groupId>
                          <artifactId>aid</artifactId>
                          <version>ver</version>
                          <dependencies>
                            <dependency>
                              <groupId>dgid</groupId>
                              <artifactId>daid</artifactId>
                              <version>drqv</version>
                              <exclusions>
                                <exclusion>
                                  <groupId>egid</groupId>
                                  <artifactId>eaid</artifactId>
                                </exclusion>
                              </exclusions>
                            </dependency>
                          </dependencies>
                        </project>
                        """)
                .and(result.getArtifactPath().toFile())
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();
    }

    @Test
    public void testMockAgent() throws Exception {
        Artifact artifact = new DefaultArtifact("gid", "aid", "ext", "cla", "ver");
        Artifact versionlessArtifact =
                new DefaultArtifact("gid", "aid", "ext", "cla", Artifact.DEFAULT_VERSION);
        ArtifactMetadata md = new ArtifactMetadata();
        md.setPath("/foo/bar");

        MockAgent mockAgent = EasyMock.createMock(MockAgent.class);
        EasyMock.expect(mockAgent.tryInstallArtifact(artifact)).andReturn(true);
        MetadataResult mockMdResult1 = EasyMock.createMock(MetadataResult.class);
        MetadataResult mockMdResult2 = EasyMock.createMock(MetadataResult.class);
        MetadataResolver mockMdResolver = EasyMock.createMock(MetadataResolver.class);
        ServiceLocator mockServiceLocator = EasyMock.createMock(ServiceLocator.class);
        EasyMock.expect(mockServiceLocator.getService(Logger.class))
                .andReturn(getService(Logger.class));
        EasyMock.expect(mockServiceLocator.getService(Configurator.class))
                .andReturn(getService(Configurator.class));
        EasyMock.expect(mockServiceLocator.getService(MetadataResolver.class))
                .andReturn(mockMdResolver);
        EasyMock.expect(mockMdResolver.resolveMetadata(EasyMock.anyObject(MetadataRequest.class)))
                .andReturn(mockMdResult1);
        EasyMock.expect(mockMdResolver.resolveMetadata(EasyMock.anyObject(MetadataRequest.class)))
                .andReturn(mockMdResult2);
        EasyMock.expect(mockMdResult1.getMetadataFor(artifact)).andReturn(null);
        EasyMock.expect(mockMdResult1.getMetadataFor(versionlessArtifact)).andReturn(null);
        EasyMock.expect(mockMdResult2.getMetadataFor(artifact)).andReturn(null);
        EasyMock.expect(mockMdResult2.getMetadataFor(versionlessArtifact)).andReturn(md);
        EasyMock.replay(
                mockAgent, mockMdResult1, mockMdResult2, mockMdResolver, mockServiceLocator);

        DefaultResolver resolver = new DefaultResolver(mockServiceLocator);
        resolver.mockAgent = mockAgent;
        ResolutionRequest request = new ResolutionRequest(artifact);
        ResolutionResult result = resolver.resolve(request);
        assertNotNull(result);
        assertEquals(Path.of("/foo/bar"), result.getArtifactPath());

        EasyMock.verify(
                mockAgent, mockMdResult1, mockMdResult2, mockMdResolver, mockServiceLocator);
    }
}
