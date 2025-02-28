/*-
 * Copyright (c) 2017-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool.installer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for XMvn Installer tool.
 *
 * @author Mikolaj Izdebski
 */
public class InstallerHelpIntegrationTest extends AbstractToolIntegrationTest {
    @Test
    public void testInstallerHelp() throws Exception {
        assertEquals(0, invokeTool("xmvn-install", "--help"));
        assertFalse(getStderr().findAny().isPresent());
        assertTrue(getStdout().anyMatch(line -> line.startsWith("Usage: xmvn-install")));
    }
}
