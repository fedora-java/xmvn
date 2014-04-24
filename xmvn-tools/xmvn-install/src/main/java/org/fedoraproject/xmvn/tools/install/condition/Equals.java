/*-
 * Copyright (c) 2014 Red Hat, Inc.
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

/**
 * @author Mikolaj Izdebski
 */
class Equals
    extends BooleanExpression
{
    private final StringExpression lhs;

    private final StringExpression rhs;

    public Equals( StringExpression lhs, StringExpression rhs )
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean getValue( Context context )
    {
        String lhsValue = lhs.getValue( context );
        String rhsValue = rhs.getValue( context );

        return ( lhsValue == null && rhsValue == null ) || lhsValue.equals( rhsValue );
    }
}
