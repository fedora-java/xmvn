/*-
 * Copyright (c) 2018-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.junit.jupiter.api.Test;

/**
 * @author Marian Koncek
 */
class ResolutionRequestUnmarshallerListTest {
    private final String resourcePath =
            "src/test/resources/org/fedoraproject/xmvn/tools/resolve/xml";

    @Test
    void emptierList() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-emptier-list.xml")) {
            assertThat(ResolverDAO.unmarshalRequests(is)).isEmpty();
        }
    }

    @Test
    void emptyList() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-empty-list.xml")) {
            assertThat(ResolverDAO.unmarshalRequests(is)).isEmpty();
        }
    }

    @Test
    void fullArtifact() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-full-artifact.xml")) {
            List<ResolutionRequest> list = ResolverDAO.unmarshalRequests(is);
            assertThat(list).hasSize(1);

            Artifact artifact = list.get(0).getArtifact();

            assertThat(artifact.getArtifactId()).isEqualTo("test1");
            assertThat(artifact.getGroupId()).isEqualTo("test1");
            assertThat(artifact.getExtension()).isEqualTo("test1");
            assertThat(artifact.getClassifier()).isEqualTo("test1");
            assertThat(artifact.getVersion()).isEqualTo("test1");
            assertThat(artifact.getPath().toString()).isEqualTo("/dev/null");
        }
    }

    @Test
    void fullRequests() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-full-requests.xml")) {
            List<ResolutionRequest> list = ResolverDAO.unmarshalRequests(is);
            assertThat(list).hasSize(6);

            assertThat(list.get(0).isProviderNeeded()).isTrue();
            assertThat(list.get(0).isPersistentFileNeeded()).isTrue();

            assertThat(list.get(1).isProviderNeeded()).isFalse();
            assertThat(list.get(1).isPersistentFileNeeded()).isFalse();

            assertThat(list.get(2).isProviderNeeded()).isFalse();
            assertThat(list.get(2).isPersistentFileNeeded()).isTrue();

            assertThat(list.get(3).isProviderNeeded()).isTrue();
            assertThat(list.get(3).isPersistentFileNeeded()).isFalse();

            assertThat(list.get(4).isPersistentFileNeeded()).isTrue();

            assertThat(list.get(5).isProviderNeeded()).isTrue();
        }
    }

    @Test
    void integrationExample() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-integration-example.xml")) {
            List<ResolutionRequest> list = ResolverDAO.unmarshalRequests(is);
            assertThat(list).hasSize(2);

            assertThat(list.get(0).getArtifact().getGroupId()).isEqualTo("foobar");
            assertThat(list.get(0).getArtifact().getArtifactId()).isEqualTo("xyzzy");

            assertThat(list.get(1).getArtifact().getArtifactId()).isEqualTo("junit");
            assertThat(list.get(1).getArtifact().getGroupId()).isEqualTo("junit");
        }
    }

    @Test
    void minimalArtifacts() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-minimal-artifacts.xml")) {
            List<ResolutionRequest> list = ResolverDAO.unmarshalRequests(is);
            assertThat(list).hasSize(5);

            int artifactNum = 1;
            for (ResolutionRequest rr : list) {
                Artifact artifact = rr.getArtifact();
                assertThat(artifact.getArtifactId()).isEqualTo("test" + artifactNum);
                assertThat(artifact.getGroupId()).isEqualTo("test" + artifactNum);
                ++artifactNum;
            }
        }
    }

    @Test
    void nestedBrackets() throws Exception {
        try (InputStream is = new FileInputStream(resourcePath + "/test-nested-brackets.xml")) {
            List<ResolutionRequest> list = ResolverDAO.unmarshalRequests(is);
            assertThat(list).hasSize(1);

            assertThat(list.get(0).isPersistentFileNeeded()).isFalse();

            list.get(0).getArtifact();
            list.get(0).getArtifact().getExtension();
            assertThat(list.get(0).getArtifact().getExtension()).isEqualTo("jar");
            assertThat(list.get(0).getArtifact().getArtifactId())
                    .isEqualTo("aliased-component-metadata");
            assertThat(list.get(0).getArtifact().getVersion()).isEqualTo("any");
            assertThat(list.get(0).getArtifact().getGroupId()).isEqualTo("alias-test");

            assertThat(list.get(0).isProviderNeeded()).isFalse();
        }
    }
}
