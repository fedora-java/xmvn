/*-
 * Copyright (c) 2014-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for javadoc MOJO.
 * 
 * @author Mikolaj Izdebski
 */
public class JavadocMojoTest
{
    @Test
    public void testJavadocIsModular8()
        throws Exception
    {
        Path javadocPath = Paths.get( "/usr/lib/jvm/java-1.8.0/bin/javadoc" );
        assumeTrue( Files.isExecutable( javadocPath ) );
        assertFalse( JavadocMojo.isJavadocModular( javadocPath ) );
    }

    @Test
    public void testJavadocIsModular11()
        throws Exception
    {
        Path javadocPath = Paths.get( "/usr/lib/jvm/java-11/bin/javadoc" );
        assumeTrue( Files.isExecutable( javadocPath ) );
        assertTrue( JavadocMojo.isJavadocModular( javadocPath ) );
    }

    @Test
    public void testJavadocIsModularError()
        throws Exception
    {
        Path javadocPath = Paths.get( "/bin/false" );
        assumeTrue( Files.isExecutable( javadocPath ) );
        try
        {
            JavadocMojo.isJavadocModular( javadocPath );
            fail();
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( e.getMessage().startsWith( "Javadoc failed" ) );
        }
    }
}
