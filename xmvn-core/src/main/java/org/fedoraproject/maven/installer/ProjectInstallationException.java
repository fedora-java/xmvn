package org.fedoraproject.maven.installer;

/**
 * @author Mikolaj Izdebski
 */
public class ProjectInstallationException
    extends Exception
{
    private static final long serialVersionUID = 1;

    public ProjectInstallationException()
    {
    }

    public ProjectInstallationException( String message )
    {
        super( message );
    }

    public ProjectInstallationException( Throwable cause )
    {
        super( cause );
    }

    public ProjectInstallationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ProjectInstallationException( String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
