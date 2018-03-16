/*-
 * Copyright (c) 2015-2017 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Basic integration tests for XMvn (Maven with extensions).
 * 
 * @author Mikolaj Izdebski
 */
public class BasicIntegrationTest
    extends AbstractMavenIntegrationTest
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
        assertTrue( getStdout().anyMatch( s -> s.startsWith( "[ERROR] The goal you specified requires a project to execute "
            + "but there is no POM in this directory" ) ) );
    }

    /**
     * This test is supposed to verify that XMvn can be executed and that it can successfully build the most basic
     * project.
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyProject()
        throws Exception
    {
        performTest( "verify" );
    }

    /**
     * This project won't compile with upstream Maven -- it uses generics, but declares source 1.4 (generics are
     * supported since 1.5). This project should however compile successfully with XMvn as it overrides default source
     * setting.
     * 
     * @throws Exception
     */
    @Test
    public void testCompilerSource15()
        throws Exception
    {
        performTest( "verify" );
    }

    /**
     * This test uses `enum' as identifier, which is not supported in Java 1.5. This project is expected to fail with
     * upstream Maven (as compiler source is set to 1.6) and succeed with XMvn (as special configuration sets source to
     * 1.4).
     * 
     * @throws Exception
     */
    @Test
    public void testOverrideCompilerSource()
        throws Exception
    {
        performTest( "verify" );
    }

    @Test
    public void testPlugin()
        throws Exception
    {
        performTest( "process-classes" );
        assertTrue( getStdout().anyMatch( s -> s.startsWith( "[INFO] --- plexus-component-metadata:1.7.1:generate-metadata (default)" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "src/main/resources/META-INF/plexus/components.xml" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "component-metadata-test.xml" ) ) );
    }

    @Test
    public void testPluginAlias()
        throws Exception
    {
        performTest( "process-classes" );
        assertTrue( getStdout().anyMatch( s -> s.startsWith( "[INFO] --- plexus-component-metadata:1.7.1:generate-metadata (default)" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "src/main/resources/META-INF/plexus/components.xml" ) ) );
        assertTrue( Files.isRegularFile( Paths.get( "component-metadata-test.xml" ) ) );
    }
}
