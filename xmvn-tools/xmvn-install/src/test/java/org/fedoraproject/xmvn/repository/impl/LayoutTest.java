/*-
 * Copyright (c) 2012-2026 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Properties;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class LayoutTest {
    private Repository mavenRepository;

    private Repository jppRepository;

    @BeforeEach
    void setUp() throws Exception {
        mavenRepository = new MavenRepositoryFactory().getInstance(null, new Properties(), null);
        jppRepository = new JppRepositoryFactory().getInstance(null, new Properties(), null);

        assertThat(mavenRepository).isNotNull();
        assertThat(jppRepository).isNotNull();
    }

    private void testPaths(Repository repository, Artifact artifact, String expected) {
        ArtifactContext context = new ArtifactContext(artifact);
        Path repoPath =
                repository.getPrimaryArtifactPath(
                        artifact, context, artifact.getGroupId() + "/" + artifact.getArtifactId());

        if (expected == null) {
            assertThat(repoPath).isNull();
        } else {
            assertThat(repoPath).isNotNull();
            assertThat(repoPath.toString()).isEqualTo(expected);
        }
    }

    /**
     * Test layout objects.
     *
     * @throws Exception
     */
    @Test
    void layouts() throws Exception {
        Artifact artifact =
                Artifact.of("an-example.artifact:used-FOR42.testing:ext-ens.ion:blah-1.2.3-foo");

        testPaths(
                mavenRepository,
                artifact,
                "an-example/artifact/used-FOR42.testing/blah-1.2.3-foo/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion");
        testPaths(mavenRepository, artifact.withVersion("SYSTEM"), null);
        testPaths(
                jppRepository,
                artifact,
                "an-example.artifact/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion");
        testPaths(
                jppRepository,
                artifact.withVersion("SYSTEM"),
                "an-example.artifact/used-FOR42.testing.ext-ens.ion");
    }

    /**
     * Test is JPP prefixes in groupId are handled correctly.
     *
     * @throws Exception
     */
    @Test
    void jppPrefixes() throws Exception {
        Artifact artifact1 = Artifact.of("JPP:testing:abc:1.2.3");
        Artifact artifact2 = Artifact.of("JPP/group:testing:abc:1.2.3");
        Artifact artifact3 = Artifact.of("JPP-group:testing:abc:1.2.3");

        testPaths(jppRepository, artifact1.withVersion("SYSTEM"), "testing.abc");
        testPaths(jppRepository, artifact2.withVersion("SYSTEM"), "group/testing.abc");
        testPaths(jppRepository, artifact3.withVersion("SYSTEM"), "JPP-group/testing.abc");
    }
}
