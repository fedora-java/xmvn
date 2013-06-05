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

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author Mikolaj Izdebski
 */
public class JavaVersionTest
    extends AbstractDependencyTest
{
    private void testJavaVersionDependencies( String roleHint, boolean managed )
        throws Exception
    {
        Plugin compilerPlugin = new Plugin();
        compilerPlugin.setArtifactId( "maven-compiler-plugin" );
        Model model = new Model();
        model.setBuild( new Build() );

        if ( managed )
        {
            model.getBuild().setPluginManagement( new PluginManagement() );
            model.getBuild().getPluginManagement().addPlugin( compilerPlugin );
        }
        else
        {
            model.getBuild().addPlugin( compilerPlugin );
        }

        // No configuration.
        setModel( model );
        performTests( roleHint );

        // Empty configuration.
        Xpp3Dom config = new Xpp3Dom( "configuration" );
        compilerPlugin.setConfiguration( config );
        performTests( roleHint );

        // Source with no value.
        Xpp3Dom source = new Xpp3Dom( "source" );
        config.addChild( source );
        performTests( roleHint );

        // Incorrect values should not cause any exception.
        source.setValue( "foo-bar" );
        performTests( roleHint );

        // Values like 7 and 7.0 need to be supported in addition to 1.7.
        source.setValue( "1.7" );
        expectJavaVersion( "1.7" );
        performTests( roleHint );
        source.setValue( "7.0" );
        performTests( roleHint );
        source.setValue( "7" );
        performTests( roleHint );

        // XML values ought to be trimmed.
        source.setValue( " \r 1.3   \n\t" );
        expectJavaVersion( "1.3" );
        performTests( roleHint );

        // Source 1.3 but target 1.6.
        Xpp3Dom target = new Xpp3Dom( "target" );
        config.addChild( target );
        target.setValue( "1.6" );
        expectJavaVersion( "1.6" );
        performTests( roleHint );

        // Main configuration specifies source 1.3 target 1.6, but there is plugin execution that specifies source 8.
        PluginExecution execution = new PluginExecution();
        compilerPlugin.addExecution( execution );
        Xpp3Dom execConfig = new Xpp3Dom( "configuration" );
        execution.setConfiguration( execConfig );
        Xpp3Dom execSource = new Xpp3Dom( "source" );
        execSource.setValue( "8" );
        execConfig.addChild( execSource );
        expectJavaVersion( "1.8" );
        performTests( roleHint );

        // Special targets jsr14 and jsr14 should be handled correctly.
        config = new Xpp3Dom( "configuration" );
        config.addChild( target );
        target.setValue( "jsr14" );
        expectJavaVersion( "1.4" );
        target.setValue( "cldc1.1" );
        expectJavaVersion( "1.1" );
    }

    public void testBuildJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.BUILD, false );
    }

    public void testBuildManagedJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.BUILD, true );
    }

    public void testRuntimeJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.RUNTIME, false );
    }

    public void testRuntimeManagedJavaVersionDependencies()
        throws Exception
    {
        testJavaVersionDependencies( DependencyExtractor.RUNTIME, true );
    }
}
