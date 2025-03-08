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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.nio.file.Path;
import java.util.Properties;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactInstallerFactoryTest {
    @Test
    void noPluginsAvailable() {
        Path pluginDir = Path.of("src/test/resources/plugins-not-found").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin-missing");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertThat(inst).isExactlyInstanceOf(DefaultArtifactInstaller.class);
    }

    @Test
    void missingPlugin() {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin-missing");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertThat(inst).isExactlyInstanceOf(DefaultArtifactInstaller.class);
    }

    @Test
    void legacyPlugin() throws Exception {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin1");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertThat(inst.getClass().getCanonicalName()).isEqualTo("foo.bar.MyPlugin");

        try {
            inst.install(null, null, null, null, "install");
            fail("Expected AbstractMethodError");
        } catch (AbstractMethodError e) {
            // "Not implemented exception" is thrown by the plugin
            assertThat(e).hasMessageStartingWith("Receiver class foo.bar.MyPlugin");
            assertThat(e).hasMessageContaining("abstract void install(");
        }
    }

    @Test
    void modernPlugin() throws Exception {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin3");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertThat(inst.getClass().getCanonicalName()).isEqualTo("foo.bar.MyPluginModern");

        try {
            inst.install(null, null, null, null, "install");
            fail("Expected UnsupportedOperationException");
        } catch (ArtifactInstallationException e) {
            // "Nothing to do" is thrown by the plugin
            assertThat(e).hasMessage("Nothing to do");
            assertThat(e.getStackTrace()[0].toString())
                    .isEqualTo("foo.bar.MyPluginModern.install(MyPluginModern.java:35)");
        }

        try {
            inst.install(null, null, null, null, "my-repo");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertThat(e)
                    .hasMessage("This artifact installer does not support non-default repository.");
        }
    }

    @Test
    void brokenPlugin() {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin2");
        try {
            aif.getInstallerFor(null, props);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e)
                    .hasMessage(
                            "Unable to load XMvn Installer plugin for packaging type myplugin2");
            assertThat(e).hasCauseInstanceOf(ReflectiveOperationException.class);
        }
    }
}
