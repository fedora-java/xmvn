package org.fedoraproject.xmvn.connector.ivy;

import java.io.File;
import java.io.IOException;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( EasyMockRunner.class )
public class DeployTest
{
    @TestSubject
    private final IvyResolver ivyResolver = new IvyResolver();

    @Mock( type = MockType.NICE )
    private Deployer deployer;

    @Before
    public void setUp()
    {
        ivyResolver.setSettings( new IvySettings() );
    }

    @Test
    public void testName()
        throws Exception
    {
        ModuleRevisionId mri = EasyMock.createMock( ModuleRevisionId.class );
        EasyMock.expect( mri.getOrganisation() ).andReturn( "foo" );
        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getExt() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "foo" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return null;
            }
        } );
        EasyMock.replay( artifact, mri, deployer );
        ivyResolver.publish( artifact, new File( "/tmp/foo" ), true );
    }

    @Test
    public void test3()
        throws Exception
    {
        ModuleRevisionId mri = EasyMock.createMock( ModuleRevisionId.class );
        EasyMock.expect( mri.getOrganisation() ).andReturn( "foo" );
        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getExt() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "foo" );
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" );
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri );
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return new Exception();
            }
        } );
        EasyMock.replay( artifact, mri, deployer );
        try
        {
            ivyResolver.publish( artifact, new File( "/tmp/foo" ), true );
            Assert.fail();
        }
        catch ( Exception e )
        {

        }
    }

    @Test
    public void test5()
        throws Exception
    {
        ModuleRevisionId mri = EasyMock.createMock( ModuleRevisionId.class );
        EasyMock.expect( mri.getOrganisation() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getName() ).andReturn( "aoeu" );
        EasyMock.expect( mri.getExtraAttribute( "classifier" ) ).andReturn( null );
        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" ).atLeastOnce();
        EasyMock.expect( artifact.getType() ).andReturn( "jar" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return null;
            }
        } ).atLeastOnce();
        EasyMock.replay( artifact, mri, deployer );
        ivyResolver.publish( artifact, new File( "src/test/resources/bz1127804.ivy" ), true );
    }

    @Test
    public void test2()
        throws Exception
    {
        ModuleRevisionId mri = EasyMock.createMock( ModuleRevisionId.class );
        EasyMock.expect( mri.getOrganisation() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getName() ).andReturn( "aoeu" );
        EasyMock.expect( mri.getExtraAttribute( "classifier" ) ).andReturn( null );
        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" ).atLeastOnce();
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return null;
            }
        } ).atLeastOnce();
        EasyMock.replay( artifact, mri, deployer );
        ivyResolver.publish( artifact, new File( "src/test/resources/bz1127804.ivy" ), true );
    }

    @Test
    public void test2e()
        throws Exception
    {
        ModuleRevisionId mri = EasyMock.createMock( ModuleRevisionId.class );
        EasyMock.expect( mri.getOrganisation() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getName() ).andReturn( "aoeu" );
        EasyMock.expect( mri.getExtraAttribute( "classifier" ) ).andReturn( null );
        Artifact artifact = EasyMock.createMock( Artifact.class );
        EasyMock.expect( artifact.getExt() ).andReturn( "xml" ).atLeastOnce();
        EasyMock.expect( artifact.getName() ).andReturn( "foo" ).atLeastOnce();
        EasyMock.expect( mri.getRevision() ).andReturn( "1.2.3" ).atLeastOnce();
        EasyMock.expect( artifact.getType() ).andReturn( "ivy" ).atLeastOnce();
        EasyMock.expect( artifact.getModuleRevisionId() ).andReturn( mri ).atLeastOnce();
        EasyMock.expect( artifact.getExtraAttribute( "classifier" ) ).andReturn( null );
        EasyMock.expect( deployer.deploy( EasyMock.isA( DeploymentRequest.class ) ) ).andReturn( new DeploymentResult()
        {
            @Override
            public Exception getException()
            {
                return null;
            }
        } ).atLeastOnce();
        EasyMock.replay( artifact, mri, deployer );
        try
        {
            ivyResolver.publish( artifact, new File( "src/test/resources/foo.ivy" ), true );
            Assert.fail();
        }
        catch ( IOException e )
        {

        }
    }
}
