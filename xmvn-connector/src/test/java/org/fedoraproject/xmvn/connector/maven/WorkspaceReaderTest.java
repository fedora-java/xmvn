/*-
 * Copyright (c) 2016-2021 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import com.google.inject.Binder;
import org.easymock.EasyMock;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
public class WorkspaceReaderTest
    extends AbstractTest
{
    private WorkspaceReader workspace;

    private Resolver resolver;

    @Override
    public void configure( Binder binder )
    {
        // Nothing to do
    }

    @BeforeEach
    public void setUp()
        throws Exception
    {
        resolver = EasyMock.createMock( Resolver.class );
        getContainer().addComponent( resolver, Resolver.class, "default" );

        workspace = lookup( WorkspaceReader.class, "ide" );
    }

    @Test
    public void testDependencyInjection()
        throws Exception
    {
        assertNotNull( workspace );
        assertTrue( workspace instanceof XMvnWorkspaceReader );
    }

    @Test
    public void testFindArtifact()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( Paths.get( "/foo/bar" ) );
        EasyMock.replay( resolver, result );

        File file = workspace.findArtifact( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result );

        assertEquals( new File( "/foo/bar" ), file );
    }

    @Test
    public void testArtifactNotFound()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( null );
        EasyMock.replay( resolver, result );

        File file = workspace.findArtifact( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result );

        assertEquals( null, file );
    }

    @Test
    public void testResolutionListener()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );
        ResolutionListener listener = EasyMock.createMock( ResolutionListener.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( Paths.get( "/foo/bar" ) );
        listener.resolutionRequested( request );
        EasyMock.expectLastCall();
        listener.resolutionCompleted( request, result );
        EasyMock.expectLastCall();
        EasyMock.replay( resolver, result, listener );

        ( (XMvnWorkspaceReader) workspace ).addResolutionListener( listener );
        workspace.findArtifact( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result, listener );
    }

    @Test
    public void testFindVersionsSystem()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( Paths.get( "/foo/bar" ) );
        EasyMock.expect( result.getCompatVersion() ).andReturn( null );
        EasyMock.replay( resolver, result );

        List<String> versions =
            workspace.findVersions( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result );

        assertEquals( 1, versions.size() );
        assertEquals( "SYSTEM", versions.get( 0 ) );
    }

    @Test
    public void testFindVersionsCompat()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( Paths.get( "/foo/bar" ) );
        EasyMock.expect( result.getCompatVersion() ).andReturn( "4.5.6" );
        EasyMock.replay( resolver, result );

        List<String> versions =
            workspace.findVersions( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result );

        assertEquals( 1, versions.size() );
        assertEquals( "4.5.6", versions.get( 0 ) );
    }

    @Test
    public void testFindVersionsNotFound()
        throws Exception
    {
        ResolutionRequest request = new ResolutionRequest( new DefaultArtifact( "foo:bar:1.2.3" ) );
        ResolutionResult result = EasyMock.createMock( ResolutionResult.class );

        EasyMock.expect( resolver.resolve( request ) ).andReturn( result );
        EasyMock.expect( result.getArtifactPath() ).andReturn( null );
        EasyMock.replay( resolver, result );

        List<String> versions =
            workspace.findVersions( new org.eclipse.aether.artifact.DefaultArtifact( "foo:bar:1.2.3" ) );
        EasyMock.verify( resolver, result );

        assertEquals( 0, versions.size() );
    }

    @Test
    public void testGetRepository()
        throws Exception
    {
        WorkspaceRepository repository = workspace.getRepository();
        assertNotNull( repository );
    }
}
