/*-
 * Copyright (c) 2014-2023 Red Hat, Inc.
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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;

import org.fedoraproject.xmvn.logging.Logger;

/**
 * Test if artifacts which files are not regular files are handled properly.
 */
public abstract class AbstractInstallMojoTest
{
    private MavenProject project;

    private Artifact artifact;

    private ArtifactHandler artifactHandler;

    private Logger logger;

    protected MavenProject getProject()
    {
        return project;
    }

    protected Artifact getArtifact()
    {
        return artifact;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected abstract File getArtifactFile()
        throws Exception;

    protected void setMojoMockExpectations()
        throws Exception
    {
        expect( artifact.getGroupId() ).andReturn( "test-gid" ).anyTimes();
        expect( artifact.getArtifactId() ).andReturn( "test-aid" ).anyTimes();
        expect( artifact.getVersion() ).andReturn( "test-version" ).anyTimes();
        expect( artifact.getType() ).andReturn( "jar" ).anyTimes();
        expect( artifact.getClassifier() ).andReturn( "" ).anyTimes();
        expect( artifact.getFile() ).andReturn( getArtifactFile() ).atLeastOnce();
        expect( artifact.getArtifactHandler() ).andReturn( artifactHandler ).anyTimes();

        expect( artifactHandler.getExtension() ).andReturn( "jar" ).anyTimes();
        expect( artifactHandler.getClassifier() ).andReturn( "" ).anyTimes();

        replay( artifact, artifactHandler, logger );
    }

    protected void verifyMojoMocks()
    {
        verify( artifact, artifactHandler, logger );
    }

    @BeforeEach
    public void setUp()
        throws Exception
    {
        project = new MavenProject();
        project.setModelVersion( "4.0.0" );
        project.setGroupId( "test-gid" );
        project.setArtifactId( "test-aid" );
        project.setVersion( "test-version" );

        artifact = EasyMock.createNiceMock( Artifact.class );
        artifactHandler = EasyMock.createNiceMock( ArtifactHandler.class );
        logger = EasyMock.createNiceMock( Logger.class );
    }
}
