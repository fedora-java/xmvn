/*-
 * Copyright (c) 2022 Red Hat, Inc.
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

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;

/**
 * Integration tests for javadoc MOJO.
 * 
 * @author Marian Koncek
 */
public class JavadocExcludePackagesRegexTest
    extends AbstractMojoIntegrationTest
{
    @Test
    public void testJavadocExcludePackagesRegex()
        throws Exception
    {
        performTest( "org.fedoraproject.xmvn:xmvn-mojo:javadoc" );

        assertTrue( Files.isDirectory( Paths.get( "target/xmvn-apidocs/b/y" ) ) );
        assertTrue( Files.isDirectory( Paths.get( "target/xmvn-apidocs/b/z" ) ) );
        assertTrue( Files.isDirectory( Paths.get( "target/xmvn-apidocs/c/z" ) ) );

        assertTrue( Files.notExists( Paths.get( "target/xmvn-apidocs/a" ) ) );
        assertTrue( Files.notExists( Paths.get( "target/xmvn-apidocs/b/x" ) ) );
        assertTrue( Files.notExists( Paths.get( "target/xmvn-apidocs/c/x" ) ) );
        assertTrue( Files.notExists( Paths.get( "target/xmvn-apidocs/c/y" ) ) );
    }
}
