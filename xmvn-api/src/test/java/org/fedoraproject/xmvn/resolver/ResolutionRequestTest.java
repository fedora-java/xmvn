/*-
 * Copyright (c) 2016-2023 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Roman Vais
 */
public class ResolutionRequestTest
{
    private Artifact artifact;

    private final ResolutionRequest rrq = new ResolutionRequest();

    @BeforeEach
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
        assertTrue( artifact == rrq.getArtifact() );

        // tests set and get 'ProviderNeeded'
        rrq.setProviderNeeded( true );
        assertTrue( rrq.isProviderNeeded() );
        rrq.setProviderNeeded( false );
        assertFalse( rrq.isProviderNeeded() );

        // tests set and get 'PersistentFileNeeded'
        rrq.setPersistentFileNeeded( true );
        assertTrue( rrq.isPersistentFileNeeded() );
        rrq.setPersistentFileNeeded( false );
        assertFalse( rrq.isPersistentFileNeeded() );
    }

    /**
     * Test of constructor wit artifact argument
     */
    @Test
    public void extraConstructorTest()
        throws Exception
    {
        ResolutionRequest extraRq = new ResolutionRequest( artifact );
        assertTrue( artifact == extraRq.getArtifact() );
    }

    /**
     * Test of equality method
     */
    @Test
    public void equalityTest()
        throws Exception
    {
        assertTrue( rrq.equals( rrq ) );
        assertFalse( rrq.equals( null ) );
        assertFalse( rrq.equals( new Object() ) );

        ResolutionRequest extraRq = new ResolutionRequest();
        assertTrue( rrq.equals( extraRq ) );

        extraRq.setArtifact( artifact );
        assertFalse( rrq.equals( extraRq ) );

        rrq.setArtifact( artifact );
        assertTrue( rrq.equals( extraRq ) );

        rrq.setProviderNeeded( true );
        assertFalse( rrq.equals( extraRq ) );

        rrq.setProviderNeeded( false );
        rrq.setPersistentFileNeeded( true );
        assertFalse( rrq.equals( extraRq ) );

        extraRq.setArtifact( EasyMock.createMock( Artifact.class ) );
        assertFalse( rrq.equals( extraRq ) );
    }
}
