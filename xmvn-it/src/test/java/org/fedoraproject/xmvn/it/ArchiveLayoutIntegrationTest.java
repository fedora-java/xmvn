/*-
 * Copyright (c) 2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Test whether binary distribution has expected layout.
 * 
 * @author Mikolaj Izdebski
 */
public class ArchiveLayoutIntegrationTest
    extends AbstractIntegrationTest
{
    private static class PathExpectation
    {
        private final String regex;

        private final Pattern pattern;

        private final int lowerBound;

        private final int upperBound;

        private int matchCount;

        public PathExpectation( int lowerBound, int upperBound, String regex )
        {
            this.regex = regex;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            pattern = Pattern.compile( regex );
        }

        public boolean matches( String path )
        {
            if ( pattern.matcher( path ).matches() )
            {
                matchCount++;
                return true;
            }

            return false;
        }

        public void verify( List<String> errors )
        {
            if ( matchCount < lowerBound || matchCount > upperBound )
            {
                errors.add( "Pattern " + regex + " was expected at least " + lowerBound + " and at most " + upperBound
                    + " times, but was found " + matchCount + " times" );
            }
        }
    }

    private List<PathExpectation> expectations = new ArrayList<>();

    private void expect( int lowerBound, int upperBound, String regex )
    {
        expectations.add( new PathExpectation( lowerBound, upperBound, regex ) );
    }

    private void matchSingleFile( Path baseDir, Path path, String dirSuffix, List<String> errors )
    {
        String pathStr = baseDir.relativize( path ) + dirSuffix;

        if ( expectations.stream().filter( expectation -> expectation.matches( pathStr ) ).count() == 0 )
        {
            errors.add( "Path " + pathStr + " did not match any pattern" );
        }
    }

    private void matchDirectoryTree( Path baseDir, Path dir, List<String> errors )
        throws Exception
    {
        matchSingleFile( baseDir, dir, "/", errors );

        try ( DirectoryStream<Path> ds = Files.newDirectoryStream( dir ) )
        {
            for ( Path path : ds )
            {
                if ( Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) )
                {
                    matchDirectoryTree( baseDir, path, errors );
                }
                else
                {
                    matchSingleFile( baseDir, path, "", errors );
                }
            }
        }
    }

    @Test
    public void testArchiveLayout()
        throws Exception
    {
        expect( 1, 1, "/" );
        expect( 1, 1, "LICENSE" );
        expect( 1, 1, "NOTICE" );
        expect( 1, 1, "README\\.txt" );
        expect( 1, 1, "NOTICE-XMVN" );
        expect( 1, 1, "AUTHORS-XMVN" );
        expect( 1, 1, "README-XMVN\\.md" );

        expect( 1, 1, "bin/" );
        expect( 1, 1, "bin/mvn" );
        expect( 1, 1, "bin/mvn\\.cmd" );
        expect( 1, 1, "bin/mvnDebug" );
        expect( 1, 1, "bin/mvnDebug\\.cmd" );
        expect( 1, 1, "bin/mvnyjp" );
        expect( 1, 1, "bin/m2\\.conf" );

        expect( 1, 1, "boot/" );
        expect( 1, 1, "boot/plexus-classworlds-.*\\.jar" );
        expect( 1, 1, "boot/plexus-classworlds.license" );

        expect( 1, 1, "conf/" );
        expect( 1, 1, "conf/settings\\.xml" );
        expect( 1, 1, "conf/toolchains\\.xml" );
        expect( 1, 1, "conf/logging/" );
        expect( 1, 1, "conf/logging/simplelogger\\.properties" );

        expect( 1, 1, "lib/" );
        expect( 30, 60, "lib/[^/]*\\.jar" );
        expect( 15, 30, "lib/[^/]*\\.license" );
        expect( 10, 100, "lib/jansi-native/.*" );

        expect( 1, 1, "lib/ext/" );
        expect( 1, 1, "lib/ext/README\\.txt" );
        expect( 1, 1, "lib/ext/xmvn-connector-.*\\.jar" );
        expect( 1, 1, "lib/ext/xmvn-core-.*\\.jar" );
        expect( 1, 1, "lib/ext/xmvn-api-.*\\.jar" );

        expect( 1, 1, "lib/installer/" );
        expect( 1, 1, "lib/installer/xmvn-install-.*\\.jar" );
        expect( 1, 1, "lib/installer/xmvn-api-.*\\.jar" );
        expect( 1, 1, "lib/installer/xmvn-core-.*\\.jar" );
        expect( 1, 1, "lib/installer/jcommander-.*\\.jar" );
        expect( 1, 1, "lib/installer/slf4j-api-.*\\.jar" );
        expect( 1, 1, "lib/installer/slf4j-simple-.*\\.jar" );
        expect( 1, 1, "lib/installer/asm-.*\\.jar" );
        expect( 1, 1, "lib/installer/commons-compress-.*\\.jar" );

        expect( 1, 1, "lib/resolver/" );
        expect( 1, 1, "lib/resolver/xmvn-resolve-.*\\.jar" );
        expect( 1, 1, "lib/resolver/xmvn-api-.*\\.jar" );
        expect( 1, 1, "lib/resolver/xmvn-core-.*\\.jar" );
        expect( 1, 1, "lib/resolver/jcommander-.*\\.jar" );

        expect( 1, 1, "lib/subst/" );
        expect( 1, 1, "lib/subst/xmvn-subst-.*\\.jar" );
        expect( 1, 1, "lib/subst/xmvn-api-.*\\.jar" );
        expect( 1, 1, "lib/subst/xmvn-core-.*\\.jar" );
        expect( 1, 1, "lib/subst/jcommander-.*\\.jar" );

        Path baseDir = getMavenHome();
        List<String> errors = new ArrayList<>();
        matchDirectoryTree( baseDir, baseDir, errors );

        for ( PathExpectation expect : expectations )
        {
            expect.verify( errors );
        }

        if ( !errors.isEmpty() )
        {
            fail( String.join( "\n", errors ) );
        }
    }
}
