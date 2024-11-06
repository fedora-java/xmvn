/*-
 * Copyright (c) 2015-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base class MOJO integration tests.
 *
 * @author Mikolaj Izdebski
 */
public class AbstractMojoIntegrationTest extends AbstractMavenIntegrationTest {
    @BeforeEach
    public void addMetadata() throws Exception {
        PackageMetadata md = new PackageMetadata();

        for (String module : Arrays.asList("xmvn-mojo", "xmvn-core", "xmvn-api", "xmvn-parent")) {
            Path moduleDir = Path.of("../../..").resolve(module);
            Path pomPath = moduleDir.resolve("pom.xml");
            Path jarPath = moduleDir.resolve("target/classes");

            assertTrue(Files.exists(pomPath));
            ArtifactMetadata pomMd = new ArtifactMetadata();
            pomMd.setGroupId("org.fedoraproject.xmvn");
            pomMd.setArtifactId(module);
            pomMd.setVersion("DUMMY_IGNORED");
            pomMd.addProperty("xmvn.resolver.disableEffectivePom", "true");
            pomMd.setExtension("pom");
            pomMd.setPath(pomPath.toString());
            md.addArtifact(pomMd);

            if (Files.exists(jarPath)) {
                ArtifactMetadata jarMd = new ArtifactMetadata();
                jarMd.setGroupId("org.fedoraproject.xmvn");
                jarMd.setArtifactId(module);
                jarMd.setVersion("DUMMY_IGNORED");
                jarMd.addProperty("xmvn.resolver.disableEffectivePom", "true");
                jarMd.setExtension("jar");
                jarMd.setPath(jarPath.toString());
                md.addArtifact(jarMd);
            }
        }

        md.writeToXML(Path.of("mojo-metadata.xml"));

        Configuration conf = new Configuration();
        conf.setResolverSettings(new ResolverSettings());
        conf.getResolverSettings().addMetadataRepository("mojo-metadata.xml");

        Files.createDirectories(Path.of(".xmvn/config.d"));
        conf.writeToXML(Path.of(".xmvn/config.d/mojo-it-conf.xml"));
    }

    public void performMojoTest(String... args) throws Exception {
        String xmvnVersion = getTestProperty("xmvn.version");
        Deque<String> argList = new ArrayDeque<>(Arrays.asList(args));
        String shortGoal = argList.removeLast();
        String fullGoal = "org.fedoraproject.xmvn:xmvn-mojo:" + xmvnVersion + ":" + shortGoal;
        argList.addLast(fullGoal);
        super.performTest(argList);
    }
}
