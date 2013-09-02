package org.fedoraproject.maven.model;

public class ModelFormatException
    extends Exception
{
    private static final long serialVersionUID = 8595961929364201867L;

    public ModelFormatException( String message )
    {
        super( message );
    }

    public ModelFormatException( Throwable cause )
    {
        super( cause );
    }

    public ModelFormatException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
