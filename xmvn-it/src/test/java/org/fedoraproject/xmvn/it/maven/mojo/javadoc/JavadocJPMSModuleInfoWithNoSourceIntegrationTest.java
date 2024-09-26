/*-
 * Copyright (c) 2015-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo.javadoc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;

/**
 * Integration tests for javadoc MOJO.
 * 
 * @author Marian Koncek
 */
public class JavadocJPMSModuleInfoWithNoSourceIntegrationTest
    extends AbstractMojoIntegrationTest
{
    @Test
    public void testJavadocJPMSModuleInfoWithNoSource()
        throws Exception
    {
        assumeTrue( getJavaVersion() >= 9 );
        performMojoTest( "verify", "javadoc" );

        assertTrue( getStdout().anyMatch( "[WARNING] Skipping Javadoc generation: no Java sources found"::equals ) );
        assertFalse( Files.exists( Paths.get( "target/xmvn-apidocs" ) ) );
        assertFalse( Files.exists( Paths.get( ".xmvn/apidocs" ) ) );
    }
}
