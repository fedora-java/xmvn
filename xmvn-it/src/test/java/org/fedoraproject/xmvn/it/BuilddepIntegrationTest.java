/*-
 * Copyright (c) 2015-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fedoraproject.xmvn.tools.install.condition.DomUtils;

/**
 * @author Mikolaj Izdebski
 */
public class BuilddepIntegrationTest
    extends AbstractMojoIntegrationTest
{
    /**
     * @author Mikolaj Izdebski
     */
    public static interface Visitor
    {
        void visit( String groupId, String artifactId, String version );
    }

    private Visitor visitor = EasyMock.createMock( Visitor.class );

    public void expectBuildDependency( String groupId, String artifactId )
    {
        expectBulidDependency( groupId, artifactId, null );
    }

    public void expectBulidDependency( String groupId, String artifactId, String version )
    {
        visitor.visit( groupId, artifactId, version );
        EasyMock.expectLastCall();
    }

    private void verifyBuilddepXml()
        throws Exception
    {
        Path builddepPath = Paths.get( ".xmvn-builddep" );
        assertTrue( Files.isRegularFile( builddepPath ) );

        for ( Element dep : DomUtils.parseAsParent( DomUtils.parse( builddepPath ) ) )
        {
            assertEquals( "dependency", dep.getNodeName() );
            Map<String, String> children =
                DomUtils.parseAsParent( dep ).stream() //
                        .collect( Collectors.toMap( Node::getNodeName, DomUtils::parseAsText ) );
            visitor.visit( children.get( "groupId" ), children.get( "artifactId" ), children.get( "version" ) );
        }
    }

    public void performBuilddepTest()
        throws Exception
    {
        performTest( "verify", "org.fedoraproject.xmvn:xmvn-mojo:builddep" );

        EasyMock.replay( visitor );
        verifyBuilddepXml();
        EasyMock.verify( visitor );
    }

    @Test
    public void testBuilddepExpandVariables()
        throws Exception
    {
        expectBuildDependency( "junit", "junit" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepPackaging()
        throws Exception
    {
        expectBuildDependency( "junit", "junit" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepReactorDependencies()
        throws Exception
    {
        performBuilddepTest();
    }

    @Test
    public void testBuilddepSkippedTestDependencies()
        throws Exception
    {
        expectBuildDependency( "xpp3", "xpp3" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepUnusedPlugins()
        throws Exception
    {
        performBuilddepTest();
    }

    @Test
    public void testBuilddepMavenPluginPlugin()
        throws Exception
    {
        expectBuildDependency( "org.apache.maven.plugins", "maven-plugin-plugin" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepMavenPluginPluginManaged()
        throws Exception
    {
        expectBuildDependency( "org.apache.maven.plugins", "maven-plugin-plugin" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepSubmodule()
        throws Exception
    {
        expectBuildDependency( "org.codehaus.plexus", "plexus-component-metadata" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepSubmoduleInheritance()
        throws Exception
    {
        expectBuildDependency( "org.codehaus.plexus", "plexus-component-metadata" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepExternalInheritance()
        throws Exception
    {
        performBuilddepTest();
    }

    @Test
    public void testBuilddepProfiles()
        throws Exception
    {
        expectBuildDependency( "junit", "junit" );
        expectBuildDependency( "org.codehaus.plexus", "plexus-component-metadata" );
        performBuilddepTest();
    }

    @Test
    public void testBuilddepProfileActivation()
        throws Exception
    {
        expectBuildDependency( "junit", "junit" );
        expectBuildDependency( "org.codehaus.plexus", "plexus-component-metadata" );
        performBuilddepTest();
    }
}
