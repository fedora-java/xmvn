/*-
 * Copyright (c) 2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.aether;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.plugin.version.PluginVersionResult;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Roman Vais
 */
public class PluginVersionResolverTest
{
    @TestSubject
    private PluginVersionResolver resolver;

    @Mock( type = MockType.STRICT )
    private PluginVersionRequest rq;

    @Mock( type = MockType.STRICT )
    private RepositorySystemSession session;

    @Mock( type = MockType.STRICT )
    private WorkspaceReader reader;

    private WorkspaceRepository repo;

    @Before
    public void setUp()
    {
        resolver = new XMvnPluginVersionResolver();

        rq = EasyMock.createMock( PluginVersionRequest.class );
        session = EasyMock.createMock( RepositorySystemSession.class );
        reader = EasyMock.createMock( WorkspaceReader.class );
        repo = new WorkspaceRepository();
    }

    @Test
    public void testInjection()
        throws Exception
    {
        Assert.assertTrue( this.resolver instanceof XMvnPluginVersionResolver );
    }

    @Test
    public void testResolution()
        throws Exception
    {
        // test of default version resolution (version list is empty)
        EasyMock.expect( rq.getRepositorySession() ).andReturn( session );
        EasyMock.expect( rq.getGroupId() ).andReturn( "test.example" );
        EasyMock.expect( rq.getArtifactId() ).andReturn( "nonexistent" );

        EasyMock.expect( session.getWorkspaceReader() ).andReturn( reader );

        EasyMock.expect( reader.findVersions( EasyMock.anyObject( DefaultArtifact.class ) ) ).andReturn( Collections.emptyList() );
        EasyMock.expect( reader.getRepository() ).andReturn( repo );

        EasyMock.replay( rq, session, reader );

        PluginVersionResult result;

        result = resolver.resolve( rq );
        Assert.assertTrue( result.getRepository().equals( repo ) );
        Assert.assertTrue( result.getVersion().equals( Artifact.DEFAULT_VERSION ) );

        EasyMock.verify( rq, session, reader );

        // test of arbitrary version resolution (version list contains some version)
        ArrayList<String> list = new ArrayList<>();
        list.add( "1.2.3" );

        EasyMock.reset( rq, session, reader );
        EasyMock.expect( rq.getRepositorySession() ).andReturn( session );
        EasyMock.expect( rq.getGroupId() ).andReturn( "test.example" );
        EasyMock.expect( rq.getArtifactId() ).andReturn( "nonexistent" );

        EasyMock.expect( session.getWorkspaceReader() ).andReturn( reader );

        EasyMock.expect( reader.findVersions( EasyMock.anyObject( DefaultArtifact.class ) ) ).andReturn( list );
        EasyMock.expect( reader.getRepository() ).andReturn( repo );

        EasyMock.replay( rq, session, reader );

        result = resolver.resolve( rq );
        Assert.assertTrue( result.getRepository().equals( repo ) );
        Assert.assertTrue( result.getVersion().equals( "1.2.3" ) );

        EasyMock.verify( rq, session, reader );
    }

}
