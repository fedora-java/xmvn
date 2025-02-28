/*-
 * Copyright (c) 2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo.install;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.fedoraproject.xmvn.it.maven.mojo.AbstractMojoIntegrationTest;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.junit.jupiter.api.Test;

public class InstallMojoIntegrationTest extends AbstractMojoIntegrationTest {
    private static String id(ArtifactMetadata amd) {
        return amd.getArtifactId() + ":" + amd.getClassifier() + ":" + amd.getExtension();
    }

    @Test
    public void testInstallMojo() throws Exception {
        performMojoTest("package", "install");
        Path reactorPath = Path.of(".xmvn-reactor");
        assertTrue(Files.isRegularFile(reactorPath));
        PackageMetadata pmd = PackageMetadata.readFromXML(reactorPath);
        assertEquals(6, pmd.getArtifacts().size());

        var map = pmd.getArtifacts().stream().collect(Collectors.toMap(amd -> id(amd), amd -> amd));
        List<ArtifactMetadata> sortedList = new ArrayList<>(new TreeMap<>(map).values());
        Iterator<ArtifactMetadata> it = sortedList.iterator();

        assertTrue(it.hasNext());
        ArtifactMetadata aj = it.next();
        assertEquals("xmvn.it.install.mojo", aj.getGroupId());
        assertEquals("install-attached", aj.getArtifactId());
        assertEquals("jar", aj.getExtension());
        assertEquals("", aj.getClassifier());
        assertEquals("42", aj.getVersion());
        assertTrue(Files.isRegularFile(Path.of(aj.getPath())));
        assertEquals(1, aj.getDependencies().size());

        assertTrue(it.hasNext());
        ArtifactMetadata ap = it.next();
        assertEquals("xmvn.it.install.mojo", ap.getGroupId());
        assertEquals("install-attached", ap.getArtifactId());
        assertEquals("pom", ap.getExtension());
        assertEquals("", ap.getClassifier());
        assertEquals("42", ap.getVersion());
        assertTrue(Files.isRegularFile(Path.of(ap.getPath())));
        assertEquals(1, ap.getDependencies().size());

        assertTrue(it.hasNext());
        ArtifactMetadata at = it.next();
        assertEquals("xmvn.it.install.mojo", at.getGroupId());
        assertEquals("install-attached", at.getArtifactId());
        assertEquals("jar", at.getExtension());
        assertEquals("tests", at.getClassifier());
        assertEquals("42", at.getVersion());
        assertTrue(Files.isRegularFile(Path.of(at.getPath())));
        assertEquals(1, at.getDependencies().size());

        assertTrue(it.hasNext());
        ArtifactMetadata pp = it.next();
        assertEquals("xmvn.it.install.mojo", pp.getGroupId());
        assertEquals("install-parent", pp.getArtifactId());
        assertEquals("pom", pp.getExtension());
        assertEquals("", pp.getClassifier());
        assertEquals("42", pp.getVersion());
        assertTrue(Files.isRegularFile(Path.of(pp.getPath())));
        assertEquals(0, pp.getDependencies().size());

        assertTrue(it.hasNext());
        ArtifactMetadata sj = it.next();
        assertEquals("xmvn.it.install.mojo", sj.getGroupId());
        assertEquals("install-simple", sj.getArtifactId());
        assertEquals("jar", sj.getExtension());
        assertEquals("", sj.getClassifier());
        assertEquals("42", sj.getVersion());
        assertTrue(Files.isRegularFile(Path.of(sj.getPath())));
        assertEquals(0, sj.getDependencies().size());

        assertTrue(it.hasNext());
        ArtifactMetadata sp = it.next();
        assertEquals("xmvn.it.install.mojo", sp.getGroupId());
        assertEquals("install-simple", sp.getArtifactId());
        assertEquals("pom", sp.getExtension());
        assertEquals("", sp.getClassifier());
        assertEquals("42", sp.getVersion());
        assertTrue(Files.isRegularFile(Path.of(sp.getPath())));
        assertEquals(0, sp.getDependencies().size());
    }
}
