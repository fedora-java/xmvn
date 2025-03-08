/*-
 * Copyright (c) 2017-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool.installer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for XMvn Installer tool.
 *
 * @author Mikolaj Izdebski
 */
class InstallerIntegrationTest extends AbstractToolIntegrationTest {
    @Test
    void testInstallJar() throws Exception {
        expandBaseDirInPlace("install-plan.xml");

        int rc =
                invokeTool(
                        "xmvn-install",
                        "-n",
                        "xyzzy",
                        "-R",
                        "install-plan.xml",
                        "-d",
                        "dest",
                        "-X",
                        "-i",
                        "custom-install");
        assertThat(rc).isEqualTo(0);
        assertThat(getStdout()).isEmpty();
        assertThat(getStderr()).contains("[INFO] Installation successful");

        Path pomPath = Path.of("dest/usr/share/maven-poms/xyzzy/junit.pom");
        assertThat(Files.isRegularFile(pomPath, LinkOption.NOFOLLOW_LINKS)).isTrue();
        assertThat(Files.readAllLines(pomPath))
                .first()
                .isEqualTo("NOT A VALID XML <XMvn should not parse this...>");

        Path mdPath = Path.of("dest/usr/share/maven-metadata/xyzzy.xml");
        assertThat(Files.isRegularFile(mdPath, LinkOption.NOFOLLOW_LINKS)).isTrue();
        assertThat(Files.readAllLines(mdPath)).first().isEqualTo("<?xml version=\"1.0\" ?>");

        Path jarPath = Path.of("dest/usr/share/java/xyzzy/junit.jar");
        assertThat(Files.isRegularFile(jarPath, LinkOption.NOFOLLOW_LINKS)).isTrue();
        try (InputStream is =
                new URL("jar:file:" + jarPath.toAbsolutePath() + "!/META-INF/MANIFEST.MF")
                        .openStream()) {
            Attributes mf = new Manifest(is).getMainAttributes();
            assertThat(mf.getValue("JavaPackages-GroupId")).isEqualTo("junit");
            assertThat(mf.getValue("JavaPackages-ArtifactId")).isEqualTo("junit");
            assertThat(mf.getValue("JavaPackages-Version")).isEqualTo("4.12");
            assertThat(mf.getValue("X-Test1")).isEqualTo("42");
        }
    }
}
