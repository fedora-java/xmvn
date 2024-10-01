/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Collections;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxReader;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

/** @author Mikolaj Izdebski */
public class ConditionTest {
    private ArtifactContext context1;

    private ArtifactContext context2;

    @BeforeEach
    public void setUp() {
        context1 = new ArtifactContext(
                new DefaultArtifact("some-gid", "the-aid", "zip", "xyzzy", "1.2.3"),
                Collections.singletonMap("foo", "bar"));

        context2 = new ArtifactContext(
                new DefaultArtifact("org.apache.maven", "maven-model", "3.0.5"),
                Collections.singletonMap("native", "true"));
    }

    private Element buildDom(CharSequence data) throws Exception {
        Reader stringReader = new StringReader(data.toString());
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(stringReader);
        ConfigurationStaxReader modelloReader = new ConfigurationStaxReader();
        Method initDocMethod = ConfigurationStaxReader.class.getDeclaredMethod("initDoc");
        initDocMethod.setAccessible(true);
        initDocMethod.invoke(modelloReader);
        Method buildDomMethod =
                ConfigurationStaxReader.class.getDeclaredMethod("buildDom", XMLStreamReader.class, boolean.class);
        buildDomMethod.setAccessible(true);
        return (Element) buildDomMethod.invoke(modelloReader, xmlReader, true);
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
        StringBuilder sb = new StringBuilder();
        sb.append("<filter>");
        sb.append("  <or>");
        sb.append("    <and>");
        sb.append("      <equals>");
        sb.append("        <extension/>");
        sb.append("        <string>jar</string>");
        sb.append("      </equals>");
        sb.append("      <not>");
        sb.append("        <equals>");
        sb.append("          <property>native</property>");
        sb.append("          <string>true</string>");
        sb.append("        </equals>");
        sb.append("      </not>");
        sb.append("    </and>");
        sb.append("    <!-- Maybe /usr/share/java is not the best place to store");
        sb.append("         ZIP files, but packages are doing so anyways and");
        sb.append("         allowing ZIPs here simplifies packaging.  TODO: find a");
        sb.append("         better location for ZIP files.  -->");
        sb.append("    <equals>");
        sb.append("      <extension/>");
        sb.append("      <string>zip</string>");
        sb.append("    </equals>");
        sb.append("  </or>");
        sb.append("</filter>");

        Condition cond = new Condition(buildDom(sb));
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
        StringBuilder sb = new StringBuilder();
        sb.append("<filter>");
        sb.append("  <or>");
        sb.append("    <and>");
        sb.append("      <true/>");
        sb.append("      <false/>");
        sb.append("      <true/>");
        sb.append("    </and>");
        sb.append("    <false/>");
        sb.append("    <xor>");
        sb.append("      <false/>");
        sb.append("      <true/>");
        sb.append("      <false/>");
        sb.append("    </xor>");
        sb.append("  </or>");
        sb.append("</filter>");

        Condition cond = new Condition(buildDom(sb));
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
        StringBuilder sb = new StringBuilder();
        sb.append("<filter>");
        sb.append("  <not>");
        sb.append("    <xor>");
        sb.append("      <hello/>");
        sb.append("    </xor>");
        sb.append("  </not>");
        sb.append("</filter>");

        try {
            new Condition(buildDom(sb));
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("unknown XML node name: hello"));
        }
    }
}
