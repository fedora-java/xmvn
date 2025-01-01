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
package org.fedoraproject.xmvn.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.test.AbstractTest;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * @author Mikolaj Izdebski
 */
public class BasicDeployerTest extends AbstractTest {
    /**
     * Test if Sisu can load deployer component.
     *
     * @throws Exception
     */
    @Test
    public void testComponentLookup() throws Exception {
        Deployer deployer = getService(Deployer.class);
        assertNotNull(deployer);
    }

    @Test
    public void testDeployment() throws Exception {
        Deployer deployer = getService(Deployer.class);
        Path plan = Files.createTempDirectory("xmvn-test").resolve("plan.xml");
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath(plan);
        req.setArtifact(
                new DefaultArtifact("g:a:v").setPath(Path.of("src/test/resources/simple.xml")));
        req.addProperty("foo", "bar");
        req.addDependency(new DefaultArtifact("g1:a1:e1:c1:v1"));
        req.addDependency(
                new DefaultArtifact("g2:a2:e2:c2:v2"),
                true,
                Arrays.asList(
                        new DefaultArtifact("e:e:e:e:e"),
                        new DefaultArtifact("eg2:ea2:ee2:ec2:ev2")));
        deployer.deploy(req);
        DeploymentRequest req2 = new DeploymentRequest();
        req2.setPlanPath(plan);
        req2.setArtifact(new DefaultArtifact("foo:bar:pom:").setPath(Path.of("/dev/null")));
        deployer.deploy(req2);

        XmlAssert.assertThat(
                        """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <metadata>
                          <artifacts>
                            <artifact>
                              <groupId>g</groupId>
                              <artifactId>a</artifactId>
                              <version>v</version>
                              <path>src/test/resources/simple.xml</path>
                              <properties>
                                <foo>bar</foo>
                              </properties>
                              <dependencies>
                                <dependency>
                                  <groupId>g1</groupId>
                                  <artifactId>a1</artifactId>
                                  <extension>e1</extension>
                                  <classifier>c1</classifier>
                                  <requestedVersion>v1</requestedVersion>
                                </dependency>
                                <dependency>
                                  <groupId>g2</groupId>
                                  <artifactId>a2</artifactId>
                                  <extension>e2</extension>
                                  <classifier>c2</classifier>
                                  <requestedVersion>v2</requestedVersion>
                                  <optional>true</optional>
                                  <exclusions>
                                    <exclusion>
                                      <groupId>e</groupId>
                                      <artifactId>e</artifactId>
                                    </exclusion>
                                    <exclusion>
                                      <groupId>eg2</groupId>
                                      <artifactId>ea2</artifactId>
                                    </exclusion>
                                  </exclusions>
                                </dependency>
                              </dependencies>
                            </artifact>
                            <artifact>
                              <groupId>foo</groupId>
                              <artifactId>bar</artifactId>
                              <extension>pom</extension>
                              <version>SYSTEM</version>
                              <path>/dev/null</path>
                            </artifact>
                          </artifacts>
                        </metadata>
                        """)
                .and(plan.toFile())
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();
    }

    @Test
    public void testReadError() throws Exception {
        Deployer deployer = getService(Deployer.class);
        Path plan = Files.createTempDirectory("xmvn-test").resolve("plan.xml");
        Files.createDirectory(plan);
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath(plan);
        req.setArtifact(
                new DefaultArtifact("g:a:v").setPath(Path.of("src/test/resources/simple.xml")));
        DeploymentResult res = deployer.deploy(req);
        assertNotNull(res.getException());
        assertTrue(IOException.class.isAssignableFrom(res.getException().getClass()));
        assertEquals("Failed to parse reactor installation plan", res.getException().getMessage());
    }

    static final Pattern PROCESS_UID_PATTERN =
            Pattern.compile("^Uid:\\s+\\d+\\s+(\\d+)\\s+\\d+\\s+\\d+\\s*$");

    private boolean runningAsRoot() {
        try (Stream<String> lines = Files.lines(Path.of("/proc/self/status"))) {
            return lines.map(
                            s -> {
                                Matcher matcher = PROCESS_UID_PATTERN.matcher(s);

                                if (matcher.matches()) {
                                    if ("0".equals(matcher.group(1))) {
                                        return true;
                                    }
                                }

                                return false;
                            })
                    .anyMatch(result -> result);
        } catch (IOException ex) {
            System.err.println("Unable to read from \"/proc/self/status\"");
            return false;
        }
    }

    @Test
    public void testWriteError() throws Exception {
        assumeFalse(runningAsRoot());
        Deployer deployer = getService(Deployer.class);
        Path plan = Files.createTempDirectory("xmvn-test").resolve("plan.xml");
        try (BufferedWriter bw = Files.newBufferedWriter(plan)) {
            bw.write("<metadata/>");
        }
        Files.setPosixFilePermissions(plan, Set.of(PosixFilePermission.OTHERS_READ));
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath(plan);
        req.setArtifact(
                new DefaultArtifact("g:a:v").setPath(Path.of("src/test/resources/simple.xml")));
        DeploymentResult res = deployer.deploy(req);
        assertNotNull(res.getException());
        assertTrue(IOException.class.isAssignableFrom(res.getException().getClass()));
    }
}
