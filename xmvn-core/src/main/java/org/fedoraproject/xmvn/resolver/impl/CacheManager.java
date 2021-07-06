/*-
 * Copyright (c) 2015-2021 Red Hat, Inc.
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
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mikolaj Izdebski
 */
class CacheManager
{
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final MessageDigest digest;

    private static volatile Path cacheHome;

    public CacheManager()
    {
        try
        {
            digest = MessageDigest.getInstance( DIGEST_ALGORITHM );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "Digest algorithm " + DIGEST_ALGORITHM + " is not available", e );
        }
    }

    private String hash( Path path )
        throws IOException
    {
        return hash( Files.readAllBytes( path ) );
    }

    String hash( byte[] bytes )
    {
        byte[] digest = this.digest.digest( bytes );
        return new BigInteger( 1, digest ).setBit( digest.length << 3 ).toString( 16 ).substring( 1 ).toUpperCase();
    }

    private static Path getPathDefault( String key, Object defaultValue )
    {
        String value = System.getenv( key );
        if ( value == null || value.isEmpty() )
        {
            value = defaultValue.toString();
        }

        return Paths.get( value );
    }

    private static Path getCacheHome()
    {
        if ( cacheHome == null )
        {
            Path xdgHome = getPathDefault( "HOME", System.getProperty( "user.home" ) );
            Path cacheRoot = getPathDefault( "XDG_CONFIG_HOME", xdgHome.resolve( ".cache" ) );
            cacheHome = cacheRoot.resolve( "xmvn" );
        }

        return cacheHome;
    }

    public Path cacheFile( Path path )
        throws IOException
    {
        String hash = hash( path );
        String hash1 = hash.substring( 0, 2 );

        Path cacheDir = getCacheHome().resolve( hash1 ).resolve( hash );
        Files.createDirectories( cacheDir );

        Path cacheFile = cacheDir.resolve( path.getFileName() );

        if ( !Files.isRegularFile( cacheFile ) )
        {
            Files.copy( path, cacheFile );
        }

        return cacheFile;
    }
}
