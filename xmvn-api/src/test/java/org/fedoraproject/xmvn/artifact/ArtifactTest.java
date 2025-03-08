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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactTest {
    /** Test one-argument constructor. */
    @Test
    void constructor1() throws Exception {
        Artifact artifact2 = Artifact.of("gid:aid");
        assertThat(artifact2.getGroupId()).isEqualTo("gid");
        assertThat(artifact2.getArtifactId()).isEqualTo("aid");
        assertThat(artifact2.getExtension()).isEqualTo("jar");
        assertThat(artifact2.getClassifier()).isEqualTo("");
        assertThat(artifact2.getVersion()).isEqualTo("SYSTEM");
        assertThat(artifact2.getPath()).isNull();

        Artifact artifact3 = Artifact.of("gid:aid:ver");
        assertThat(artifact3.getGroupId()).isEqualTo("gid");
        assertThat(artifact3.getArtifactId()).isEqualTo("aid");
        assertThat(artifact3.getExtension()).isEqualTo("jar");
        assertThat(artifact3.getClassifier()).isEqualTo("");
        assertThat(artifact3.getVersion()).isEqualTo("ver");
        assertThat(artifact3.getPath()).isNull();

        Artifact artifact4 = Artifact.of("gid:aid:ext:ver");
        assertThat(artifact4.getGroupId()).isEqualTo("gid");
        assertThat(artifact4.getArtifactId()).isEqualTo("aid");
        assertThat(artifact4.getExtension()).isEqualTo("ext");
        assertThat(artifact4.getClassifier()).isEqualTo("");
        assertThat(artifact4.getVersion()).isEqualTo("ver");
        assertThat(artifact4.getPath()).isNull();

        Artifact artifact5 = Artifact.of("gid:aid:ext:cla:ver");
        assertThat(artifact5.getGroupId()).isEqualTo("gid");
        assertThat(artifact5.getArtifactId()).isEqualTo("aid");
        assertThat(artifact5.getExtension()).isEqualTo("ext");
        assertThat(artifact5.getClassifier()).isEqualTo("cla");
        assertThat(artifact5.getVersion()).isEqualTo("ver");
        assertThat(artifact5.getPath()).isNull();

        // Empty extension
        Artifact artifact6 = Artifact.of("gid:aid::cla:ver");
        assertThat(artifact6.getGroupId()).isEqualTo("gid");
        assertThat(artifact6.getArtifactId()).isEqualTo("aid");
        assertThat(artifact6.getExtension()).isEqualTo("jar");
        assertThat(artifact6.getClassifier()).isEqualTo("cla");
        assertThat(artifact6.getVersion()).isEqualTo("ver");
        assertThat(artifact6.getPath()).isNull();

        // Empty version
        Artifact artifact7 = Artifact.of("gid:aid:ext:cla:");
        assertThat(artifact7.getGroupId()).isEqualTo("gid");
        assertThat(artifact7.getArtifactId()).isEqualTo("aid");
        assertThat(artifact7.getExtension()).isEqualTo("ext");
        assertThat(artifact7.getClassifier()).isEqualTo("cla");
        assertThat(artifact7.getVersion()).isEqualTo("SYSTEM");
        assertThat(artifact7.getPath()).isNull();
    }

    /** Test one-argument constructor with invalid coordinates. */
    @Test
    void invalidCoordinates() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of("foo"));
    }

    /** Test one-argument constructor with too many fields in coordinates. */
    @Test
    void tooManyFields() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of("gid:aid:ext:cla:ver:extra"));
    }

    /** Test two-argument constructor with groupId as null pointer. */
    @Test
    void groupIdNull() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of(null, ""));
    }

    /** Test two-argument constructor with artifactId as null pointer. */
    @Test
    void artifactIdNull() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of("gid", null));
    }

    /** Test two-argument constructor with groupId as null pointer. */
    @Test
    void groupIdEmpty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of("", "aid"));
    }

    /** Test two-argument constructor with artifactId as null pointer. */
    @Test
    void artifactIdEmpty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Artifact.of("gid", ""));
    }

    /** Test two-argument constructor. */
    @Test
    void constructor2() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid");
        assertThat(artifact.getGroupId()).isEqualTo("gid");
        assertThat(artifact.getArtifactId()).isEqualTo("aid");
        assertThat(artifact.getExtension()).isEqualTo("jar");
        assertThat(artifact.getClassifier()).isEqualTo("");
        assertThat(artifact.getVersion()).isEqualTo("SYSTEM");
        assertThat(artifact.getPath()).isNull();
    }

    /** Test three-argument constructor. */
    @Test
    void constructor3() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ver");
        assertThat(artifact.getGroupId()).isEqualTo("gid");
        assertThat(artifact.getArtifactId()).isEqualTo("aid");
        assertThat(artifact.getExtension()).isEqualTo("jar");
        assertThat(artifact.getClassifier()).isEqualTo("");
        assertThat(artifact.getVersion()).isEqualTo("ver");
        assertThat(artifact.getPath()).isNull();
    }

    /** Test four-argument constructor. */
    @Test
    void constructor4() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "ver");
        assertThat(artifact.getGroupId()).isEqualTo("gid");
        assertThat(artifact.getArtifactId()).isEqualTo("aid");
        assertThat(artifact.getExtension()).isEqualTo("ext");
        assertThat(artifact.getClassifier()).isEqualTo("");
        assertThat(artifact.getVersion()).isEqualTo("ver");
        assertThat(artifact.getPath()).isNull();
    }

    /** Test five-argument constructor. */
    @Test
    void constructor5() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertThat(artifact.getGroupId()).isEqualTo("gid");
        assertThat(artifact.getArtifactId()).isEqualTo("aid");
        assertThat(artifact.getExtension()).isEqualTo("ext");
        assertThat(artifact.getClassifier()).isEqualTo("cla");
        assertThat(artifact.getVersion()).isEqualTo("ver");
        assertThat(artifact.getPath()).isNull();

        Artifact artifact1 = Artifact.of("gid", "aid", "", "cla", "ver");
        assertThat(artifact1.getGroupId()).isEqualTo("gid");
        assertThat(artifact1.getArtifactId()).isEqualTo("aid");
        assertThat(artifact1.getExtension()).isEqualTo("jar");
        assertThat(artifact1.getClassifier()).isEqualTo("cla");
        assertThat(artifact1.getVersion()).isEqualTo("ver");
        assertThat(artifact1.getPath()).isNull();

        Artifact artifact2 = Artifact.of("gid", "aid", "ext", "cla", "");
        assertThat(artifact2.getGroupId()).isEqualTo("gid");
        assertThat(artifact2.getArtifactId()).isEqualTo("aid");
        assertThat(artifact2.getExtension()).isEqualTo("ext");
        assertThat(artifact2.getClassifier()).isEqualTo("cla");
        assertThat(artifact2.getVersion()).isEqualTo("SYSTEM");
        assertThat(artifact2.getPath()).isNull();
    }

    @Test
    void setVersion() throws Exception {
        Artifact artifact = Artifact.of("gid:aid:ext:cla:ver");
        Artifact newArtifact = artifact.withVersion("1.2.3");
        assertThat(artifact).isNotSameAs(newArtifact);
        assertThat(newArtifact.getVersion()).isEqualTo("1.2.3");
        assertThat(artifact.getVersion()).isEqualTo("ver");
    }

    @Test
    void setPath() throws Exception {
        Artifact artifact = Artifact.of("gid:aid:ext:cla:ver");
        Artifact newArtifact = artifact.withPath(Path.of("/tmp/foo"));
        assertThat(artifact).isNotSameAs(newArtifact);
        assertThat(newArtifact.getPath()).isEqualTo(Path.of("/tmp/foo"));
        assertThat(artifact.getPath()).isNull();
    }

    /** Test if string conversion produces expected coordinates. */
    @Test
    void testToString() throws Exception {
        Artifact artifact2 = Artifact.of("gid", "aid");
        assertThat(artifact2.toString()).isEqualTo("gid:aid:jar:SYSTEM");

        Artifact artifact3 = Artifact.of("gid", "aid", "ver");
        assertThat(artifact3.toString()).isEqualTo("gid:aid:jar:ver");

        Artifact artifact4 = Artifact.of("gid", "aid", "ext", "ver");
        assertThat(artifact4.toString()).isEqualTo("gid:aid:ext:ver");

        Artifact artifact5 = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertThat(artifact5.toString()).isEqualTo("gid:aid:ext:cla:ver");
    }

    /** Test if equality behaves sanely. */
    @Test
    void equals() throws Exception {
        Artifact artifact = Artifact.of("gid", "aid", "ext", "cla", "ver");
        Path path = Path.of("/some/path");

        assertThat(artifact).isEqualTo(artifact);
        assertThat(artifact).isNotEqualTo(null);
        assertThat(artifact).isNotEqualTo(42);

        Artifact artifact0 = Artifact.of("gid:aid:ext:cla:ver");

        assertThat(artifact0).isEqualTo(artifact);
        assertThat(artifact0.withPath(path)).isEqualTo(artifact.withPath(path));
        assertThat(artifact0).isNotEqualTo(artifact.withPath(path));

        Artifact artifact1 = Artifact.of("gidX", "aid", "ext", "cla", "ver");
        Artifact artifact2 = Artifact.of("gid", "aidX", "ext", "cla", "ver");
        Artifact artifact3 = Artifact.of("gid", "aid", "extX", "cla", "ver");
        Artifact artifact4 = Artifact.of("gid", "aid", "ext", "claX", "ver");
        Artifact artifact5 = Artifact.of("gid", "aid", "ext", "cla", "verX");
        Artifact artifact6 = Artifact.of("gid", "aid", "ext", "cla", "ver").withPath(path);

        assertThat(artifact1).isNotEqualTo(artifact);
        assertThat(artifact2).isNotEqualTo(artifact);
        assertThat(artifact3).isNotEqualTo(artifact);
        assertThat(artifact4).isNotEqualTo(artifact);
        assertThat(artifact5).isNotEqualTo(artifact);
        assertThat(artifact6).isNotEqualTo(artifact);
    }

    @Test
    void testHashCode() throws Exception {
        Artifact artifact0 = Artifact.of("gid:aid:ext:cla:ver");
        Artifact artifact1 = Artifact.of("gid", "aid", "ext", "cla", "ver");
        assertThat(artifact1.hashCode()).isEqualTo(artifact0.hashCode());
    }
}
