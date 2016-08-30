package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;

import org.fedoraproject.xmvn.utils.AtomicFileCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class Bisect
{
    private final Logger logger = LoggerFactory.getLogger( Bisect.class );

    private final AtomicFileCounter counter;

    public Bisect()
    {
        try
        {
            String path = System.getProperty( "xmvn.bisect.counter" );
            if ( Strings.isNullOrEmpty( path ) )
            {
                counter = null;
                logger.info( "Bisect is disabled" );
            }
            else
            {
                counter = new AtomicFileCounter( path );
                logger.info( "Bisect initialized with value {}", counter.getValue() );
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "I/O error occured when initializing bisect counter" );
        }
    }

    public boolean tryBisect()
    {
        try
        {
            if ( counter == null )
            {
                logger.info( "Bisect is disabled" );
                return false;
            }
            if ( counter.tryDecrement() == 0 )
            {
                logger.info( "Bisect resolving from system repo" );
                return false;
            }
            logger.info( "Bisect resolving from Maven repo" );
            return true;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "I/O error occured when trying to decrement bisect counter" );
        }
    }
}
