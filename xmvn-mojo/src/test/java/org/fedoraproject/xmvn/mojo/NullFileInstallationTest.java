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

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;

/**
 * Test if non-POM modules which have no artifact file are handled properly.
 * 
 * @author Mikolaj Izdebski
 */
@RunWith( EasyMockRunner.class )
public class NullFileInstallationTest
    extends AbstractInstallMojoTest
{
    boolean deployed;

    @Mock( type = MockType.NICE )
    DeploymentResult result;

    Path pomPath = Paths.get( "/some/non/existent.path" );

    class DeployerMock
        implements Deployer
    {
        @Override
        public DeploymentResult deploy( DeploymentRequest request )
        {
            assertFalse( deployed );
            deployed = true;

            Artifact artifact = request.getArtifact();
            assertNotNull( artifact );
            assertEquals( "pom", artifact.getExtension() );
            assertEquals( pomPath, artifact.getPath() );
            return result;
        }
    }

    @Override
    protected File getArtifactFile()
        throws Exception
    {
        return null;
    }

    @Test
    public void testNullFileInstallation()
        throws Exception
    {
        setMojoMockExpectations();
        replay( result );

        getProject().setArtifact( getArtifact() );
        getProject().setFile( pomPath.toFile() );

        InstallMojo mojo = new InstallMojo( new DeployerMock(), getLogger() );
        mojo.setReactorProjects( Collections.singletonList( getProject() ) );
        deployed = false;
        mojo.execute();
        assertTrue( deployed );

        verify( result );
        verifyMojoMocks();
    }
}
