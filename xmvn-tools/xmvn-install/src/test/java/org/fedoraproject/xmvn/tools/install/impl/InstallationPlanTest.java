/*-
 * Copyright (c) 2014-2026 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.fedoraproject.xmvn.tools.install.impl.InstallationPlanLoader.createInstallationPlan;

import java.nio.file.Path;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Simacek
 */
class InstallationPlanTest {
    @Test
    void nonexistent() throws Exception {
        InstallationPlan plan = new InstallationPlan(Path.of("not-there"));
        assertThat(plan.getArtifacts()).isEmpty();
    }

    @Test
    void valid() throws Exception {
        InstallationPlan plan = createInstallationPlan("valid.xml");
        assertThat(plan.getArtifacts()).hasSize(2);
        assertThat(plan.getArtifacts().get(0).getArtifactId()).isEqualTo("test");
        assertThat(plan.getArtifacts().get(1).getArtifactId()).isEqualTo("test2");
    }

    @Test
    void uuid() throws Exception {
        createInstallationPlan("uuid.xml");
    }

    @Test
    void namespace() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("namespace.xml"));
    }

    @Test
    void alias() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("alias.xml"));
    }

    @Test
    void compat() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("compat.xml"));
    }

    @Test
    void noGroupId() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-gid.xml"));
    }

    @Test
    void noArtifactId() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-aid.xml"));
    }

    @Test
    void noVersion() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-version.xml"));
    }

    @Test
    void noFile() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-file.xml"));
    }

    @Test
    void nonexistenFile() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("nonexistent-file.xml"));
    }

    @Test
    void nonregularFile() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("nonregular-file.xml"));
    }

    @Test
    void nonreadableFile() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("nonreadable-file.xml"));
    }

    @Test
    void noArtifactIdDep() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-aid-dep.xml"));
    }

    @Test
    void noGroupIdDep() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-gid-dep.xml"));
    }

    @Test
    void noVersionDep() throws Exception {
        InstallationPlan plan = createInstallationPlan("no-version-dep.xml");

        List<ArtifactMetadata> artifacts = plan.getArtifacts();
        assertThat(artifacts).hasSize(2);

        List<Dependency> dependencies = artifacts.get(1).getDependencies();
        assertThat(dependencies).hasSize(2);

        assertThat(dependencies.get(0).getRequestedVersion()).isEqualTo("4.1");
        assertThat(dependencies.get(1).getRequestedVersion()).isEqualTo(Artifact.DEFAULT_VERSION);
    }

    @Test
    void namespaceDep() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("namespace-dep.xml"));
    }

    @Test
    void resolvedVersionDep() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("resolved-version.xml"));
    }

    @Test
    void noArtifactIdExclusion() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-aid-excl.xml"));
    }

    @Test
    void noGroupIdExclusion() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("no-gid-excl.xml"));
    }

    @Test
    void skipped() throws Exception {
        assertThatExceptionOfType(ArtifactInstallationException.class)
                .isThrownBy(() -> createInstallationPlan("skipped.xml"));
    }

    @Test
    void metadataUuid() throws Exception {
        createInstallationPlan("metadata-uuid.xml");
    }
}
