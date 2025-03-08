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
package org.fedoraproject.xmvn.config.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.test.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class ConfigurationMergerTest extends AbstractTest {
    private ConfigurationMerger merger;

    @BeforeEach
    void setUp() {
        merger = new ConfigurationMerger();
    }

    private static String toString(Configuration conf) throws Exception {
        return conf.toXML().replaceFirst("^<\\?xml[^>]+>", "");
    }

    @Test
    void merge() throws Exception {
        Configuration c1 =
                Configuration.readFromXML(Path.of("src/test/resources/conf-dominant.xml"));
        Configuration c2 =
                Configuration.readFromXML(Path.of("src/test/resources/conf-recessive.xml"));
        Configuration c4 =
                Configuration.readFromXML(Path.of("src/test/resources/conf-superdominant.xml"));

        Configuration c3 = merger.merge(null, c2);
        assertThat(toString(c3)).isEqualTo(toString(c2));

        Configuration c5 = merger.merge(c1, c2);

        Configuration out = merger.merge(c4, c5);

        assertThat(out.getProperties()).hasSize(3);
        assertThat(out.getProperties().get("p1")).isEqualTo("v1");
        assertThat(out.getProperties().get("p2")).isEqualTo("v2");
        assertThat(out.getProperties().get("p3")).isEqualTo("v3");
        assertThat(out.getBuildSettings().isDebug()).isTrue();
        assertThat(out.getBuildSettings().isSkipTests()).isFalse();
        assertThat(out.getResolverSettings().isDebug()).isTrue();
        assertThat(out.getInstallerSettings().isDebug()).isTrue();
        assertThat(out.getInstallerSettings().getMetadataDir()).isEqualTo("/foo/bar");
        assertThat(out.getResolverSettings().isIgnoreDuplicateMetadata()).isFalse();

        Configuration c6 = merger.merge(c2, c2);
        assertThat(toString(c6)).isEqualTo(toString(c2));
    }
}
