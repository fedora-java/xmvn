/*-
 * Copyright (c) 2024-2025 Red Hat, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Mikolaj Izdebski
 */
public class RpmDbTest {
    @TempDir private Path tempDir;

    @BeforeAll
    public static void ensureRpmIsPresent() {
        assumeTrue(Files.isExecutable(Path.of("/usr/bin/rpm")));
    }

    @Test
    public void testRpmPathLookupSuccess() {
        RpmDb db = new RpmDb();
        String pkg = db.lookupPath("/usr/bin");
        assertThat(pkg).matches("filesystem (.*)");
    }

    @Test
    public void testRpmPathLookupFail() {
        RpmDb db = new RpmDb();
        String pkg = db.lookupPath(tempDir.toString());
        assertThat(pkg).isNull();
    }
}
