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
package org.fedoraproject.maven.model;

import java.util.Iterator;
import java.util.concurrent.Semaphore;

import org.apache.maven.model.Build;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Mikolaj Izdebski
 */
public class ModelProcessorTest
    extends PlexusTestCase
{
    /**
     * Test if Plexus can load processor component.
     * 
     * @throws Exception
     */
    public void testComponentLookup()
        throws Exception
    {
        ModelProcessor processor = lookup( ModelProcessor.class );
        assertNotNull( processor );
    }

    /**
     * Test if processor can apply identity transformation on empty model.
     * 
     * @throws Exception
     */
    public void testIdentityTransformation()
        throws Exception
    {
        ModelProcessor processor = lookup( ModelProcessor.class );
        processor.processModel( new Model(), new AbstractModelVisitor() );
    }

    /**
     * Test if processor can retrieve information.
     * 
     * @throws Exception
     */
    public void testInformationRetrieval()
        throws Exception
    {
        Model model = new Model();

        Profile profile = new Profile();
        model.addProfile( profile );

        Dependency dependency = new Dependency();
        profile.addDependency( dependency );

        Exclusion exclusion = new Exclusion();
        exclusion.setArtifactId( "foo" );
        dependency.addExclusion( exclusion );

        Dependency dependency2 = new Dependency();
        model.addDependency( dependency2 );

        Exclusion exclusion2 = new Exclusion();
        exclusion2.setArtifactId( "bar" );
        dependency2.addExclusion( exclusion2 );

        final Semaphore fooSemaphore = new Semaphore( 0 );
        final Semaphore barSemaphore = new Semaphore( 0 );

        ModelProcessor processor = lookup( ModelProcessor.class );
        processor.processModel( model, new AbstractModelVisitor()
        {
            @Override
            public void visitDependencyExclusion( Exclusion exclusion )
            {
                assertEquals( "bar", exclusion.getArtifactId() );
                barSemaphore.release();
            }

            @Override
            public void visitProfileDependencyExclusion( Exclusion exclusion )
            {
                assertEquals( "foo", exclusion.getArtifactId() );
                fooSemaphore.release();
            }

            @Override
            public void visitBuildPluginDependencyExclusion( Exclusion exclusion )
            {
                fail();
            }

            @Override
            public void visitBuildPluginManagementPluginDependencyExclusion( Exclusion exclusion )
            {
                fail();
            }

        } );

        assertEquals( 1, fooSemaphore.availablePermits() );
        assertEquals( 1, barSemaphore.availablePermits() );
    }

    /**
     * Test if processor can apply transformations.
     * 
     * @throws Exception
     */
    public void testTransformations()
        throws Exception
    {
        Model model = new Model();
        model.addModule( "foo" );
        model.addModule( "baz" );
        model.addModule( "bar" );

        Build build = new Build();
        model.setBuild( build );

        PluginManagement pluginManagement = new PluginManagement();
        build.setPluginManagement( pluginManagement );

        Plugin plugin = new Plugin();
        plugin.setGroupId( "xyz" );
        pluginManagement.addPlugin( plugin );

        Reporting reporting = new Reporting();
        model.setReporting( reporting );

        ReportPlugin reportPlugin = new ReportPlugin();
        reporting.addPlugin( reportPlugin );

        model.setCiManagement( new CiManagement() );

        ModelProcessor processor = lookup( ModelProcessor.class );
        processor.processModel( model, new AbstractModelVisitor()
        {
            @Override
            public String replaceModule( String module )
            {
                if ( module.equals( "baz" ) )
                    return null;
                return module.toUpperCase();
            }

            @Override
            public void visitBuildPluginManagementPlugin( Plugin plugin )
            {
                plugin.setArtifactId( "ABC" );
            }

            @Override
            public ReportPlugin replaceReportingPlugin( ReportPlugin plugin )
            {
                return null;
            }

            @Override
            public CiManagement replaceCiManagement( CiManagement ciManagement )
            {
                return null;
            }
        } );

        Iterator<String> moduleIterator = model.getModules().iterator();
        assertEquals( "FOO", moduleIterator.next() );
        assertEquals( "BAR", moduleIterator.next() );
        assertFalse( moduleIterator.hasNext() );

        assertNotNull( model.getBuild() );
        assertNotNull( model.getBuild().getPluginManagement() );
        Iterator<Plugin> pluginIterator = model.getBuild().getPluginManagement().getPlugins().iterator();
        assertTrue( pluginIterator.hasNext() );
        Plugin plugin1 = pluginIterator.next();
        assertEquals( "ABC", plugin1.getArtifactId() );
        assertEquals( plugin1, plugin );
        assertFalse( pluginIterator.hasNext() );

        assertNotNull( model.getReporting() );
        assertTrue( model.getReporting().getPlugins().isEmpty() );

        assertNull( model.getCiManagement() );
    }
}
