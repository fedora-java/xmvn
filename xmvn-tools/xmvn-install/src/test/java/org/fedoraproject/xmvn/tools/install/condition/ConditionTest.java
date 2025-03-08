/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.StringReader;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author Mikolaj Izdebski
 */
class ConditionTest {
    private ArtifactContext context1;

    private ArtifactContext context2;

    @BeforeEach
    void setUp() {
        context1 =
                new ArtifactContext(
                        Artifact.of("some-gid", "the-aid", "zip", "xyzzy", "1.2.3"),
                        Collections.singletonMap("foo", "bar"));

        context2 =
                new ArtifactContext(
                        Artifact.of("org.apache.maven", "maven-model", "3.0.5"),
                        Collections.singletonMap("native", "true"));
    }

    private Element buildDom(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement();
    }

    /**
     * Test if null conditions are always met.
     *
     * @throws Exception
     */
    @Test
    void nullCondition() throws Exception {
        Condition cond = new Condition(null);
        assertThat(cond.getValue(context1)).isTrue();
        assertThat(cond.getValue(context2)).isTrue();
    }

    /**
     * Test if basic conditions work.
     *
     * @throws Exception
     */
    @Test
    void basicCondition() throws Exception {
        String xml =
                """
                <filter>
                  <or>
                    <and>
                      <equals>
                        <extension/>
                        <string>jar</string>
                      </equals>
                      <not>
                        <equals>
                          <property>native</property>
                          <string>true</string>
                        </equals>
                      </not>
                    </and>
                    <!-- Maybe /usr/share/java is not the best place to store
                         ZIP files, but packages are doing so anyways and
                         allowing ZIPs here simplifies packaging.  TODO: find a
                         better location for ZIP files.  -->
                    <equals>
                      <extension/>
                      <string>zip</string>
                    </equals>
                  </or>
                </filter>
                """;

        Condition cond = new Condition(buildDom(xml));
        assertThat(cond.getValue(context1)).isTrue();
        assertThat(cond.getValue(context2)).isFalse();
    }

    /**
     * Test if AND, OR and XOR operators allow more than one argument.
     *
     * @throws Exception
     */
    @Test
    void ternaryOperators() throws Exception {
        String xml =
                """
                <filter>
                  <or>
                    <and>
                      <true/>
                      <false/>
                      <true/>
                    </and>
                    <false/>
                    <xor>
                      <false/>
                      <true/>
                      <false/>
                    </xor>
                  </or>
                </filter>
                 """;

        Condition cond = new Condition(buildDom(xml));
        assertThat(cond.getValue(context1)).isTrue();
        assertThat(cond.getValue(context2)).isTrue();
    }

    /**
     * Test if syntax errors are caught by AST walker.
     *
     * @throws Exception
     */
    @Test
    void syntaxError() throws Exception {
        String xml =
                """
                <filter>
                  <not>
                    <xor>
                      <hello/>
                    </xor>
                  </not>
                </filter>
                """;

        try {
            new Condition(buildDom(xml));
            fail("");
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("unknown XML node name: hello");
        }
    }
}
