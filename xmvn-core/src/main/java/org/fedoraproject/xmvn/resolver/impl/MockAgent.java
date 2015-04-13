/*-
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
class MockAgent
{
    private static final Path REQUEST_PIPE = Paths.get( "/var/run/xmvn/mock-request" );

    private static final Path REPLY_PIPE = Paths.get( "/var/run/xmvn/mock-reply" );

    private final Logger logger = LoggerFactory.getLogger( MockAgent.class );

    private volatile Boolean present;

    public boolean isPresent()
    {
        if ( present == null )
        {
            present =
                Files.isWritable( REQUEST_PIPE ) && !Files.isRegularFile( REQUEST_PIPE )
                    && !Files.isDirectory( REQUEST_PIPE ) && Files.isReadable( REPLY_PIPE )
                    && !Files.isRegularFile( REPLY_PIPE ) && !Files.isDirectory( REPLY_PIPE );
        }

        return present;
    }

    private String communicate( String request )
        throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile( REQUEST_PIPE.toString(), "rw" ))
        {
            try (FileChannel channel = raf.getChannel())
            {
                try (FileLock lock = channel.lock())
                {
                    Files.write( REQUEST_PIPE, request.getBytes() );

                    return Files.readAllLines( REPLY_PIPE ).iterator().next();
                }
            }
        }
    }

    public void tryInstallArtifact( Artifact artifact )
    {
        try
        {
            logger.info( "Requesting Mock to install artifact {}", artifact );
            String reply = communicate( artifact.toString() + "\n" );
            logger.info( "Mock replied: {}", reply );
        }
        catch ( IOException e )
        {
            logger.error( "Failed to communicate with XMvn Mock plugin", e );
        }
    }
}
