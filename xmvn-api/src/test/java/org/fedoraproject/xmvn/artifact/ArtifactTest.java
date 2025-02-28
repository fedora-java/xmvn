/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactTest {
    /** Test one-argument constructor. */
    @Test
    public void testConstructor1() throws Exception {
        Artifact artifact2 = Artifact.of("gid:aid");
        assertEquals("gid", artifact2.getGroupId());
        assertEquals("aid", artifact2.getArtifactId());
        assertEquals("jar", artifact2.getExtension());
        assertEquals("", artifact2.getClassifier());
        assertEquals("SYSTEM", artifact2.getVersion());
        assertNull(artifact2.getPath());

        Artifact artifact3 = Artifact.of("gid:aid:ver");
        assertEquals("gid", artifact3.getGroupId());
        assertEquals("aid", artifact3.getArtifactId());
        assertEquals("jar", artifact3.getExtension());
        assertEquals("", artifact3.getClassifier());
        assertEquals("ver", artifact3.getVersion());
        assertNull(artifact3.getPath());

        Artifact artifact4 = Artifact.of("gid:aid:ext:ver");
        assertEquals("gid", artifact4.getGroupId());
        assertEquals("aid", artifact4.getArtifactId());
        assertEquals("ext", artifact4.getExtension());
        assertEquals("", artifact4.getClassifier());
        assertEquals("ver", artifact4.getVersion());
        assertNull(artifact4.getPath());

        Artifact artifact5 = Artifact.of("gid:aid:ext:cla:ver");
        assertEquals("gid", artifact5.getGroupId());
        assertEquals("aid", artifact5.getArtifactId());
        assertEquals("ext", artifact5.getExtension());
        assertEquals("cla", artifact5.getClassifier());
        assertEquals("ver", artifact5.getVersion());
        assertNull(artifact5.getPath());

        // Empty extension
        Artifact artifact6 = Artifact.of("gid:aid::cla:ver");
        assertEquals("gid", artifact6.getGroupId());
        assertEquals("aid", artifact6.getArtifactId());
        assertEquals("jar", artifact6.getExtension());
        assertEquals("cla", artifact6.getClassifier());
        assertEquals("ver", artifact6.getVersion());
        assertNull(artifact6.getPath());

        // Empty version
        Artifact artifact7 = Artifact.of("gid:aid:ext:cla:");
        assertEquals("gid", artifact7.getGroupId());
        assertEquals("aid", artifact7.getArtifactId());
        assertEquals("ext", artifact7.getExtension());
        assertEquals("cla", artifact7.getClassifier());
        assertEquals("SYSTEM", artifact7.getVersion());
        assertNull(artifact7.getPath());
    }

    /** Test one-argument constructor with invalid coordinates. */
    @Test
    public void testInvalidCoordinates() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> Artifact.of("foo"));
    }

    /** Test one-argument constructor with too many fields in coordinates. */
    @Test
    public void testTooManyFields() throws Exception {
        assertThrows(
                IllegalArgumentException.class, () -> Artifact.of("gid:aid:ext:cla:ver:extra"));
    }

    /** Test two-argument constructor with groupId as null pointer. */
    @Test
    public void testGroupIdNull() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> Artifact.of(null, ""));
    }

    /** Test two-argument constructor with artifactId as null pointer. */
    @Test
    public void testArtifactIdNull() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> Artifact.of("gid", null));
    }

    /** Test two-argument constructor with groupId as null pointer. */
    @Test
    public void testGroupIdEmpty() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> Artifact.of("", "aid"));
    }

    /** Test two-argument constructor with artifactId as null pointer. */
    @Test
    public void testArtifactIdEmpty() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> Artifact.of("gid", ""));
    }

    /** Test two-argument constructor. */
    @Test
    public void testConstructor2() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid");
        assertEquals("gid", artifact.getGroupId());
        assertEquals("aid", artifact.getArtifactId());
        assertEquals("jar", artifact.getExtension());
        assertEquals("", artifact.getClassifier());
        assertEquals("SYSTEM", artifact.getVersion());
        assertNull(artifact.getPath());
    }

    /** Test three-argument constructor. */
    @Test
    public void testConstructor3() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ver");
        assertEquals("gid", artifact.getGroupId());
        assertEquals("aid", artifact.getArtifactId());
        assertEquals("jar", artifact.getExtension());
        assertEquals("", artifact.getClassifier());
        assertEquals("ver", artifact.getVersion());
        assertNull(artifact.getPath());
    }

    /** Test four-argument constructor. */
    @Test
    public void testConstructor4() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "ver");
        assertEquals("gid", artifact.getGroupId());
        assertEquals("aid", artifact.getArtifactId());
        assertEquals("ext", artifact.getExtension());
        assertEquals("", artifact.getClassifier());
        assertEquals("ver", artifact.getVersion());
        assertNull(artifact.getPath());
    }

    /** Test five-argument constructor. */
    @Test
    public void testConstructor5() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertEquals("gid", artifact.getGroupId());
        assertEquals("aid", artifact.getArtifactId());
        assertEquals("ext", artifact.getExtension());
        assertEquals("cla", artifact.getClassifier());
        assertEquals("ver", artifact.getVersion());
        assertNull(artifact.getPath());

        Artifact artifact1 = Artifact.of("gid", "aid", "", "cla", "ver");
        assertEquals("gid", artifact1.getGroupId());
        assertEquals("aid", artifact1.getArtifactId());
        assertEquals("jar", artifact1.getExtension());
        assertEquals("cla", artifact1.getClassifier());
        assertEquals("ver", artifact1.getVersion());
        assertNull(artifact1.getPath());

        Artifact artifact2 = Artifact.of("gid", "aid", "ext", "cla", "");
        assertEquals("gid", artifact2.getGroupId());
        assertEquals("aid", artifact2.getArtifactId());
        assertEquals("ext", artifact2.getExtension());
        assertEquals("cla", artifact2.getClassifier());
        assertEquals("SYSTEM", artifact2.getVersion());
        assertNull(artifact2.getPath());
    }

    @Test
    public void testSetVersion() throws Exception {
        Artifact artifact = Artifact.of("gid:aid:ext:cla:ver");
        Artifact newArtifact = artifact.setVersion("1.2.3");
        assertNotSame(artifact, newArtifact);
        assertEquals("1.2.3", newArtifact.getVersion());
        assertEquals("ver", artifact.getVersion());
    }

    @Test
    public void testSetPath() throws Exception {
        Artifact artifact = Artifact.of("gid:aid:ext:cla:ver");
        Artifact newArtifact = artifact.withPath(Path.of("/tmp/foo"));
        assertNotSame(artifact, newArtifact);
        assertEquals(Path.of("/tmp/foo"), newArtifact.getPath());
        assertNull(artifact.getPath());
    }

    /** Test if string conversion produces expected coordinates. */
    @Test
    public void testToString() throws Exception {
        Artifact artifact2 = Artifact.of("gid", "aid");
        assertEquals("gid:aid:jar:SYSTEM", artifact2.toString());

        Artifact artifact3 = Artifact.of("gid", "aid", "ver");
        assertEquals("gid:aid:jar:ver", artifact3.toString());

        Artifact artifact4 = Artifact.of("gid", "aid", "ext", "ver");
        assertEquals("gid:aid:ext:ver", artifact4.toString());

        Artifact artifact5 = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertEquals("gid:aid:ext:cla:ver", artifact5.toString());
    }

    /** Test if equality behaves sanely. */
    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void testEquals() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "ver");
        Path path = Path.of("/some/path");

        assertTrue(artifact.equals(artifact));
        assertFalse(artifact.equals(null));
        assertFalse(artifact.equals(42));

        Artifact artifact0 = Artifact.of("gid:aid:ext:cla:ver");

        assertTrue(artifact.equals(artifact0));
        assertTrue(artifact.withPath(path).equals(artifact0.withPath(path)));
        assertFalse(artifact.withPath(path).equals(artifact0));

        Artifact artifact1 = Artifact.of("gidX", "aid", "ext", "cla", "ver");
        Artifact artifact2 = Artifact.of("gid", "aidX", "ext", "cla", "ver");
        Artifact artifact3 = Artifact.of("gid", "aid", "extX", "cla", "ver");
        Artifact artifact4 = Artifact.of("gid", "aid", "ext", "claX", "ver");
        Artifact artifact5 = Artifact.of("gid", "aid", "ext", "cla", "verX");
        Artifact artifact6 = Artifact.of("gid", "aid", "ext", "cla", "ver").withPath(path);

        assertFalse(artifact.equals(artifact1));
        assertFalse(artifact.equals(artifact2));
        assertFalse(artifact.equals(artifact3));
        assertFalse(artifact.equals(artifact4));
        assertFalse(artifact.equals(artifact5));
        assertFalse(artifact.equals(artifact6));
    }

    @Test
    public void testHashCode() throws Exception {
        Artifact artifact0 = Artifact.of("gid:aid:ext:cla:ver");
        Artifact artifact1 = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertEquals(artifact0.hashCode(), artifact1.hashCode());
    }
}
