/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.dependency;

/**
 * @author Mikolaj Izdebski
 */
public class JavaVersionTest
    extends AbstractDependencyTest
{
    private void testJavaVersionDependencies( String roleHint )
        throws Exception
    {
        // No configuration.
        setModel( "java-version-unspecified.xml" );
        performTests( roleHint );

        // Empty configuration.
        setModel( "java-version-empty.xml" );
        performTests( roleHint );

        // Source with no value.
        setModel( "java-version-source-empty.xml" );
        performTests( roleHint );

        // Incorrect values should not cause any exception.
        setModel( "java-version-source-incorrect.xml" );
        performTests( roleHint );

        // Values like 7 and 7.0 need to be supported in addition to 1.7.
        setModel( "java-version-source-1.7.xml" );
        expectJavaVersion( "1.7" );
        performTests( roleHint );
        setModel( "java-version-source-7.0.xml" );
        performTests( roleHint );
        setModel( "java-version-source-7.xml" );
        performTests( roleHint );

        // XML values ought to be trimmed.
        setModel( "java-version-whitespace-trim.xml" );
        expectJavaVersion( "1.3" );
        performTests( roleHint );

        // Source 1.3 but target 1.6.
        setModel( "java-version-source-1.3-target-1.6.xml" );
        expectJavaVersion( "1.6" );
        performTests( roleHint );

        // Main configuration specifies source 1.3 target 1.6, but there is plugin execution that specifies source 8.
        setModel( "java-version-execution-1.8.xml" );
        expectJavaVersion( "1.8" );
        performTests( roleHint );

        // Special targets jsr14 and jsr14 should be handled correctly.
        setModel( "java-version-jsr14.xml" );
        expectJavaVersion( "1.4" );
        setModel( "java-version-cldc1.1.xml" );
        expectJavaVersion( "1.1" );
    }

    public void testBuildJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.BUILD );
    }

    public void testRuntimeJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.RUNTIME );
    }
}
