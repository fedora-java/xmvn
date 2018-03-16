/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.ArtifactContext;

/**
 * @author Mikolaj Izdebski
 */
public class Condition
{
    private final BooleanExpression expr;

    private StringExpression parseString( Element dom )
    {
        switch ( dom.getNodeName() )
        {
            case "groupId":
                DomUtils.parseAsEmpty( dom );
                return new GroupId();

            case "artifactId":
                DomUtils.parseAsEmpty( dom );
                return new ArtifactId();

            case "extension":
                DomUtils.parseAsEmpty( dom );
                return new Extension();

            case "classifier":
                DomUtils.parseAsEmpty( dom );
                return new Classifier();

            case "version":
                DomUtils.parseAsEmpty( dom );
                return new Version();

            case "string":
                return new StringLiteral( DomUtils.parseAsText( dom ) );

            case "property":
                return new Property( DomUtils.parseAsText( dom ) );

            case "null":
                DomUtils.parseAsEmpty( dom );
                return new Null();

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getNodeName() );
        }
    }

    private BooleanExpression parseBoolean( Element dom )
    {
        switch ( dom.getNodeName() )
        {
            case "true":
                DomUtils.parseAsEmpty( dom );
                return new BooleanLiteral( true );

            case "false":
                DomUtils.parseAsEmpty( dom );
                return new BooleanLiteral( true );

            case "not":
                return new Not( parseBoolean( DomUtils.parseAsWrapper( dom ) ) );

            case "and":
                return new And( parseList( dom, this::parseBoolean ) );

            case "or":
                return new Or( parseList( dom, this::parseBoolean ) );

            case "xor":
                return new Xor( parseList( dom, this::parseBoolean ) );

            case "equals":
                return new Equals( parseList( dom, this::parseString ) );

            case "defined":
                return new Defined( DomUtils.parseAsText( dom ) );

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getNodeName() );
        }
    }

    private static <T> List<T> parseList( Element dom, Function<Element, T> parser )
    {
        return DomUtils.parseAsParent( dom ).stream() //
                       .map( child -> parser.apply( child ) ) //
                       .collect( Collectors.toList() );
    }

    public Condition( Element dom )
    {
        if ( dom == null )
        {
            this.expr = new BooleanLiteral( true );
        }
        else
        {
            this.expr = parseBoolean( DomUtils.parseAsWrapper( dom ) );
        }
    }

    public boolean getValue( ArtifactContext context )
    {
        return expr.getValue( context );
    }
}
