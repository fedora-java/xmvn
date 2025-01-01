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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author Mikolaj Izdebski
 */
public class ConditionTest {
    private ArtifactContext context1;

    private ArtifactContext context2;

    @BeforeEach
    public void setUp() {
        context1 =
                new ArtifactContext(
                        new DefaultArtifact("some-gid", "the-aid", "zip", "xyzzy", "1.2.3"),
                        Collections.singletonMap("foo", "bar"));

        context2 =
                new ArtifactContext(
                        new DefaultArtifact("org.apache.maven", "maven-model", "3.0.5"),
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
    public void testNullCondition() throws Exception {
        Condition cond = new Condition(null);
        assertTrue(cond.getValue(context1));
        assertTrue(cond.getValue(context2));
    }

    /**
     * Test if basic conditions work.
     *
     * @throws Exception
     */
    @Test
    public void testBasicCondition() throws Exception {
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
        assertTrue(cond.getValue(context1));
        assertFalse(cond.getValue(context2));
    }

    /**
     * Test if AND, OR and XOR operators allow more than one argument.
     *
     * @throws Exception
     */
    @Test
    public void testTernaryOperators() throws Exception {
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
        assertTrue(cond.getValue(context1));
        assertTrue(cond.getValue(context2));
    }

    /**
     * Test if syntax errors are caught by AST walker.
     *
     * @throws Exception
     */
    @Test
    public void testSyntaxError() throws Exception {
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
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("unknown XML node name: hello"));
        }
    }
}
