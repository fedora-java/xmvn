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

import java.util.List;
import org.fedoraproject.xmvn.repository.ArtifactContext;

/**
 * @author Mikolaj Izdebski
 */
abstract class BooleanOperator extends BooleanExpression {
    private final boolean neutralValue;

    private final List<BooleanExpression> children;

    public BooleanOperator(boolean neutralValue, List<BooleanExpression> children) {
        this.neutralValue = neutralValue;
        this.children = children;
    }

    @Override
    public boolean getValue(ArtifactContext context) {
        boolean value = neutralValue;

        for (BooleanExpression child : children) {
            value = evaluate(value, child.getValue(context));
        }

        return value;
    }

    protected abstract boolean evaluate(boolean lhs, boolean rhs);
}
