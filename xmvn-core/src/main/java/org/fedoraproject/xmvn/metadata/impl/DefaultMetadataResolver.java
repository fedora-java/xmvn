/*-
 * Copyright (c) 2014-2019 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import org.fedoraproject.xmvn.logging.impl.ConsoleLogger;
import org.fedoraproject.xmvn.logging.impl.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;

/**
 * Default implementation of XMvn {@code MetadataResolver} interface.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = MetadataResolver.class )
public class DefaultMetadataResolver
    implements MetadataResolver
{
    @Requirement
    private Logger logger = new ConsoleLogger();

    private final ThreadPoolExecutor executor;

    public DefaultMetadataResolver()
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        int nThread = 2 * Math.min( Math.max( Runtime.getRuntime().availableProcessors(), 1 ), 8 );
        executor = new ThreadPoolExecutor( nThread, nThread, 1, TimeUnit.MINUTES, queue, ( runnable ) ->
        {
            Thread thread = new Thread( runnable );
            thread.setName( DefaultMetadataResolver.class.getCanonicalName() + ".worker" );
            thread.setDaemon( true );
            return thread;
        } );
    }

    @Override
    public MetadataResult resolveMetadata( MetadataRequest request )
    {
        return new DefaultMetadataResult( logger, readMetadata( request.getMetadataRepositories() ),
                                          request.isIgnoreDuplicates() );
    }

    List<PackageMetadata> readMetadata( List<String> metadataLocations )
    {
        Map<Path, Future<PackageMetadata>> futures = new LinkedHashMap<>();

        for ( String pathString : metadataLocations )
        {
            Path path = Paths.get( pathString );

            if ( Files.isDirectory( path ) )
            {
                String[] flist = path.toFile().list();
                if ( flist != null )
                {
                    Arrays.sort( flist );
                    for ( String fragFilename : flist )
                    {
                        Path xmlPath = path.resolve( fragFilename );
                        futures.put( xmlPath, executor.submit( () -> readMetadata( xmlPath ) ) );
                    }
                }
            }
            else
            {
                futures.put( path, executor.submit( () -> readMetadata( path ) ) );
            }
        }

        try
        {
            List<PackageMetadata> result = new ArrayList<>();

            for ( Entry<Path, Future<PackageMetadata>> entry : futures.entrySet() )
            {
                Path path = entry.getKey();
                Future<PackageMetadata> future = entry.getValue();

                try
                {
                    PackageMetadata metadata = future.get();
                    result.add( metadata );

                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Adding metadata from file {}", path );

                        for ( ArtifactMetadata artifact : metadata.getArtifacts() )
                            logger.debug( "Added metadata for {}", artifact );
                    }
                }
                catch ( ExecutionException e )
                {
                    // Ignore. Failure to read PackageMetadata of a single package should not break the whole system
                    logger.debug( "Skipping metadata file {}: {}", path, e );
                }
            }

            return result;
        }
        catch ( InterruptedException e )
        {
            logger.debug( "Metadata reader thread was interrupted" );
            throw new RuntimeException( e );
        }
    }

    private static PackageMetadata readMetadata( Path path )
        throws Exception
    {
        try ( InputStream fis = Files.newInputStream( path ) )
        {
            try ( BufferedInputStream bis = new BufferedInputStream( fis, 128 ) )
            {
                try ( InputStream is = isCompressed( bis ) ? new GZIPInputStream( bis ) : bis )
                {
                    MetadataStaxReader reader = new MetadataStaxReader();
                    return reader.read( is );
                }
            }
        }
    }

    private static boolean isCompressed( BufferedInputStream bis )
        throws IOException
    {
        try
        {
            bis.mark( 2 );
            DataInputStream ois = new DataInputStream( bis );
            int magic = Short.reverseBytes( ois.readShort() ) & 0xFFFF;
            return magic == GZIPInputStream.GZIP_MAGIC;
        }
        catch ( EOFException e )
        {
            return false;
        }
        finally
        {
            bis.reset();
        }
    }
}
