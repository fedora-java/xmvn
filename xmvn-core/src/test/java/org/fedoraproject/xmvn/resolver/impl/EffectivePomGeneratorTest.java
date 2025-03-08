/*-
 * Copyright (c) 2025 Red Hat, Inc.
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

import java.util.List;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

class EffectivePomGeneratorTest {

    private final EffectivePomGenerator epg = new EffectivePomGenerator();

    private void performTest(Artifact art, List<Dependency> deps, String expectXml)
            throws Exception {
        ArtifactMetadata amd = EasyMock.createStrictMock(ArtifactMetadata.class);
        EasyMock.expect(amd.getDependencies()).andReturn(deps);
        EasyMock.replay(amd);
        String xml = epg.generateEffectivePom(amd, art);
        EasyMock.verify(amd);
        XmlAssert.assertThat(xml).and(expectXml).ignoreComments().ignoreWhitespace().areSimilar();
    }

    @Test
    void effectivePomSimple() throws Exception {
        performTest(
                Artifact.of("gid", "aid", "ver"),
                List.of(),
                """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>gid</groupId>
                  <artifactId>aid</artifactId>
                  <version>ver</version>
                </project>
                """);
    }

    @Test
    void effectivePomDependency() throws Exception {
        Dependency dep = new Dependency();
        dep.setGroupId("dgid");
        dep.setArtifactId("daid");
        dep.setRequestedVersion("reqver");
        dep.setResolvedVersion("resver");
        dep.setExtension("dext");
        dep.setClassifier("dcla");
        dep.setOptional(true);
        performTest(
                Artifact.of("gid", "aid", "ver"),
                List.of(dep),
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
                      <type>dext</type>
                      <classifier>dcla</classifier>
                      <version>reqver</version>
                      <optional>true</optional>
                    </dependency>
                  </dependencies>
                </project>
                """);
    }

    @Test
    void effectivePomExclusion() throws Exception {
        Dependency dep = new Dependency();
        dep.setGroupId("dgid");
        dep.setArtifactId("daid");
        DependencyExclusion ex = new DependencyExclusion();
        ex.setGroupId("egid");
        ex.setArtifactId("eaid");
        dep.addExclusion(ex);
        performTest(
                Artifact.of("gid", "aid", "ver"),
                List.of(dep),
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
                      <version>SYSTEM</version>
                      <exclusions>
                        <exclusion>
                          <groupId>egid</groupId>
                          <artifactId>eaid</artifactId>
                        </exclusion>
                      </exclusions>
                    </dependency>
                  </dependencies>
                </project>
                """);
    }
}
