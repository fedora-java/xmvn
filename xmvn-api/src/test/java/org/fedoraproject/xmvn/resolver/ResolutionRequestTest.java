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
package org.fedoraproject.xmvn.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Roman Vais
 */
class ResolutionRequestTest {
    private Artifact artifact;

    private final ResolutionRequest rrq = new ResolutionRequest();

    @BeforeEach
    void setUp() {
        artifact = EasyMock.createMock(Artifact.class);
        rrq.setArtifact(null);
        rrq.setProviderNeeded(false);
        rrq.setPersistentFileNeeded(false);
    }

    /** Test of get and set methods */
    @Test
    void getSetTest() throws Exception {
        // tests set and get artifact
        rrq.setArtifact(artifact);
        assertThat(artifact).isSameAs(rrq.getArtifact());

        // tests set and get 'ProviderNeeded'
        rrq.setProviderNeeded(true);
        assertThat(rrq.isProviderNeeded()).isTrue();
        rrq.setProviderNeeded(false);
        assertThat(rrq.isProviderNeeded()).isFalse();

        // tests set and get 'PersistentFileNeeded'
        rrq.setPersistentFileNeeded(true);
        assertThat(rrq.isPersistentFileNeeded()).isTrue();
        rrq.setPersistentFileNeeded(false);
        assertThat(rrq.isPersistentFileNeeded()).isFalse();
    }

    /** Test of constructor wit artifact argument */
    @Test
    void extraConstructorTest() throws Exception {
        ResolutionRequest extraRq = new ResolutionRequest(artifact);
        assertThat(artifact).isSameAs(extraRq.getArtifact());
    }

    /** Test of equality method */
    @Test
    void equalityTest() throws Exception {
        assertThat(rrq).isEqualTo(rrq);
        assertThat(rrq).isNotEqualTo(null);
        assertThat(new Object()).isNotEqualTo(rrq);

        ResolutionRequest extraRq = new ResolutionRequest();
        assertThat(extraRq).isEqualTo(rrq);

        extraRq.setArtifact(artifact);
        assertThat(extraRq).isNotEqualTo(rrq);

        rrq.setArtifact(artifact);
        assertThat(extraRq).isEqualTo(rrq);

        rrq.setProviderNeeded(true);
        assertThat(extraRq).isNotEqualTo(rrq);

        rrq.setProviderNeeded(false);
        rrq.setPersistentFileNeeded(true);
        assertThat(extraRq).isNotEqualTo(rrq);

        extraRq.setArtifact(EasyMock.createMock(Artifact.class));
        assertThat(extraRq).isNotEqualTo(rrq);
    }
}
