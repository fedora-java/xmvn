/*-
 * Copyright (c) 2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config;

import io.kojan.xml.Entity;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xmlunit.assertj3.XmlAssert;

class DOMTest {
    class Bean {
        Element dom;
    }

    @Test
    public void testDOM() throws Exception {
        var entity =
                Entity.ofMutable(
                        "test",
                        Bean::new,
                        DOM.of("foo", bean -> bean.dom, (bean, dom) -> bean.dom = dom));
        String xml =
                """
		        <test>
		          <foo>
		            <bar>baz</bar>
		            <nested>
		              <xyzzy/>
		            </nested>
		          </foo>
		        </test>
		        """;
        Bean b = entity.fromXML(xml);
        String xml2 = entity.toXML(b);
        XmlAssert.assertThat(xml2).and(xml).ignoreWhitespace().areSimilar();
    }
}
