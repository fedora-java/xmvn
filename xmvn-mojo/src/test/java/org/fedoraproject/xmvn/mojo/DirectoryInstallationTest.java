/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
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
    extends AbstractInstallMojoTest
{
    @Mock( type = MockType.STRICT )
    private Deployer deployer;

    @Mock( type = MockType.NICE )
    private DeploymentResult deploymentResult;

    @Override
    protected File getArtifactFile()
        throws Exception
    {
        Path emptyDirectory = Files.createTempDirectory( "xmvn-test" );
        return emptyDirectory.toFile();
    }

    @Test
    public void testDirectoryAsProjectFile()
        throws Exception
    {
        setMojoMockExpectations();

        // Expect deployment of POM file only
        expect( deployer.deploy( isA( DeploymentRequest.class ) ) ).andReturn( deploymentResult );

        replay( deployer, deploymentResult );

        getProject().setArtifact( getArtifact() );

        InstallMojo mojo = new InstallMojo( deployer, getLogger() );
        mojo.setReactorProjects( Collections.singletonList( getProject() ) );
        mojo.execute();

        verify( deployer, deploymentResult );
        verifyMojoMocks();
    }
}
