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
package org.fedoraproject.xmvn.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class MavenRepositoryTest {
    @Test
    public void testMavenRepository() throws Exception {
        Configuration configuration = new Configuration();
        Repository repository = new Repository();
        repository.setId("test123");
        repository.setType("maven");
        configuration.addRepository(repository);

        Configurator configurator = EasyMock.createMock(Configurator.class);
        EasyMock.expect(configurator.getConfiguration()).andReturn(configuration).atLeastOnce();
        EasyMock.replay(configurator);

        RepositoryConfigurator repoConfigurator = new DefaultRepositoryConfigurator(configurator);
        org.fedoraproject.xmvn.repository.Repository repo =
                repoConfigurator.configureRepository("test123");
        assertNotNull(repo);
        EasyMock.verify(configurator);

        Artifact artifact1 = Artifact.of("foo.bar:the-artifact:baz:1.2.3");
        ArtifactContext context = new ArtifactContext(artifact1);
        assertEquals(
                Path.of("foo/bar/the-artifact/1.2.3/the-artifact-1.2.3.baz"),
                repo.getPrimaryArtifactPath(artifact1, context, "IGNORE-ME"));

        Artifact artifact2 = artifact1.setVersion(null);
        ArtifactContext context2 = new ArtifactContext(artifact2);
        assertNull(repo.getPrimaryArtifactPath(artifact2, context2, "IGNORE-ME"));
    }
}
