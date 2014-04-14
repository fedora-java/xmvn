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
package org.fedoraproject.xmvn.artifact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class DefaultArtifactTest
{
    /**
     * Test one-argument constructor.
     */
    @Test
    public void testConstructor1()
        throws Exception
    {
        Artifact artifact2 = new DefaultArtifact( "gid:aid" );
        assertEquals( "gid", artifact2.getGroupId() );
        assertEquals( "aid", artifact2.getArtifactId() );
        assertEquals( "jar", artifact2.getExtension() );
        assertEquals( "", artifact2.getClassifier() );
        assertEquals( "SYSTEM", artifact2.getVersion() );
        assertNull( artifact2.getPath() );
        assertNull( artifact2.getNamespace() );
        assertNull( artifact2.getStereotype() );

        Artifact artifact3 = new DefaultArtifact( "gid:aid:ver" );
        assertEquals( "gid", artifact3.getGroupId() );
        assertEquals( "aid", artifact3.getArtifactId() );
        assertEquals( "jar", artifact3.getExtension() );
        assertEquals( "", artifact3.getClassifier() );
        assertEquals( "ver", artifact3.getVersion() );
        assertNull( artifact3.getPath() );
        assertNull( artifact3.getNamespace() );
        assertNull( artifact3.getStereotype() );

        Artifact artifact4 = new DefaultArtifact( "gid:aid:ext:ver" );
        assertEquals( "gid", artifact4.getGroupId() );
        assertEquals( "aid", artifact4.getArtifactId() );
        assertEquals( "ext", artifact4.getExtension() );
        assertEquals( "", artifact4.getClassifier() );
        assertEquals( "ver", artifact4.getVersion() );
        assertNull( artifact4.getPath() );
        assertNull( artifact4.getNamespace() );
        assertNull( artifact4.getStereotype() );

        Artifact artifact5 = new DefaultArtifact( "gid:aid:ext:cla:ver" );
        assertEquals( "gid", artifact5.getGroupId() );
        assertEquals( "aid", artifact5.getArtifactId() );
        assertEquals( "ext", artifact5.getExtension() );
        assertEquals( "cla", artifact5.getClassifier() );
        assertEquals( "ver", artifact5.getVersion() );
        assertNull( artifact5.getPath() );
        assertNull( artifact5.getNamespace() );
        assertNull( artifact5.getStereotype() );
    }

    /**
     * Test one-argument constructor with invalid coordinates.
     */
    @Test( expected = IllegalArgumentException.class )
    public void testInvalidCoordinates()
        throws Exception
    {
        new DefaultArtifact( "foo" );
    }

    /**
     * Test one-argument constructor with too many fields in coordinates.
     */
    @Test( expected = IllegalArgumentException.class )
    public void testTooManyFields()
        throws Exception
    {
        new DefaultArtifact( "gid:aid:ext:cla:ver:extra" );
    }

    /**
     * Test two-argument constructor.
     */
    @Test
    public void testConstructor2()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "gid", "aid" );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "jar", artifact.getExtension() );
        assertEquals( "", artifact.getClassifier() );
        assertEquals( "SYSTEM", artifact.getVersion() );
        assertNull( artifact.getPath() );
        assertNull( artifact.getNamespace() );
        assertNull( artifact.getStereotype() );
    }

    /**
     * Test three-argument constructor.
     */
    @Test
    public void testConstructor3()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "gid", "aid", "ver" );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "jar", artifact.getExtension() );
        assertEquals( "", artifact.getClassifier() );
        assertEquals( "ver", artifact.getVersion() );
        assertNull( artifact.getPath() );
        assertNull( artifact.getNamespace() );
        assertNull( artifact.getStereotype() );
    }

    /**
     * Test four-argument constructor.
     */
    @Test
    public void testConstructor4()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "ver" );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "ext", artifact.getExtension() );
        assertEquals( "", artifact.getClassifier() );
        assertEquals( "ver", artifact.getVersion() );
        assertNull( artifact.getPath() );
        assertNull( artifact.getNamespace() );
        assertNull( artifact.getStereotype() );
    }

    /**
     * Test five-argument constructor.
     */
    @Test
    public void testConstructor5()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", "ver" );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "ext", artifact.getExtension() );
        assertEquals( "cla", artifact.getClassifier() );
        assertEquals( "ver", artifact.getVersion() );
        assertNull( artifact.getPath() );
        assertNull( artifact.getNamespace() );
        assertNull( artifact.getStereotype() );
    }

    /**
     * Test if string conversion produces expected coordinates.
     */
    @Test
    public void testToString()
        throws Exception
    {
        Artifact artifact2 = new DefaultArtifact( "gid", "aid" );
        assertEquals( "gid:aid:jar:SYSTEM", artifact2.toString() );

        Artifact artifact3 = new DefaultArtifact( "gid", "aid", "ver" );
        assertEquals( "gid:aid:jar:ver", artifact3.toString() );

        Artifact artifact4 = new DefaultArtifact( "gid", "aid", "ext", "ver" );
        assertEquals( "gid:aid:ext:ver", artifact4.toString() );

        Artifact artifact5 = new DefaultArtifact( "gid", "aid", "ext", "cla", "ver" );
        assertEquals( "gid:aid:ext:cla:ver", artifact5.toString() );
    }

    /**
     * Test if equality behaves sanely.
     */
    @Test
    public void testEquals()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "gid", "aid", "ext", "cla", "ver" );

        assertTrue( artifact.equals( artifact ) );
        assertFalse( artifact.equals( null ) );
        assertFalse( artifact.equals( 42 ) );

        Artifact artifact0 = new DefaultArtifact( "gid:aid:ext:cla:ver" );

        assertTrue( artifact.equals( artifact0 ) );

        Artifact artifact1 = new DefaultArtifact( "gidX", "aid", "ext", "cla", "ver" );
        Artifact artifact2 = new DefaultArtifact( "gid", "aidX", "ext", "cla", "ver" );
        Artifact artifact3 = new DefaultArtifact( "gid", "aid", "extX", "cla", "ver" );
        Artifact artifact4 = new DefaultArtifact( "gid", "aid", "ext", "claX", "ver" );
        Artifact artifact5 = new DefaultArtifact( "gid", "aid", "ext", "cla", "verX" );

        assertFalse( artifact.equals( artifact1 ) );
        assertFalse( artifact.equals( artifact2 ) );
        assertFalse( artifact.equals( artifact3 ) );
        assertFalse( artifact.equals( artifact4 ) );
        assertFalse( artifact.equals( artifact5 ) );
    }
}
