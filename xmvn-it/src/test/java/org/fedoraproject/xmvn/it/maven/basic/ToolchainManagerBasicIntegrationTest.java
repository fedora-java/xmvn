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
package org.fedoraproject.xmvn.it.maven.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;

/**
 * Basic integration tests for XMvn (Maven with extensions).
 * 
 * @author Mikolaj Izdebski
 */
public class ToolchainManagerBasicIntegrationTest
    extends AbstractMavenIntegrationTest
{
    @Test
    public void testToolchainManager()
        throws Exception
    {
        performTest( "verify" );
        assertTrue( getStdout().anyMatch( s -> s.equals( "[INFO] Toolchain in compiler-plugin: JDK[/tmp]" ) ) );
        assertTrue( getStdout().anyMatch( s -> s.equals( "[INFO] Toolchain in surefire-plugin: JDK[/tmp]" ) ) );
    }
}
