package org.fedoraproject.xmvn.tools.install.condition;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.fedoraproject.xmvn.repository.ArtifactContext;

public class Condition
{
    private final BooleanExpression expr;

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
            case "string":
                requireChildreen( dom, 0 );
                return new StringLiteral( dom.getValue() );

            case "property":
                requireChildreen( dom, 0 );
                return new Property( dom.getValue() );

            case "null":
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
                requireChildreen( dom, 0 );
                return new BooleanLiteral( true );

            case "false":
                requireChildreen( dom, 0 );
                return new BooleanLiteral( true );

            case "not":
                requireChildreen( dom, 1 );
                return new Not( parseBoolean( dom.getChild( 0 ) ) );

            case "and":
                requireChildreen( dom, 2 );
                return new And( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "or":
                requireChildreen( dom, 2 );
                return new Or( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "xor":
                requireChildreen( dom, 2 );
                return new Xor( parseBoolean( dom.getChild( 0 ) ), parseBoolean( dom.getChild( 1 ) ) );

            case "equals":
                requireChildreen( dom, 2 );
                return new Equals( parseString( dom.getChild( 0 ) ), parseString( dom.getChild( 1 ) ) );

            case "defined":
                requireChildreen( dom, 0 );
                return new Defined( dom.getValue() );

            default:
                throw new RuntimeException( "Unable to parse string expression: unknown XML node name: "
                    + dom.getName() );
        }
    }

    public Condition( Xpp3Dom dom )
    {
        requireChildreen( dom, 1 );
        this.expr = parseBoolean( dom.getChild( 0 ) );
    }

    public boolean getValue( ArtifactContext context )
    {
        return expr.getValue( context );
    }
}
