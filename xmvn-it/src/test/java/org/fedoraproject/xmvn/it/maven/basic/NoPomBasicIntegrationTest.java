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
package org.fedoraproject.xmvn.it.maven.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Basic integration tests for XMvn (Maven with extensions).
 *
 * @author Mikolaj Izdebski
 */
public class NoPomBasicIntegrationTest extends AbstractMavenIntegrationTest {
    @Test
    public void testNoPom() throws Exception {
        expectFailure();
        performTest("validate");
        assertTrue(
                getStdout()
                        .anyMatch(
                                s ->
                                        s.startsWith(
                                                "[ERROR] The goal you specified requires a project to execute "
                                                        + "but there is no POM in this directory")));
    }
}
