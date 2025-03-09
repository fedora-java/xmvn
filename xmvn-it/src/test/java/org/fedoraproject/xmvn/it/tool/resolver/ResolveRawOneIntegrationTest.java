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
import java.util.stream.Collectors;
import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * Integration tests for XMvn Resolver tool.
 *
 * @author Mikolaj Izdebski
 */
class ResolveRawOneIntegrationTest extends AbstractToolIntegrationTest {
    @Test
    void testResolveRawOne() throws Exception {
        String input =
                String.join(
                        "\n",
                        "<requests>",
                        " <![CDATA[ ]]>",
                        " <request>",
                        "  <persistentFileNeeded>false</persistentFileNeeded>",
                        "  <artifact>",
                        "   <extension>jar</extension>",
                        "   <artifactId>aliased-component-metadata</artifactId>",
                        "   <classifier/>",
                        "   <!-- huh -->",
                        "   <version>any</version>",
                        "   <groupId>alias-<![CDATA[test]]></groupId>",
                        "  </artifact>",
                        "  <providerNeeded><![CDATA[false]]></providerNeeded>",
                        " </request>",
                        "</requests>");
        assertThat(invokeToolWithInput(input, "xmvn-resolve", "--raw-request")).isEqualTo(0);
        assertThat(getStderr()).isEmpty();
        Path absPath =
                getDependencyDir().resolve("plexus-component-metadata-2.2.0.jar").toRealPath();
        String expectedOutput =
                String.join(
                        "\n",
                        "<results>",
                        " <result>",
                        "  <artifactPath>" + absPath + "</artifactPath>",
                        "  <namespace/>",
                        " </result>",
                        "</results>");
        XmlAssert.assertThat(expectedOutput)
                .and(getStdout().collect(Collectors.joining("\n")))
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();
    }
}
