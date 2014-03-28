/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;

/**
 * Test if artifacts which files are not regular files are handled properly.
 * 
 * @author Mikolaj Izdebski
 */
@RunWith( EasyMockRunner.class )
public class DirectoryInstallationTest
{
    @Mock( type = MockType.NICE )
    private Artifact projectArtifact;

    @Mock( type = MockType.NICE )
    private ArtifactHandler artifactHandler;

    @Mock( type = MockType.STRICT )
    private Deployer deployer;

    @Mock( type = MockType.NICE )
    private DeploymentResult deploymentResult;

    private MavenProject projectStub;

    @Before
    public void setUp()
        throws Exception
    {
        projectStub = new MavenProject();
        projectStub.setModelVersion( "4.0.0" );
        projectStub.setGroupId( "test-gid" );
        projectStub.setArtifactId( "test-aid" );
        projectStub.setVersion( "test-version" );
    }

    @Test
    public void testDirectoryAsProjectFile()
        throws Exception
    {
        Path emptyDirectory = Files.createTempDirectory( "xmvn-test" );

        expect( projectArtifact.getGroupId() ).andReturn( "test-gid" ).anyTimes();
        expect( projectArtifact.getArtifactId() ).andReturn( "test-aid" ).anyTimes();
        expect( projectArtifact.getVersion() ).andReturn( "test-version" ).anyTimes();
        expect( projectArtifact.getType() ).andReturn( "jar" ).anyTimes();
        expect( projectArtifact.getClassifier() ).andReturn( "" ).anyTimes();
        expect( projectArtifact.getFile() ).andReturn( emptyDirectory.toFile() ).atLeastOnce();
        expect( projectArtifact.getArtifactHandler() ).andReturn( artifactHandler ).anyTimes();

        expect( artifactHandler.getExtension() ).andReturn( "jar" ).anyTimes();
        expect( artifactHandler.getClassifier() ).andReturn( "" ).anyTimes();

        // Expect deployment of POM file only
        expect( deployer.deploy( isA( DeploymentRequest.class ) ) ).andReturn( deploymentResult );

        replay( projectArtifact, artifactHandler, deployer, deploymentResult );

        projectStub.setArtifact( projectArtifact );

        InstallMojo mojo = new InstallMojo( deployer );
        mojo.setReactorProjects( Collections.singletonList( projectStub ) );
        mojo.execute();

        verify( projectArtifact, artifactHandler, deployer, deploymentResult );
    }
}
