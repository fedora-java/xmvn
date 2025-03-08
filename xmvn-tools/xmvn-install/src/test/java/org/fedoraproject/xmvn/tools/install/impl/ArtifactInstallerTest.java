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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.JavaPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Simacek
 */
class ArtifactInstallerTest {
    Repository repositoryMock;

    private ArtifactInstaller installer;

    @BeforeEach
    void configure() {
        repositoryMock = EasyMock.createMock(Repository.class);

        RepositoryConfigurator repoConfigurator =
                new RepositoryConfigurator() {
                    @Override
                    public Repository configureRepository(String repoId) {
                        assertThat(repoId).isEqualTo("my-install-repo");
                        return repositoryMock;
                    }

                    @Override
                    public Repository configureRepository(String repoId, String namespace) {
                        fail("");
                        return null;
                    }
                };

        installer = new DefaultArtifactInstaller(repoConfigurator);
    }

    private void install(JavaPackage pkg, ArtifactMetadata am, PackagingRule rule)
            throws ArtifactInstallationException {
        expect(
                        repositoryMock.getPrimaryArtifactPath(
                                isA(Artifact.class), isA(ArtifactContext.class), isA(String.class)))
                .andReturn(Path.of("com.example-test"));
        expect(repositoryMock.getRootPaths()).andReturn(Set.of());
        expect(repositoryMock.getNamespace()).andReturn("ns");
        replay(repositoryMock);

        installer.install(pkg, am, rule, "foo", "my-install-repo");

        verify(repositoryMock);
    }

    private ArtifactMetadata createArtifact() throws Exception {
        Path sourceJar = Path.of("src/test/resources/example.jar");
        Path tempJar = Path.of("target/test-temp-resources/example.jar");
        Files.createDirectories(tempJar.getParent());
        Files.copy(sourceJar, tempJar, StandardCopyOption.REPLACE_EXISTING);

        ArtifactMetadata artifact = new ArtifactMetadata();
        artifact.setGroupId("com.example");
        artifact.setArtifactId("test");
        artifact.setVersion("4.5");
        artifact.setPath(tempJar.toString());
        return artifact;
    }

    @Test
    void installation() throws Exception {
        ArtifactMetadata artifact = createArtifact();
        JavaPackage pkg = new JavaPackage("", "test", Path.of("usr/share/maven-metadata"));
        PackagingRule rule = new PackagingRule();

        install(pkg, artifact, rule);

        PackageMetadata metadata = pkg.getMetadata();
        assertThat(metadata.getArtifacts()).hasSize(1);
        ArtifactMetadata actualArtifact = metadata.getArtifacts().get(0);
        assertThat(actualArtifact.getNamespace()).isEqualTo("ns");

        assertThat(pkg.getFiles()).hasSize(2);
        Iterator<File> iterator = pkg.getFiles().iterator();
        File file = iterator.next();
        if (file.getTargetPath().equals(Path.of("usr/share/maven-metadata/test.xml"))) {
            file = iterator.next();
        }
        assertThat(file.getTargetPath()).isEqualTo(Path.of("com.example-test"));
        assertThat(artifact.getPath()).isEqualTo("/com.example-test");
    }

    @Test
    void compatVersion() throws Exception {
        ArtifactMetadata artifact = createArtifact();
        JavaPackage pkg = new JavaPackage("", "test", Path.of("usr/share/maven-metadata"));
        PackagingRule rule = new PackagingRule();
        rule.addVersion("3.4");
        rule.addVersion("3");

        install(pkg, artifact, rule);

        PackageMetadata metadata = pkg.getMetadata();
        assertThat(metadata.getArtifacts()).hasSize(1);
        ArtifactMetadata actualArtifact = metadata.getArtifacts().get(0);
        assertThat(actualArtifact.getCompatVersions()).containsExactlyInAnyOrder("3", "3.4");
    }

    @Test
    void aliases() throws Exception {
        ArtifactMetadata artifact = createArtifact();
        JavaPackage pkg = new JavaPackage("", "test", Path.of("usr/share/maven-metadata"));
        PackagingRule rule = new PackagingRule();

        org.fedoraproject.xmvn.config.Artifact alias1 =
                new org.fedoraproject.xmvn.config.Artifact();
        alias1.setGroupId("com.example");
        alias1.setArtifactId("alias1");
        alias1.setVersion("3.4");
        org.fedoraproject.xmvn.config.Artifact alias2 =
                new org.fedoraproject.xmvn.config.Artifact();
        alias2.setGroupId("com.example");
        alias2.setArtifactId("alias2");
        alias2.setClassifier("war");
        rule.addAlias(alias1);
        rule.addAlias(alias2);

        install(pkg, artifact, rule);

        PackageMetadata metadata = pkg.getMetadata();
        assertThat(metadata.getArtifacts()).hasSize(1);
        ArtifactMetadata actualArtifact = metadata.getArtifacts().get(0);
        List<ArtifactAlias> actualAliases = actualArtifact.getAliases();
        assertThat(actualAliases).hasSize(2);
        assertThat(actualAliases.get(0).getGroupId()).isEqualTo("com.example");
        assertThat(actualAliases.get(0).getArtifactId()).isEqualTo("alias1");
        assertThat(actualAliases.get(1).getGroupId()).isEqualTo("com.example");
        assertThat(actualAliases.get(1).getArtifactId()).isEqualTo("alias2");
        assertThat(actualAliases.get(1).getClassifier()).isEqualTo("war");
    }
}
