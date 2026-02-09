/*-
 * Copyright (c) 2016-2026 Red Hat, Inc.
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

import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for XMvn Resolver tool.
 *
 * @author Mikolaj Izdebski
 */
class ResolveFailIntegrationTest extends AbstractToolIntegrationTest {
    @Test
    void testResolveFail() throws Exception {
        assertThat(invokeTool("xmvn-resolve", "foobar:xyzzy")).isEqualTo(1);
        assertThat(getStderr())
                .anyMatch(s -> s.endsWith("Unable to resolve artifact foobar:xyzzy:jar:SYSTEM"));
        assertThat(getStdout()).isEmpty();
    }
}
