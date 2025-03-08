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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class BooleanExpressionTest {
    @Test
    void basicExpressions() {
        Artifact artifact = Artifact.of("foo", "bar");
        ArtifactContext context = new ArtifactContext(artifact);

        BooleanExpression trueExpression = new BooleanLiteral(true);
        assertThat(trueExpression.getValue(context)).isTrue();

        BooleanExpression falseExpression = new BooleanLiteral(false);
        assertThat(falseExpression.getValue(context)).isFalse();

        BooleanExpression andExpression = new And(Arrays.asList(trueExpression, falseExpression));
        assertThat(andExpression.getValue(context)).isFalse();

        BooleanExpression orExpression = new Or(Arrays.asList(trueExpression, falseExpression));
        assertThat(orExpression.getValue(context)).isTrue();

        BooleanExpression xorExpression = new Xor(Arrays.asList(trueExpression, falseExpression));
        assertThat(xorExpression.getValue(context)).isTrue();
    }

    @Test
    void properties() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("foo", "bar");
        properties.put("baz", "");
        Artifact artifact = Artifact.of("dummy", "dummy");
        ArtifactContext context = new ArtifactContext(artifact, properties);

        StringExpression fooProperty = new Property("foo");
        assertThat(fooProperty.getValue(context)).isEqualTo("bar");

        StringExpression bazProperty = new Property("baz");
        assertThat(bazProperty.getValue(context)).isEmpty();

        StringExpression xyzzyProperty = new Property("xyzzy");
        assertThat(xyzzyProperty.getValue(context)).isNull();

        BooleanExpression fooDefined = new Defined("foo");
        assertThat(fooDefined.getValue(context)).isTrue();

        BooleanExpression bazDefined = new Defined("baz");
        assertThat(bazDefined.getValue(context)).isTrue();

        BooleanExpression xyzzyDefined = new Defined("xyzzy");
        assertThat(xyzzyDefined.getValue(context)).isFalse();
    }
}
