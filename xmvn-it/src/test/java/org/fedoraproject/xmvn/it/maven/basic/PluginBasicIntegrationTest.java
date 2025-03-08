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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Basic integration tests for XMvn (Maven with extensions).
 *
 * @author Mikolaj Izdebski
 */
class PluginBasicIntegrationTest extends AbstractMavenIntegrationTest {
    @Test
    void testPlugin() throws Exception {
        performTest("process-classes");
        assertThat(getStdout())
                .anyMatch(
                        s ->
                                s.startsWith(
                                        "[INFO] --- plexus-component-metadata:1.7.1:generate-metadata (default)"));
        assertThat(Path.of("src/main/resources/META-INF/plexus/components.xml")).isRegularFile();
        assertThat(Path.of("component-metadata-test.xml")).isRegularFile();
    }
}
