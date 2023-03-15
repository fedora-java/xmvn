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
package org.fedoraproject.xmvn.it.maven.mojo.builddep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;
import org.fedoraproject.xmvn.tools.install.condition.DomUtils;

/**
 * Integration tests for builddep MOJO.
 * 
 * @author Mikolaj Izdebski
 */
public abstract class AbstractBuilddepIntegrationTest
    extends AbstractMojoIntegrationTest
{
    /**
     * @author Mikolaj Izdebski
     */
    public interface Visitor
    {
        void visit( String groupId, String artifactId, String version );
    }

    private final Visitor visitor = EasyMock.createMock( Visitor.class );

    public void expectBuildDependency( String groupId, String artifactId )
    {
        expectBulidDependency( groupId, artifactId, null );
    }

    public void expectBulidDependency( String groupId, String artifactId, String version )
    {
        visitor.visit( groupId, artifactId, version );
        EasyMock.expectLastCall();
    }

    public void verifyBuilddepXml()
        throws Exception
    {
        Path builddepPath = Paths.get( ".xmvn-builddep" );
        assertTrue( Files.isRegularFile( builddepPath ) );

        for ( Element dep : DomUtils.parseAsParent( DomUtils.parse( builddepPath ) ) )
        {
            assertEquals( "dependency", dep.getNodeName() );
            Map<String, String> children = DomUtils.parseAsParent( dep ).stream() //
                                                   .collect( Collectors.toMap( Node::getNodeName,
                                                                               DomUtils::parseAsText ) );
            visitor.visit( children.get( "groupId" ), children.get( "artifactId" ), children.get( "version" ) );
        }
    }

    public void performBuilddepTest()
        throws Exception
    {
        performMojoTest( "verify", "builddep" );

        EasyMock.replay( visitor );
        verifyBuilddepXml();
        EasyMock.verify( visitor );
    }
}
