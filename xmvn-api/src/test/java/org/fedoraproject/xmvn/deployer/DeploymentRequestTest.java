/*-
 * Copyright (c) 2016-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.deployer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Roman Vais
 */

@RunWith( EasyMockRunner.class )
public class DeploymentRequestTest
{
    private ArrayList<Artifact> depExcl;

    private Artifact artifact, dependencyA, dependencyB, dependencyC, dependencyD;

    @TestSubject
    private final DeploymentRequest deployRq = new DeploymentRequest();

    @Before
    public void setUp()
    {
        artifact = EasyMock.createMock( Artifact.class );
        dependencyA = EasyMock.createMock( Artifact.class );
        dependencyB = EasyMock.createMock( Artifact.class );
        dependencyC = EasyMock.createMock( Artifact.class );
        dependencyD = EasyMock.createMock( Artifact.class );

        EasyMock.replay( artifact, dependencyA, dependencyB, dependencyC, dependencyD );
        EasyMock.verify( artifact, dependencyA, dependencyB, dependencyC, dependencyD );

        depExcl = new ArrayList<>();
        depExcl.add( dependencyC );
        depExcl.add( dependencyD );
    }

    /**
     * Test of get and set artifact methods together with equality method
     */
    @Test
    public void getSetEqualTest()
        throws Exception
    {
        // tests setting an artifact
        deployRq.setArtifact( artifact );
        Artifact gained = deployRq.getArtifact();
        Assert.assertSame( artifact, gained );

        deployRq.setArtifact( null );
        gained = deployRq.getArtifact();
        Assert.assertSame( gained, null );

        // tests equality of empty DeploymentRequests
        DeploymentRequest extraRq = new DeploymentRequest();
        Assert.assertTrue( deployRq.equals( extraRq ) );

        // tests inequality if one of the requests doesn't have artifact
        extraRq.setArtifact( artifact );
        Assert.assertFalse( deployRq.equals( extraRq ) );

        extraRq.setArtifact( null );
        deployRq.setArtifact( artifact );
        Assert.assertFalse( deployRq.equals( extraRq ) );

        // tests if requests with the same artifact are considered equal
        extraRq.setArtifact( artifact );
        Assert.assertTrue( deployRq.equals( extraRq ) );

        // test remaining cases
        Assert.assertFalse( deployRq.equals( null ) );
        Assert.assertFalse( deployRq.equals( new Object() ) );
        Assert.assertTrue( deployRq.equals( deployRq ) );
        EasyMock.verify( artifact, dependencyA, dependencyB, dependencyC, dependencyD );
    }

    /**
     * Test of adding, getting and removing dependencies
     */
    @Test
    public void addAndGetDependenciesTest()
        throws Exception
    {
        // tests basic add and get dependency
        deployRq.setArtifact( artifact );
        deployRq.addDependency( dependencyA );
        List<DependencyDescriptor> listedDependencies;
        listedDependencies = deployRq.getDependencies();

        DependencyDescriptor dsc = listedDependencies.get( 0 );

        if ( dsc.getDependencyArtifact() != ( dependencyA ) || listedDependencies.size() != 1 )
        {
            Assert.fail( "Added dependency is not in list or there are more items." );
        }

        // tests removal of dependency
        deployRq.addDependency( dependencyB );
        deployRq.removeDependency( dependencyA );
        listedDependencies = deployRq.getDependencies();

        if ( listedDependencies.size() > 1 )
        {
            Assert.fail( "Dependency was not removed from the list." );
        }

        // tests adding dependency with exclusions
        deployRq.addDependency( dependencyB, depExcl );
        listedDependencies = deployRq.getDependencies();
        Assert.assertTrue( listedDependencies.get( 1 ).getExclusions().equals( depExcl ) );
        deployRq.removeDependency( dependencyB );

        deployRq.addDependency( dependencyB, dependencyC, dependencyD );
        listedDependencies = deployRq.getDependencies();
        Assert.assertTrue( listedDependencies.get( 1 ).getExclusions().equals( depExcl ) );
        deployRq.removeDependency( dependencyB );

        // tests adding optional dependency without exclusions
        deployRq.addDependency( dependencyB, true, new ArrayList() );
        listedDependencies = deployRq.getDependencies();
        dsc = listedDependencies.get( 1 );
        Assert.assertTrue( dsc.isOptional() );
        Assert.assertTrue( dsc.getExclusions().isEmpty() );

        EasyMock.verify( artifact, dependencyA, dependencyB, dependencyC, dependencyD );
    }

    /**
     * Test of property manipulation
     */
    @Test
    public void propertiesTest()
        throws Exception
    {
        // no properties should be present
        Assert.assertTrue( "Hash map of properties is not empty before adding first one.",
                           deployRq.getProperties().isEmpty() );

        // tests adding and getting new properties
        deployRq.addProperty( "key", null );
        Assert.assertTrue( "Property has been added even thou it's value is a null.",
                           deployRq.getProperties().isEmpty() );

        deployRq.addProperty( "key", "value" );
        Assert.assertTrue( deployRq.getProperty( "key" ).equals( "value" ) );

        // tests removing property
        deployRq.removeProperty( "key" );
        Assert.assertFalse( "Poperty was not removed.", deployRq.getProperty( "key" ) != null );

        EasyMock.verify( artifact, dependencyA, dependencyB, dependencyC, dependencyD );
    }

    /**
     * Test of get and set methods for plan path
     */
    @Test
    public void planPathTest()
        throws Exception
    {
        Path planPath = Paths.get( "/tmp/foo/bar/plan" );
        deployRq.setPlanPath( planPath );
        Assert.assertTrue( deployRq.getPlanPath().equals( planPath ) );

        EasyMock.verify( artifact, dependencyA, dependencyB, dependencyC, dependencyD );
    }
}
