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
package org.fedoraproject.xmvn.it.maven.mojo.javadoc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for javadoc MOJO.
 *
 * @author Mikolaj Izdebski
 */
public class JavadocIntegrationTest extends AbstractMojoIntegrationTest {
    @Test
    public void testJavadoc() throws Exception {
        performMojoTest("javadoc");

        assertTrue(Files.isDirectory(Path.of("target/xmvn-apidocs")));
        assertTrue(Files.isRegularFile(Path.of("target/xmvn-apidocs/foo/Bar.html")));
        assertTrue(Files.isSymbolicLink(Path.of(".xmvn/apidocs")));
        assertTrue(Files.isSameFile(Path.of(".xmvn/apidocs"), Path.of("target/xmvn-apidocs")));

        byte[] htmlBytes = Files.readAllBytes(Path.of("target/xmvn-apidocs/foo/Bar.html"));
        String html = new String(htmlBytes, StandardCharsets.UTF_8);
        assertTrue(
                Pattern.compile("<!-- Generated by javadoc \\(\\d+\\) -->").matcher(html).find());
        assertFalse(
                Pattern.compile(".*<!-- Generated by javadoc \\(\\d+\\) on .*")
                        .matcher(html)
                        .find());
        assertFalse(html.contains("<meta name=\"dc.created\""));
    }
}
