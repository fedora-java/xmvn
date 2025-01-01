/*-
 * Copyright (c) 2021-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo.javadoc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for javadoc MOJO.
 *
 * @author Mikolaj Izdebski
 */
public class JavadocToolchainsIntegrationTest extends AbstractMojoIntegrationTest {
    @Test
    public void testJavadocToolchains() throws Exception {
        performMojoTest("javadoc");
        assertTrue(getStdout().anyMatch("[INFO] Toolchain in xmvn-mojo: JDK[/tmp]"::equals));
    }
}
