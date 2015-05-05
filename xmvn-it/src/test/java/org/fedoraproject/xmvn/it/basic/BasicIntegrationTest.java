/*-
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.fedoraproject.xmvn.it.AbstractIntegrationTest;

/**
 * @author Mikolaj Izdebski
 */
public class BasicIntegrationTest
    extends AbstractIntegrationTest
{
    @Test
    public void testVersion()
        throws Exception
    {
        performTest( "-v" );
        assertTrue( getStdout().anyMatch( s -> s.contains( "Apache Maven" ) ) );
        assertTrue( getStdout().anyMatch( s -> s.equals( "Maven home: " + getMavenHome() ) ) );
        assertFalse( getStdout().anyMatch( s -> s.toLowerCase().matches( ".*(error|exception|fail).*" ) ) );
    }

    @Test
    public void testNoGoals()
        throws Exception
    {
        expectFailure();
        performTest();
        assertTrue( getStdout().anyMatch( s -> s.startsWith( "[ERROR] No goals have been specified for this build." ) ) );
    }

    @Test
    public void testNoPom()
        throws Exception
    {
        expectFailure();
        performTest( "validate" );
        assertTrue( getStdout().anyMatch( s -> s.startsWith( "[ERROR] The goal you specified requires a project to execute but there is no POM in this directory" ) ) );
    }
}
