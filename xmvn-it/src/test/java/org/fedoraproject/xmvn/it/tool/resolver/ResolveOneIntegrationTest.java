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
package org.fedoraproject.xmvn.it.tool.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for XMvn Resolver tool.
 *
 * @author Mikolaj Izdebski
 */
class ResolveOneIntegrationTest extends AbstractToolIntegrationTest {
    @Test
    void testResolveOne() throws Exception {
        assertThat(invokeTool("xmvn-resolve", "junit:junit")).isEqualTo(0);
        assertThat(getStderr()).isEmpty();

        List<String> out = getStdout().collect(Collectors.toList());
        assertThat(out).hasSize(1);

        Path jar = Path.of(out.iterator().next());
        assertThat(jar).endsWith(Path.of("src/test/resources/empty.jar"));
        assertThat(jar).isRegularFile();
    }
}
