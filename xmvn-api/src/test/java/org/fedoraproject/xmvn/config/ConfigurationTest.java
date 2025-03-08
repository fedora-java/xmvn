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

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

class ConfigurationTest {

    @Test
    void empty() throws Exception {
        String xml = "<configuration/>";
        Configuration conf = Configuration.fromXML(xml);
        String xml2 = conf.toXML();
        XmlAssert.assertThat(xml).and(xml2).ignoreWhitespace().areIdentical();
    }

    @Test
    void aliases() throws Exception {
        String xml =
                """
                <configuration>
                  <artifactManagement>
                    <rule>
                      <artifactGlob>
                        <groupId>org.codehaus.plexus</groupId>
                      </artifactGlob>
                      <aliases>
                        <alias>
                          <groupId>org.sonatype.plexus</groupId>
                        </alias>
                      </aliases>
                    </rule>
                  </artifactManagement>
                </configuration>
                """;
        Configuration.fromXML(xml);
    }

    @Test
    void compatVersions() throws Exception {
        String xml =
                """
                <configuration>
                  <artifactManagement>
                    <rule>
                      <artifactGlob/>
                      <versions>
                        <version>1.2.3</version>
                        <version>4.5.6</version>
                      </versions>
                    </rule>
                  </artifactManagement>
                </configuration>
                """;
        Configuration.fromXML(xml);
    }
}
