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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.Properties;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactInstallerFactoryTest {
    @Test
    public void testNoPluginsAvailable() {
        Path pluginDir = Path.of("src/test/resources/plugins-not-found").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin-missing");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertTrue(DefaultArtifactInstaller.class.isAssignableFrom(inst.getClass()));
    }

    @Test
    public void testMissingPlugin() {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin-missing");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertTrue(DefaultArtifactInstaller.class.isAssignableFrom(inst.getClass()));
    }

    @Test
    public void testLegacyPlugin() throws Exception {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin1");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertEquals("foo.bar.MyPlugin", inst.getClass().getCanonicalName());

        try {
            inst.install(null, null, null, null, "install");
            fail("Expected AbstractMethodError");
        } catch (AbstractMethodError e) {
            // "Not implemented exception" is thrown by the plugin
            assertTrue(e.getMessage().startsWith("Receiver class foo.bar.MyPlugin"));
            assertTrue(e.getMessage().contains("abstract void install("));
        }
    }

    @Test
    public void testModernPlugin() throws Exception {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin3");
        ArtifactInstaller inst = aif.getInstallerFor(null, props);
        assertEquals("foo.bar.MyPluginModern", inst.getClass().getCanonicalName());

        try {
            inst.install(null, null, null, null, "install");
            fail("Expected UnsupportedOperationException");
        } catch (ArtifactInstallationException e) {
            // "Nothing to do" is thrown by the plugin
            assertEquals("Nothing to do", e.getMessage());
            assertEquals(
                    "foo.bar.MyPluginModern.install(MyPluginModern.java:35)",
                    e.getStackTrace()[0].toString());
        }

        try {
            inst.install(null, null, null, null, "my-repo");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(
                    "This artifact installer does not support non-default repository.",
                    e.getMessage());
        }
    }

    @Test
    public void testBrokenPlugin() {
        Path pluginDir = Path.of("src/test/resources/plugins").toAbsolutePath();
        ArtifactInstallerFactory aif = new ArtifactInstallerFactory(null, pluginDir);
        Properties props = new Properties();
        props.setProperty("type", "myplugin2");
        try {
            aif.getInstallerFor(null, props);
            fail();
        } catch (RuntimeException e) {
            assertEquals(
                    "Unable to load XMvn Installer plugin for packaging type myplugin2",
                    e.getMessage());
            assertNotNull(e.getCause());
            assertTrue(
                    ReflectiveOperationException.class.isAssignableFrom(e.getCause().getClass()));
        }
    }
}
