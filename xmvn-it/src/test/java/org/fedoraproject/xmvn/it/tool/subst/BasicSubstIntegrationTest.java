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
package org.fedoraproject.xmvn.it.tool.subst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Mikolaj Izdebski
 */
public class BasicSubstIntegrationTest extends AbstractToolIntegrationTest {

    @TempDir private Path tempDir;

    private Path writeJar(String name, String... entries) throws IOException {
        Path jarPath = tempDir.resolve(name + ".jar");
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1");
        for (String entry : entries) {
            String[] keyVal = entry.split("=", 2);
            mf.getMainAttributes().putValue(keyVal[0], keyVal[1]);
        }
        try (OutputStream os = Files.newOutputStream(jarPath);
                JarOutputStream jos = new JarOutputStream(os, mf)) {
            // No files, only manifest
        }
        return jarPath;
    }

    @Test
    public void testSubst() throws Exception {
        Path jarA = writeJar("A");
        Path jarB =
                writeJar(
                        "B",
                        "JavaPackages-GroupId=junit",
                        "JavaPackages-ArtifactId=junit",
                        "JavaPackages-Version=1.2.3");
        Path jarC =
                writeJar(
                        "C",
                        "JavaPackages-GroupId=foo",
                        "JavaPackages-ArtifactId=xyzzy",
                        "JavaPackages-Version=42");

        assertEquals(0, invokeTool("xmvn-subst", tempDir.toString()));
        assertFalse(getStdout().findAny().isPresent());

        assertTrue(Files.isRegularFile(jarA, LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.isSymbolicLink(jarB));
        assertTrue(Files.isRegularFile(jarC, LinkOption.NOFOLLOW_LINKS));

        assertTrue(
                getStderr()
                        .anyMatch(
                                s ->
                                        s.equals(
                                                "Skipping file "
                                                        + jarA
                                                        + ": No artifact definition found")));
        assertTrue(
                getStderr()
                        .anyMatch(
                                s ->
                                        s.equals(
                                                "Linked "
                                                        + jarB
                                                        + " to "
                                                        + getResourcesDir().resolve("empty.jar"))));
        assertTrue(
                getStderr()
                        .anyMatch(
                                s ->
                                        s.equals(
                                                "WARNING: Skipping file "
                                                        + jarC
                                                        + ": Artifact foo:xyzzy:jar:42 not found in repository")));
    }
}
