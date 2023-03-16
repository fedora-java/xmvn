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
package org.fedoraproject.xmvn.connector.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.inject.Binder;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.config.Artifact;
import org.fedoraproject.xmvn.config.Configurator;

/**
 * @author Mikolaj Izdebski
 * @author Roman Vais
 */
public class ModelValidatorTest
    extends AbstractTest
{
    private ModelValidator validator;

    private Configurator configurator;

    private Model model;

    private Build build;

    private ArrayList<Dependency> dl;

    private ArrayList<Extension> el;

    private ArrayList<Plugin> pl;

    private Path getResource( String resource )
    {
        if ( resource == null )
        {
            return null;
        }
        return Paths.get( "src/test/resources" ).resolve( resource ).toAbsolutePath();
    }

    @Override
    public void configure( Binder binder )
    {
        binder.bind( ModelValidator.class ).to( XMvnModelValidator.class );
    }

    @BeforeEach
    public void setUp()
        throws Exception
    {
        configurator = lookup( Configurator.class );
        validator = lookup( ModelValidator.class );

        Xpp3Dom mainCfg;
        Xpp3Dom glCfg;
        Xpp3Dom locCfg;
        try ( Reader reader = new FileReader( getResource( "test.pom.xml" ).toFile() ) )
        {
            mainCfg = Xpp3DomBuilder.build( reader );
        }
        glCfg = mainCfg.getChild( "basic" ).getChild( "configuration" );

        dl = new ArrayList<>();
        Dependency dep = new Dependency();
        dl.add( dep );

        el = new ArrayList<>();
        Extension ext = new Extension();
        el.add( ext );

        ArrayList<PluginExecution> pel;
        pel = new ArrayList<>();

        PluginExecution exec;

        locCfg = mainCfg.getChild( "invalidst" ).getChild( "configuration" );
        exec = new PluginExecution();
        exec.setConfiguration( locCfg );
        pel.add( exec );

        locCfg = mainCfg.getChild( "invalidnd" ).getChild( "configuration" );
        exec = new PluginExecution();
        exec.setConfiguration( locCfg );
        pel.add( exec );

        locCfg = mainCfg.getChild( "empty" ).getChild( "configuration" );
        exec = new PluginExecution();
        exec.setConfiguration( locCfg );
        pel.add( exec );

        pl = new ArrayList<>();
        Plugin plg;

        plg = new Plugin();
        plg.setGroupId( "org.apache.maven.plugins" );
        plg.setArtifactId( "maven-compiler-plugin" );
        plg.setConfiguration( glCfg );
        plg.addDependency( dep );
        plg.setExecutions( pel );
        pl.add( plg );

        plg = new Plugin();
        plg.setGroupId( "org.apache.maven.plugins" );
        plg.setArtifactId( "foo" );
        plg.setConfiguration( glCfg );
        plg.addDependency( dep );
        plg.setVersion( "starter edition" );
        pl.add( plg );

        plg = new Plugin();
        plg.setGroupId( "foofoo" );
        plg.setArtifactId( "maven-compiler-plugin" );
        plg.setConfiguration( glCfg );
        plg.addDependency( dep );
        plg.setVersion( "[1.0]" );
        pl.add( plg );

        plg = new Plugin();
        plg.setGroupId( "foobar" );
        plg.setArtifactId( "bar" );
        plg.setConfiguration( glCfg );
        plg.addDependency( dep );
        plg.setVersion( "(" );
        pl.add( plg );

        build = EasyMock.createMock( Build.class );
        model = EasyMock.createMock( Model.class );
    }

    @Test
    public void testMinimalModelValidation()
        throws Exception
    {
        EasyMock.expect( model.getBuild() ).andReturn( null ).atLeastOnce();
        EasyMock.expect( model.getDependencies() ).andReturn( new ArrayList<>() ).atLeastOnce();

        EasyMock.replay( model );

        assertTrue( validator instanceof XMvnModelValidator );
        ( (XMvnModelValidator) validator ).customizeModel( model );
        EasyMock.verify( model );
    }

    @Test
    public void testAdvancedModelValidation()
        throws Exception
    {
        EasyMock.expect( build.getExtensions() ).andReturn( el ).atLeastOnce();
        EasyMock.expect( build.getPlugins() ).andReturn( new ArrayList<>() ).atLeastOnce();

        EasyMock.expect( model.getBuild() ).andReturn( build ).atLeastOnce();
        EasyMock.expect( model.getDependencies() ).andReturn( dl ).atLeastOnce();

        EasyMock.replay( model, build );

        ( (XMvnModelValidator) validator ).customizeModel( model );
        EasyMock.verify( model, build );
    }

    @Test
    public void testFullModelValidation()
        throws Exception
    {
        EasyMock.expect( build.getExtensions() ).andReturn( el ).atLeastOnce();
        EasyMock.expect( build.getPlugins() ).andReturn( pl ).atLeastOnce();

        EasyMock.expect( model.getBuild() ).andReturn( build ).atLeastOnce();
        EasyMock.expect( model.getDependencies() ).andReturn( dl ).atLeastOnce();

        EasyMock.replay( model, build );

        ( (XMvnModelValidator) validator ).customizeModel( model );
        EasyMock.verify( model, build );
    }

    @Test
    public void testRemovingTestDeps()
        throws Exception
    {
        configurator.getConfiguration().getBuildSettings().setSkipTests( true );
        dl.iterator().next().setScope( "test" );

        EasyMock.expect( model.getBuild() ).andReturn( null ).atLeastOnce();
        EasyMock.expect( model.getDependencies() ).andReturn( dl ).atLeastOnce();
        EasyMock.replay( model );

        ( (XMvnModelValidator) validator ).customizeModel( model );
        EasyMock.verify( model );

        assertEquals( 0, dl.size() );
    }

    @Test
    public void testSkippedPlugins()
        throws Exception
    {
        Artifact sp1 = new Artifact();
        sp1.setArtifactId( "maven-compiler-plugin" );
        configurator.getConfiguration().getBuildSettings().getSkippedPlugins().add( sp1 );
        Artifact sp2 = new Artifact();
        sp2.setGroupId( "org.apache.maven.plugins" );
        sp2.setVersion( "starter edition" );
        configurator.getConfiguration().getBuildSettings().getSkippedPlugins().add( sp2 );

        EasyMock.expect( build.getExtensions() ).andReturn( new ArrayList<>() ).atLeastOnce();
        EasyMock.expect( build.getPlugins() ).andReturn( pl ).atLeastOnce();
        EasyMock.expect( model.getBuild() ).andReturn( build ).atLeastOnce();
        EasyMock.expect( model.getDependencies() ).andReturn( new ArrayList<>() ).atLeastOnce();
        EasyMock.replay( build, model );

        ( (XMvnModelValidator) validator ).customizeModel( model );
        EasyMock.verify( model );

        assertEquals( 1, pl.size() );
        Plugin plugin = pl.iterator().next();
        assertEquals( "foobar", plugin.getGroupId() );
        assertEquals( "bar", plugin.getArtifactId() );
    }
}
