/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.test.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class MetadataResolverTest extends AbstractTest {
    private MetadataResolver metadataResolver;

    @BeforeEach
    void setUp() {
        metadataResolver = new DefaultMetadataResolver(locator);
    }

    /**
     * Test if metadata resolution works for exact version.
     *
     * @throws Exception
     */
    @Test
    void compatExactVersion() throws Exception {
        List<String> pathList = List.of("src/test/resources/metadata1.xml");
        MetadataResult result = metadataResolver.resolveMetadata(new MetadataRequest(pathList));

        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "1.2-beta3");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(1);
        assertThat(am).isNotNull();
        assertThat(am.getPath()).isEqualTo("/foo/bar");
    }

    /**
     * Test if metadata resolution does not work for inexact versions.
     *
     * @throws Exception
     */
    @Test
    void compatNonExactVersion() throws Exception {
        List<String> pathList = List.of("src/test/resources/metadata1.xml");
        MetadataResult result = metadataResolver.resolveMetadata(new MetadataRequest(pathList));

        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "1.1");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(1);
        assertThat(am).isNull();
    }

    /**
     * Test if metadata resolution works for exact version.
     *
     * @throws Exception
     */
    @Test
    void nonCompatExactVersion() throws Exception {
        List<String> pathList = List.of("src/test/resources/metadata1-non-compat.xml");
        MetadataResult result = metadataResolver.resolveMetadata(new MetadataRequest(pathList));

        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", Artifact.DEFAULT_VERSION);
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(1);
        assertThat(am).isNotNull();
        assertThat(am.getPath()).isEqualTo("/foo/bar");
    }

    /**
     * Test if metadata resolution does not work for inexact versions.
     *
     * @throws Exception
     */
    @Test
    void nonCompatNonExactVersion() throws Exception {
        List<String> pathList = List.of("src/test/resources/metadata1-non-compat.xml");
        MetadataResult result = metadataResolver.resolveMetadata(new MetadataRequest(pathList));

        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "1.1");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(1);
        assertThat(am).isNull();
    }

    @Test
    void repositoryListedTwice() throws Exception {
        String path = "src/test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest(Arrays.asList(path, path));
        MetadataResult result = metadataResolver.resolveMetadata(request);

        Artifact artifact = Artifact.of("org.codehaus.plexus", "plexus-ant-factory", "1.0");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(1);
        assertThat(am).isNotNull();
        assertThat(am.getPath()).isEqualTo("/usr/share/java/plexus/ant-factory-1.0.jar");
    }

    @Test
    void repositoryListedTwiceDifferentPaths() throws Exception {
        String path1 = "src/test/resources/simple.xml";
        String path2 = "src/test/../test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest(Arrays.asList(path1, path2));
        MetadataResult result = metadataResolver.resolveMetadata(request);

        Artifact artifact = Artifact.of("org.codehaus.plexus", "plexus-ant-factory", "1.0");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(2);
        assertThat(am).isNull();
    }

    @Test
    void allowDuplicates() throws Exception {
        String path1 = "src/test/resources/simple.xml";
        String path2 = "src/test/../test/resources/simple.xml";
        MetadataRequest request = new MetadataRequest(Arrays.asList(path1, path2));
        assertThat(request.isIgnoreDuplicates()).isTrue();
        request.setIgnoreDuplicates(false);
        MetadataResult result = metadataResolver.resolveMetadata(request);

        Artifact artifact = Artifact.of("org.codehaus.plexus", "plexus-ant-factory", "1.0");
        ArtifactMetadata am = result.getMetadataFor(artifact);

        assertThat(result.getPackageMetadataMap()).hasSize(2);
        assertThat(am).isNotNull();
        assertThat(am.getPath()).isEqualTo("/usr/share/java/plexus/ant-factory-1.0.jar");
    }
}
