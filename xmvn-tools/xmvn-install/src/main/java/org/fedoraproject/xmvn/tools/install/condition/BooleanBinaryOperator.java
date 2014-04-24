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
abstract class BooleanBinaryOperator
    extends BooleanExpression
{
    private final BooleanExpression lhs;

    private final BooleanExpression rhs;

    public BooleanBinaryOperator( BooleanExpression lhs, BooleanExpression rhs )
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean getValue( Context context )
    {
        return evaluate( lhs.getValue( context ), rhs.getValue( context ) );
    }

    protected abstract boolean evaluate( boolean lhs, boolean rhs );
}
