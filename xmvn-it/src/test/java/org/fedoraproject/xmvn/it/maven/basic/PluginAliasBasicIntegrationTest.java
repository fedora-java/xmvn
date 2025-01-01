/*-
 * Copyright (c) 2015-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Basic integration tests for XMvn (Maven with extensions).
 *
 * @author Mikolaj Izdebski
 */
public class PluginAliasBasicIntegrationTest extends AbstractMavenIntegrationTest {
    @Test
    public void testPluginAlias() throws Exception {
        performTest("process-classes");
        assertTrue(
                getStdout()
                        .anyMatch(
                                s ->
                                        s.startsWith(
                                                "[INFO] --- plexus-component-metadata:1.7.1:generate-metadata (default)")));
        assertTrue(
                Files.isRegularFile(Path.of("src/main/resources/META-INF/plexus/components.xml")));
        assertTrue(Files.isRegularFile(Path.of("component-metadata-test.xml")));
    }
}
