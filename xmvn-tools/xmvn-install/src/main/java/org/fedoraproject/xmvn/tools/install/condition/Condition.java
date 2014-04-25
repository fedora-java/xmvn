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

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.fedoraproject.xmvn.repository.ArtifactContext;

/**
 * @author Mikolaj Izdebski
 */
public class Condition
{
    private final BooleanExpression expr;

    private void requireText( Xpp3Dom dom, boolean require )
    {
        if ( require == ( dom.getValue() != null ) )
            return;

        String name = dom.getName();

        if ( require )
            throw new RuntimeException( "XML node " + name + " must have text content." );

        throw new RuntimeException( "XML node " + name + " doesn't allow text content." );
    }

    private void requireChildreen( Xpp3Dom dom, int n )
    {
        if ( dom.getChildCount() == n )
            return;

        String name = dom.getName();

        if ( n == 0 )
            throw new RuntimeException( "XML node " + name + " doesn't allow any childreen." );

        if ( n == 1 )
            throw new RuntimeException( "XML node " + name + " requires exactly one child node." );

        throw new RuntimeException( "XML node " + name + " must have exactly " + n + " childreen." );
    }

    private StringExpression parseString( Xpp3Dom dom )
    {
        switch ( dom.getName() )
        {
            case "groupId":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new GroupId();

            case "artifactId":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new ArtifactId();

            case "extension":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new Extension();

            case "classifier":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new Classifier();

            case "version":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new Version();

            case "string":
                requireText( dom, true );
                requireChildreen( dom, 0 );
                return new StringLiteral( dom.getValue() );

            case "property":
                requireText( dom, true );
                requireChildreen( dom, 0 );
                return new Property( dom.getValue() );

            case "null":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new Null();

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getName() );
        }
    }

    private BooleanExpression parseBoolean( Xpp3Dom dom )
    {
        switch ( dom.getName() )
        {
            case "true":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new BooleanLiteral( true );

            case "false":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new BooleanLiteral( true );

            case "not":
                requireText( dom, false );
                requireChildreen( dom, 1 );
                return new Not( parseBoolean( dom.getChild( 0 ) ) );

            case "and":
                requireText( dom, false );
                requireChildreen( dom, 2 );
                return new And( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "or":
                requireText( dom, false );
                requireChildreen( dom, 2 );
                return new Or( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "xor":
                requireText( dom, false );
                requireChildreen( dom, 2 );
                return new Xor( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "equals":
                requireText( dom, false );
                requireChildreen( dom, 2 );
                return new Equals( parseString( dom.getChild( 0 ) ), parseString( dom.getChild( 1 ) ) );

            case "defined":
                requireText( dom, true );
                requireChildreen( dom, 0 );
                return new Defined( dom.getValue() );

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getName() );
        }
    }

    public Condition( Xpp3Dom dom )
    {
        if ( dom == null )
        {
            dom = new Xpp3Dom( "condition" );
            dom.addChild( new Xpp3Dom( "true" ) );
        }

        requireChildreen( dom, 1 );
        this.expr = parseBoolean( dom.getChild( 0 ) );
    }

    public boolean getValue( ArtifactContext context )
    {
        return expr.getValue( context );
    }
}
