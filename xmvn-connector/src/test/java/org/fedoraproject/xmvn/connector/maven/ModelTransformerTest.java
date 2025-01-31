/*-
 * Copyright (c) 2015-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Extension;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.api.spi.ModelTransformer;
import org.fedoraproject.xmvn.config.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 * @author Roman Vais
 */
public class ModelTransformerTest extends AbstractTest {
    private ModelTransformer transformer;

    private Configurator configurator;

    private ArrayList<Dependency> dl;
    private ArrayList<Extension> el;
    private ArrayList<Plugin> pl;

    @Override
    public void configure(Binder binder) {
        binder.bind(ModelTransformer.class).to(XMvnModelTransformer.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        configurator = lookup(Configurator.class);
        transformer = lookup(ModelTransformer.class);

        dl = new ArrayList<>();
        Dependency dep = Dependency.newInstance();
        dl.add(dep);

        el = new ArrayList<>();
        Extension ext = Extension.newInstance();
        el.add(ext);

        pl = new ArrayList<>();

        pl.add(
                Plugin.newBuilder()
                        .groupId("org.apache.maven.plugins")
                        .artifactId("maven-compiler-plugin")
                        .dependencies(List.of(dep))
                        .build());

        pl.add(
                Plugin.newBuilder()
                        .groupId("org.apache.maven.plugins")
                        .artifactId("foo")
                        .dependencies(List.of(dep))
                        .version("starter edition")
                        .build());

        pl.add(
                Plugin.newBuilder()
                        .groupId("foofoo")
                        .artifactId("maven-compiler-plugin")
                        .dependencies(List.of(dep))
                        .version("[1.0]")
                        .build());

        pl.add(
                Plugin.newBuilder()
                        .groupId("foobar")
                        .artifactId("bar")
                        .dependencies(List.of(dep))
                        .version("(")
                        .build());
    }

    @Test
    public void testMinimalModelTransformation() throws Exception {
        Model model = Model.newInstance();
        assertTrue(transformer instanceof XMvnModelTransformer);
        transformer.transformEffectiveModel(model);
    }

    @Test
    public void testFullModelTransformation() throws Exception {
        Model model =
                Model.newBuilder()
                        .build(Build.newBuilder().extensions(el).plugins(pl).build())
                        .dependencies(dl)
                        .build();
        transformer.transformEffectiveModel(model);
    }

    @Test
    public void testRemovingTestDeps() throws Exception {
        configurator.getConfiguration().getBuildSettings().setSkipTests(true);
        dl.add(0, dl.remove(0).withScope("test"));

        Model model =
                Model.newBuilder()
                        .dependencies(List.of(Dependency.newBuilder().scope("test").build()))
                        .build();
        model = transformer.transformEffectiveModel(model);

        assertEquals(0, model.getDependencies().size());
    }

    @Test
    public void testSkippedPlugins() throws Exception {
        Artifact sp1 = new Artifact();
        sp1.setArtifactId("maven-compiler-plugin");
        configurator.getConfiguration().getBuildSettings().getSkippedPlugins().add(sp1);
        Artifact sp2 = new Artifact();
        sp2.setGroupId("org.apache.maven.plugins");
        sp2.setVersion("starter edition");
        configurator.getConfiguration().getBuildSettings().getSkippedPlugins().add(sp2);

        Model model = Model.newBuilder().build(Build.newBuilder().plugins(pl).build()).build();
        model = transformer.transformEffectiveModel(model);

        assertEquals(1, model.getBuild().getPlugins().size());
        Plugin plugin = model.getBuild().getPlugins().iterator().next();
        assertEquals("foobar", plugin.getGroupId());
        assertEquals("bar", plugin.getArtifactId());
    }
}
