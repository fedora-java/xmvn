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
package org.fedoraproject.xmvn.resolver;

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
public class ResolutionRequestTest
{
    private Artifact artifact;

    @TestSubject
    private final ResolutionRequest rrq = new ResolutionRequest();

    @Before
    public void setUp()
    {
        artifact = EasyMock.createMock( Artifact.class );
        rrq.setArtifact( null );
        rrq.setProviderNeeded( false );
        rrq.setPersistentFileNeeded( false );
    }

    /**
     * Test of get and set methods
     */
    @Test
    public void getSetTest()
        throws Exception
    {
        // tests set and get artifact
        rrq.setArtifact( artifact );
        Assert.assertTrue( artifact == rrq.getArtifact() );

        // tests set and get 'ProviderNeeded'
        rrq.setProviderNeeded( true );
        Assert.assertTrue( rrq.isProviderNeeded() );
        rrq.setProviderNeeded( false );
        Assert.assertFalse( rrq.isProviderNeeded() );

        // tests set and get 'PersistentFileNeeded'
        rrq.setPersistentFileNeeded( true );
        Assert.assertTrue( rrq.isPersistentFileNeeded() );
        rrq.setPersistentFileNeeded( false );
        Assert.assertFalse( rrq.isPersistentFileNeeded() );
    }

    /**
     * Test of constructor wit artifact argument
     */
    @Test
    public void extraConstructorTest()
        throws Exception
    {
        ResolutionRequest extraRq = new ResolutionRequest( artifact );
        Assert.assertTrue( artifact == extraRq.getArtifact() );
    }

    /**
     * Test of equality method
     */
    @Test
    public void equalityTest()
        throws Exception
    {

        Assert.assertTrue( rrq.equals( rrq ) );
        Assert.assertFalse( rrq.equals( null ) );
        Assert.assertFalse( rrq.equals( new Object() ) );

        ResolutionRequest extraRq = new ResolutionRequest();
        Assert.assertTrue( rrq.equals( extraRq ) );

        extraRq.setArtifact( artifact );
        Assert.assertFalse( rrq.equals( extraRq ) );

        rrq.setArtifact( artifact );
        Assert.assertTrue( rrq.equals( extraRq ) );

        rrq.setProviderNeeded( true );
        Assert.assertFalse( rrq.equals( extraRq ) );

        rrq.setProviderNeeded( false );
        rrq.setPersistentFileNeeded( true );
        Assert.assertFalse( rrq.equals( extraRq ) );

        extraRq.setArtifact( EasyMock.createMock( Artifact.class ) );
        Assert.assertFalse( rrq.equals( extraRq ) );

    }

}
