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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
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
interface ReportVisitor
{
    void visitArtifact( Artifact artifact );
}

/**
 * @author Mikolaj Izdebski
 */
public class Bug1127804Test
{
    private Resolver resolver;

    private Deployer deployer;

    private Ivy ivy;

    private ReportVisitor visitor;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        resolver = createMock( Resolver.class );
        deployer = createStrictMock( Deployer.class );
        visitor = createMock( ReportVisitor.class );

        IvyResolver ivyResolver = new IvyResolver();
        ivyResolver.setResolver( resolver );
        ivyResolver.setDeployer( deployer );

        IvySettings settings = new IvySettings();
        settings.addResolver( ivyResolver );
        settings.setDefaultResolver( "XMvn" );

        ivy = Ivy.newInstance( settings );
    }

    private Path getResource( String resource )
    {
        if ( resource == null )
            return null;
        return Paths.get( "src/test/resources" ).resolve( resource ).toAbsolutePath();
    }

    public void addArtifact( String coordinates, String resource )
    {
        Artifact artifact = new DefaultArtifact( coordinates );
        ResolutionRequest request = new ResolutionRequest( artifact );
        Path artifactPath = getResource( resource );
        ResolutionResult result = new ResolutionResultMock( artifactPath );
        expect( resolver.resolve( request ) ).andReturn( result );
    }

    public void expectArtifact( String coordinates, String resource )
    {
        Artifact artifact = new DefaultArtifact( coordinates );
        artifact = artifact.setPath( getResource( resource ) );
        visitor.visitArtifact( artifact );
        expectLastCall();
    }

    private void performTest( String module )
        throws Exception
    {
        replay( resolver, deployer, visitor );

        ResolveReport report = ivy.resolve( getResource( module + ".ivy" ).toFile() );

        for ( ArtifactDownloadReport artifactReport : report.getAllArtifactsReports() )
        {
            Artifact artifact = IvyResolver.ivy2aether( artifactReport.getArtifact() );
            Path artifactPath = artifactReport.getLocalFile().toPath().toAbsolutePath();
            artifact = artifact.setPath( artifactPath );
            visitor.visitArtifact( artifact );
        }

        verify( resolver, deployer, visitor );
    }

    // Reproducer for rhbz#1127804
    @Test
    public void testArtifactClassifier()
        throws Exception
    {
        addArtifact( "org.apache.hadoop:hadoop-hdfs:pom:2.4.1", "hadoop-hdfs-2.4.1.pom" );
        addArtifact( "org.apache.hadoop:hadoop-hdfs:2.4.1", "hdfs.jar" );
        addArtifact( "org.apache.hadoop:hadoop-hdfs::sources:2.4.1", null );
        addArtifact( "org.apache.hadoop:hadoop-hdfs::src:2.4.1", null );
        addArtifact( "org.apache.hadoop:hadoop-hdfs::javadoc:2.4.1", null );
        addArtifact( "org.apache.hadoop:hadoop-hdfs", "hdfs.jar" );
        addArtifact( "org.apache.hadoop:hadoop-hdfs::tests:", "hdfs-tests.jar" );
        expectArtifact( "org.apache.hadoop:hadoop-hdfs", "hdfs.jar" );
        expectArtifact( "org.apache.hadoop:hadoop-hdfs::tests:", "hdfs-tests.jar" );
        performTest( "bz1127804" );
    }

    // Reproducer for rhbz#1383583
    @Test
    public void testPomConversion()
        throws Exception
    {
        addArtifact( "foo:parent:pom:1.0.0", "parent.pom" );
        addArtifact( "foo:parent:1.0.0", null );
        addArtifact( "foo:child:pom:1.0.0", "child.pom" );
        addArtifact( "foo:child:1.0.0", "empty.jar" );
        addArtifact( "foo:child::sources:1.0.0", null );
        addArtifact( "foo:child::src:1.0.0", null );
        addArtifact( "foo:child::javadoc:1.0.0", null );
        addArtifact( "foo:child", "empty.jar" );
        expectArtifact( "foo:child", "empty.jar" );
        performTest( "bz1383583" );
    }
}
