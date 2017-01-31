/*-
 * Copyright (c) 2016-2017 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class ResolverIntegrationTest
    extends AbstractIntegrationTest
{
    @Test
    public void testResolveNone()
        throws Exception
    {
        ProcessBuilder pb = buildToolSubprocess( "xmvn-resolve" );

        assertEquals( 0, pb.start().waitFor() );
        assertFalse( getStderr().findAny().isPresent() );
        assertFalse( getStdout().findAny().isPresent() );
    }

    @Test
    public void testResolveOne()
        throws Exception
    {
        ProcessBuilder pb = buildToolSubprocess( "xmvn-resolve", "junit:junit" );

        assertEquals( 0, pb.start().waitFor() );
        assertFalse( getStderr().findAny().isPresent() );

        List<String> out = getStdout().collect( Collectors.toList() );
        assertEquals( 1, out.size() );

        Path jar = Paths.get( out.iterator().next() );
        assertTrue( jar.endsWith( "src/test/resources/empty.jar" ) );
        assertTrue( Files.isRegularFile( jar ) );
    }

    @Test
    public void testResolveFail()
        throws Exception
    {
        ProcessBuilder pb = buildToolSubprocess( "xmvn-resolve", "foobar:xyzzy" );

        assertEquals( 1, pb.start().waitFor() );
        assertTrue( getStderr().anyMatch( s -> s.endsWith( "Unable to resolve artifact foobar:xyzzy:jar:SYSTEM" ) ) );
        assertFalse( getStdout().findAny().isPresent() );
    }
}
