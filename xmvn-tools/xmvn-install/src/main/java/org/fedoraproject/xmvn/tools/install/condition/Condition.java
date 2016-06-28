/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Mikolaj Izdebski
 */
public class Condition
{
    private final BooleanExpression expr;

    private List<Node> childrenWithType( Node parent, short type )
    {
        List<Node> children = new ArrayList<>();

        for ( int i = 0; i < parent.getChildNodes().getLength(); i++ )
        {
            if ( parent.getChildNodes().item( i ).getNodeType() == type )
                children.add( parent.getChildNodes().item( i ) );
        }

        return children;
    }

    private void requireText( Node dom, boolean require )
    {
        if ( require == childrenWithType( dom, Node.TEXT_NODE ).isEmpty() )
        {
            String msg = require ? "must have text content" : "doesn't allow text content";
            throw new RuntimeException( "XML node " + dom.getNodeName() + " " + msg + "." );
        }
    }

    private void requireChildreen( Node dom, int n )
    {
        if ( childrenWithType( dom, Node.ELEMENT_NODE ).size() == n )
            return;

        String name = dom.getNodeName();

        if ( n == 0 )
            throw new RuntimeException( "XML node " + name + " doesn't allow any children." );

        if ( n == 1 )
            throw new RuntimeException( "XML node " + name + " requires exactly one child node." );

        throw new RuntimeException( "XML node " + name + " must have exactly " + n + " children." );
    }

    private StringExpression parseString( Node dom )
    {
        switch ( dom.getNodeName() )
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
                return new StringLiteral( dom.getTextContent() );

            case "property":
                requireText( dom, true );
                requireChildreen( dom, 0 );
                return new Property( dom.getTextContent() );

            case "null":
                requireText( dom, false );
                requireChildreen( dom, 0 );
                return new Null();

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getNodeName() );
        }
    }

    private BooleanExpression parseBoolean( Node dom )
    {
        switch ( dom.getNodeName() )
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
                return new Not( parseBoolean( dom.getChildNodes().item( 0 ) ) );

            case "and":
                requireText( dom, false );
                return new And( parseBooleans( dom.getChildNodes() ) );

            case "or":
                requireText( dom, false );
                return new Or( parseBooleans( dom.getChildNodes() ) );

            case "xor":
                requireText( dom, false );
                return new Xor( parseBooleans( dom.getChildNodes() ) );

            case "equals":
                requireText( dom, false );
                requireChildreen( dom, 2 );
                return new Equals( parseString( dom.getChildNodes().item( 0 ) ),
                                   parseString( dom.getChildNodes().item( 1 ) ) );

            case "defined":
                requireText( dom, true );
                requireChildreen( dom, 0 );
                return new Defined( dom.getTextContent() );

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getNodeName() );
        }
    }

    private List<BooleanExpression> parseBooleans( NodeList doms )
    {
        List<BooleanExpression> result = new ArrayList<>();

        for ( int i = 0; i < doms.getLength(); i++ )
        {
            result.add( parseBoolean( doms.item( i ) ) );
        }

        return result;
    }

    public Condition( Node dom )
    {
        if ( dom == null )
        {
            this.expr = new BooleanLiteral( true );
        }
        else
        {
            requireChildreen( dom, 1 );
            this.expr = parseBoolean( dom.getChildNodes().item( 0 ) );
        }
    }

    public boolean getValue( ArtifactContext context )
    {
        return expr.getValue( context );
    }
}
