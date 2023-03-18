/*-
 * Copyright (c) 2015-2023 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;

/**
 * Integration tests for javadoc MOJO.
 * 
 * @author Mikolaj Izdebski
 */
public class JavadocJPMSDuplicateNameIntegrationTest
    extends AbstractMojoIntegrationTest
{
    @Test
    public void testJavadocJPMSDuplicateName()
        throws Exception
    {
        assumeTrue( getJavaVersion() >= 9 );
        performMojoTest( "verify", "javadoc" );

        assertTrue( Files.isDirectory( Paths.get( "target/xmvn-apidocs" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "target/xmvn-apidocs/libAB/app/A.html" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "target/xmvn-apidocs/libAB/app/B.html" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "target/xmvn-apidocs/libAB/app/AB.html" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "target/xmvn-apidocs/libAB/module-summary.html" ) ) );
        assertTrue( Files.isSymbolicLink( Paths.get( ".xmvn/apidocs" ) ) );
        assertTrue( Files.isSameFile( Paths.get( ".xmvn/apidocs" ), Paths.get( "target/xmvn-apidocs" ) ) );
    }
}
