/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.codehaus.plexus.util.StringUtils;

/**
 * Atomic integer object, which stores its value in a text file.
 * 
 * @author Mikolaj Izdebski
 */
public class AtomicFileCounter
{
    private static final int BUFSIZ = 64;

    private final byte[] buffer = new byte[BUFSIZ];

    private final RandomAccessFile file;

    /**
     * Create an instance of counter.
     * 
     * @param path path to the backing file
     * @throws IOException
     */
    public AtomicFileCounter( String path )
        throws IOException
    {
        file = new RandomAccessFile( new File( path ), "rw" );
    }

    /**
     * Create an instance of counter and set its initial value.
     * 
     * @param path path to the backing file
     * @param value initial value of the counter
     * @throws IOException
     */
    public AtomicFileCounter( String path, int value )
        throws IOException
    {
        this( path );
        setValue( value );
    }

    private FileLock lock()
        throws IOException
    {
        return file.getChannel().lock();
    }

    private int readValue()
        throws IOException
    {
        file.seek( 0 );
        long length = file.length();
        if ( length > BUFSIZ )
            throw new IOException( "Semaphore file is too large" );
        file.readFully( buffer, 0, (int) length );
        String text = new String( buffer, 0, (int) length, "US-ASCII" );
        return Integer.parseInt( StringUtils.chompLast( text.trim() ) );
    }

    private void writeValue( int value )
        throws IOException
    {
        file.seek( 0 );
        file.setLength( 0 );
        file.write( String.format( "%d%n", value ).getBytes( "US-ASCII" ) );
    }

    /**
     * If counter is positive then decrement it.
     * 
     * @return the initial value of the counter (before decrementing)
     * @throws IOException
     */
    public int tryDecrement()
        throws IOException
    {
        try (FileLock lock = lock())
        {
            int value = readValue();
            if ( value > 0 )
                writeValue( value - 1 );
            return value;
        }
    }

    /**
     * Get value of the counter.
     * 
     * @return value of the counter
     * @throws IOException
     */
    public int getValue()
        throws IOException
    {
        try (FileLock lock = lock())
        {
            return readValue();
        }
    }

    /**
     * Set value of the counter.
     * 
     * @param value value of the counter
     * @throws IOException
     */
    public void setValue( int value )
        throws IOException
    {
        try (FileLock lock = lock())
        {
            writeValue( value );
        }
    }
}
