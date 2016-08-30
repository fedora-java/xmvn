/*-
 * Copyright (c) 2016 Red Hat, Inc.
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class BisectIntegrationTest
    extends AbstractIntegrationTest
{
    private int runBisect()
        throws Exception
    {
        Path exe = getMavenHome().resolve( "bin/xmvn-bisect" );

        ProcessBuilder pb = new ProcessBuilder( exe.toString(), "-Dxmvn.config.sandbox=true", "clean", "compile" );
        pb.redirectInput( new File( "/dev/null" ) );
        pb.redirectOutput( new File( STDOUT ) );
        pb.redirectError( new File( STDERR ) );
        Process p = pb.start();
        return p.waitFor();
    }

    @Test
    public void testBisect()
        throws Exception
    {
        assertEquals( 0, runBisect() );

        assertTrue( getStderr().anyMatch( s -> s.endsWith( "Bisection build finished" ) ) );
        assertTrue( getStderr().anyMatch( s -> s.contains( "Failed build:" ) ) );
        assertTrue( getStderr().anyMatch( s -> s.contains( "Successful build:" ) ) );
    }
}
