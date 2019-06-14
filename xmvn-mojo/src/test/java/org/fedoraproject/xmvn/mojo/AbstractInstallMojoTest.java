/*-
 * Copyright (c) 2014-2019 Red Hat, Inc.
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
import org.codehaus.plexus.logging.Logger;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;

/**
 * Test if artifacts which files are not regular files are handled properly.
 */
public abstract class AbstractInstallMojoTest
{
    private MavenProject project;

    @Mock( type = MockType.NICE )
    private Artifact artifact;

    @Mock( type = MockType.NICE )
    private ArtifactHandler artifactHandler;

    @Mock( type = MockType.NICE )
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

    @Before
    public void setUp()
        throws Exception
    {
        project = new MavenProject();
        project.setModelVersion( "4.0.0" );
        project.setGroupId( "test-gid" );
        project.setArtifactId( "test-aid" );
        project.setVersion( "test-version" );
    }
}
