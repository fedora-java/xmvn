/*-
 * Copyright (c) 2014-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.ivy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
public class DependencyResolverTest
{
    private IvyResolver ivyResolver = new IvyResolver();

    private Resolver resolver;

    private Deployer deployer;

    @BeforeEach
    public void setUp()
    {
        resolver = EasyMock.createStrictMock( Resolver.class );
        deployer = EasyMock.createStrictMock( Deployer.class );
        ivyResolver = new IvyResolver();
        ivyResolver.setSettings( new IvySettings() );
        ivyResolver.setResolver( resolver );
        ivyResolver.setDeployer( deployer );
    }

    private Path getResource( String id, String ext )
    {
        if ( id == null )
            return null;

        return Paths.get( "src/test/resources/" + id + "." + ext ).toAbsolutePath();
    }

    private void testResolution( String pom, String jar )
        throws Exception
    {
        Artifact pomArtifact = new DefaultArtifact( "gid:aid:pom:ver" );
        ResolutionRequest pomRequest = new ResolutionRequest( pomArtifact );
        Path pomPath = getResource( pom, "pom" );
        ResolutionResult pomResult = new ResolutionResultMock( pomPath );
        expect( resolver.resolve( pomRequest ) ).andReturn( pomResult );

        Artifact jarArtifact = new DefaultArtifact( "gid:aid:jar:ver" );
        ResolutionRequest jarRequest = new ResolutionRequest( jarArtifact );
        Path jarPath = getResource( jar, "jar" );
        ResolutionResult jarResult = new ResolutionResultMock( jarPath );
        expect( resolver.resolve( jarRequest ) ).andReturn( jarResult );

        replay( resolver, deployer );

        ModuleId moduleId = new ModuleId( "gid", "aid" );
        ModuleRevisionId mrid = new ModuleRevisionId( moduleId, "ver" );
        DependencyDescriptor dd = new DefaultDependencyDescriptor( mrid, true );
        ResolvedModuleRevision rmr = ivyResolver.getDependency( dd, null );

        if ( pom == null || !Files.exists( pomPath ) )
        {
            assertNull( rmr );
        }
        else
        {
            assertNotNull( rmr );
            assertEquals( ivyResolver, rmr.getArtifactResolver() );
            assertEquals( "org.codehaus.plexus", rmr.getDescriptor().getModuleRevisionId().getOrganisation() );
            assertEquals( "plexus", rmr.getDescriptor().getModuleRevisionId().getName() );
            assertEquals( "3.3.1", rmr.getDescriptor().getModuleRevisionId().getRevision() );
        }
    }

    @Test
    public void testPomResolutionFailure()
        throws Exception
    {
        testResolution( null, null );
    }

    @Test
    public void testNonExistentPom()
        throws Exception
    {
        testResolution( "non-existent", null );
    }

    @Test
    public void testJarResolutionFailure()
        throws Exception
    {
        testResolution( "plexus", null );
    }

    @Test
    public void testNonExistentJar()
        throws Exception
    {
        testResolution( "plexus", "non-existent" );
    }

    @Test
    public void testSuccess()
        throws Exception
    {
        testResolution( "plexus", "empty" );
    }
}
