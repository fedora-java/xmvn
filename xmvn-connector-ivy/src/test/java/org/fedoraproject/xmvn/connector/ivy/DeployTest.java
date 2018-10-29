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
package org.fedoraproject.xmvn.connector.ivy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;

/**
 * @author Mikolaj Izdebski
 * @author Roman Vais
 */

@RunWith( EasyMockRunner.class )
public class DeployTest
{
    private DeploymentResult deployed, deployFail;

    private File fileValid, fileBroken;

    private Artifact artifact;

    private ModuleRevisionId mri;

    @TestSubject
    private final IvyResolver ivyResolver = new IvyResolver();

    @Mock( type = MockType.STRICT )
    private Deployer deployer;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setUp()
        throws Exception
    {
        // deployment results
        deployed = new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return null;
            }
        };

        deployFail = new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return new Exception( "Tested Exception" );
            }
        };

        // prepare all the resources needed for testing; ivy itself
        ivyResolver.setSettings( new IvySettings() );

        // artifact xml files
        fileValid = new File( tempDir.getRoot(), "bz1127804.ivy" );
        // copr ivy.xml file to temp dir, so that pom file doesn't get created in resources dir
        Files.copy( Paths.get( "src/test/resources/bz1127804.ivy" ), fileValid.toPath() );
        fileBroken = new File( "/tmp/foo/bar/foobar" );

        artifact = EasyMock.createMock( Artifact.class );
        mri = EasyMock.createMock( ModuleRevisionId.class );
    }

    /**
     * Test of basic functionality publish and deploy methods
     */
    @Test
    public void publishDeployTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "artifact" );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployed );
        EasyMock.replay( artifact, mri, deployer );

        // run the basic functionality of publish method
        ivyResolver.publish( artifact, fileValid, true );
        EasyMock.verify( artifact, mri, deployer );
    }

    /**
     * Test of deploy method with use of publish method call and valid xml file, ivy type
     */
    @Test
    public void publishDeployIvyTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "artifact" );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployed );
        EasyMock.replay( artifact, mri, deployer );

        // run the basic functionality of publish method
        ivyResolver.publish( artifact, fileValid, true );
        EasyMock.verify( artifact, mri, deployer );
    }

    /**
     * Test of deploy method with use of publish method call and valid xml file, xml extension
     */
    @Test
    public void publishDeployXmlTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "artifact" );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployed );
        EasyMock.replay( artifact, mri, deployer );

        // run the basic functionality of publish method
        ivyResolver.publish( artifact, fileValid, true );
        EasyMock.verify( artifact, mri, deployer );
    }

    /**
     * Test for IOException thrown by deploy method with use of publish method call
     */
    @Test
    public void publishDeployExceptionTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "artifact" );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployFail );
        EasyMock.replay( artifact, mri, deployer );

        // run the basic functionality of publish method
        try
        {
            ivyResolver.publish( artifact, fileBroken, true );
            Assert.fail();
        }
        catch ( Exception e )
        {
        }
        EasyMock.verify( artifact, mri, deployer );
    }

    /**
     * Test of deployEffectivePom method with use of publish method call and valid xml file
     */
    @Test
    public void publishDeployEffectivePomTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "artifact" ).atLeastOnce();
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" ).atLeastOnce();
        EasyMock.expect( mri.getName() ).andReturn( "FooRevision" );
        EasyMock.expect( mri.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" ).atLeastOnce();

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployed ).times( 2 );
        EasyMock.replay( artifact, mri, deployer );

        // run the test of the function
        ivyResolver.publish( artifact, fileValid, true );
        EasyMock.verify( artifact, mri, deployer );
    }

    /**
     * Test for IOException thrown by deployEffectivePom method with use of publish method call if given file doesn't
     * exist
     */
    @Test
    public void publishDeployEffectivePomExeptionTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();
        EasyMock.replay( artifact, deployer );

        // run the test
        try
        {
            ivyResolver.publish( artifact, fileBroken, true );
            Assert.fail();
        }
        catch ( IOException e )
        {
        }
        EasyMock.verify( artifact, deployer );
    }

    /**
     * Test for IOException thrown by deployEffectivePom method with use of publish method call caused by deployment
     * failure
     */
    @Test
    public void publishDeployEffectivePomFailTest()
        throws Exception
    {
        // prepare arguments for tested function
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();

        EasyMock.expect( mri.getOrganisation() ).andReturn( "organization" ).atLeastOnce();
        EasyMock.expect( mri.getName() ).andReturn( "FooRevision" );
        EasyMock.expect( mri.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" ).atLeastOnce();

        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( deployFail );
        EasyMock.replay( artifact, mri, deployer );
        // run the test
        try
        {
            ivyResolver.publish( artifact, fileValid, true );
            Assert.fail();
        }
        catch ( IOException e )
        {
        }
        EasyMock.verify( artifact, deployer );
    }
}
