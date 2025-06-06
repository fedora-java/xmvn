/*-
 * Copyright (c) 2013-2025 Red Hat, Inc.
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

import java.util.List;
import org.fedoraproject.xmvn.config.Artifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class EffectivePackagingTest {
    /**
     * Test if multiple rules are correctly aggregated into single effective rule.
     *
     * @throws Exception
     */
    @Test
    void ruleAggregation() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        Artifact glob = new Artifact();
        glob.setGroupId("foo");
        glob.setArtifactId("bar");
        glob.setExtension("the=ext");
        glob.setClassifier("_my_clasfr");
        glob.setVersion("baz");

        PackagingRule rule1 = new PackagingRule();
        rule1.setArtifactGlob(glob);
        rule1.addFile("file1");
        artifactManagement.add(rule1);

        PackagingRule rule2 = new PackagingRule();
        rule2.setArtifactGlob(glob);
        rule2.addFile("file2");
        artifactManagement.add(rule2);

        PackagingRule effectiveRule =
                new EffectivePackagingRule(
                        artifactManagement, "foo", "bar", "the=ext", "_my_clasfr", "baz");
        assertThat(effectiveRule.getFiles().get(0)).isEqualTo("file1");
        assertThat(effectiveRule.getFiles().get(1)).isEqualTo("file2");
        assertThat(effectiveRule.getFiles()).hasSize(2);
    }

    /**
     * Test if wildcard matching works as expected.
     *
     * @throws Exception
     */
    @Test
    void wildcards() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        Artifact glob = new Artifact();
        glob.setGroupId("foo*bar");
        glob.setArtifactId("{lorem,ipsum}-dolor");
        glob.setVersion("1.2.3");

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob(glob);
        rule.setTargetPackage("pkgX");
        artifactManagement.add(rule);

        PackagingRule effRule1 =
                new EffectivePackagingRule(
                        artifactManagement, "foo-test-bar", "ipsum-dolor", "jar", "", "1.2.3");
        assertThat(effRule1.getTargetPackage()).isNotNull();
        assertThat(effRule1.getTargetPackage()).isEqualTo("pkgX");

        PackagingRule effRule2 =
                new EffectivePackagingRule(
                        artifactManagement, "foobar", "lorem-dolor", "jar", "", "1.2.3");
        assertThat(effRule2.getTargetPackage()).isNotNull();
        assertThat(effRule2.getTargetPackage()).isEqualTo("pkgX");

        PackagingRule effRule3 =
                new EffectivePackagingRule(
                        artifactManagement, "foobar", "lorem-dolor", "jar", "", "1.253");
        assertThat(effRule3.getTargetPackage()).isNull();
    }

    /**
     * Test if empty glob *:*:* matches any artifact.
     *
     * @throws Exception
     */
    @Test
    void emptyGlob() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob(new Artifact());
        rule.setTargetPackage("somePackage");
        artifactManagement.add(rule);

        PackagingRule effRule1 =
                new EffectivePackagingRule(
                        artifactManagement, "maven-plugin", "com.example", "jar", "funny", "0.42");
        assertThat(effRule1.getTargetPackage()).isNotNull();
        assertThat(effRule1.getTargetPackage()).isEqualTo("somePackage");
    }

    /**
     * Test if empty pattern matches everything.
     *
     * @throws Exception
     */
    @Test
    void emptyPattern() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        Artifact glob = new Artifact();
        glob.setGroupId("");
        glob.setArtifactId("");
        glob.setExtension("");
        glob.setClassifier("");
        glob.setVersion("");

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob(glob);
        rule.setTargetPackage("fooBar");
        artifactManagement.add(rule);

        PackagingRule effectiveRule =
                new EffectivePackagingRule(artifactManagement, "bar", "baz", "xy", "zzy", "1.2.3");
        assertThat(effectiveRule).isNotNull();
        assertThat(effectiveRule.getTargetPackage()).isNotNull();
        assertThat(effectiveRule.getTargetPackage()).isEqualTo("fooBar");
    }

    /**
     * Test if explicit rules correctly override default singleton packaging rule.
     *
     * @throws Exception
     */
    @Test
    void singletonAndSpecificRule() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        Artifact glob1 = new Artifact();
        glob1.setGroupId("");
        glob1.setArtifactId("{sisu,guice}-{*}");
        glob1.setVersion("");

        PackagingRule rule1 = new PackagingRule();
        rule1.setArtifactGlob(glob1);
        rule1.setTargetPackage("@2");
        artifactManagement.add(rule1);

        Artifact glob2 = new Artifact();
        glob2.setArtifactId("{*}");

        PackagingRule rule2 = new PackagingRule();
        rule2.setArtifactGlob(glob2);
        rule2.setTargetPackage("@1");
        artifactManagement.add(rule2);

        PackagingRule effRule =
                new EffectivePackagingRule(
                        artifactManagement, "org.sonatype.sisu", "sisu-parent", "pom", "", "2.3.0");
        assertThat(effRule.getTargetPackage()).isNotNull();
        assertThat(effRule.getTargetPackage()).isEqualTo("parent");
    }

    /**
     * Test if artifact aliases work as expected.
     *
     * @throws Exception
     */
    @Test
    void aliases() throws Exception {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertThat(artifactManagement).isEmpty();

        Artifact glob = new Artifact();
        glob.setGroupId("");
        glob.setArtifactId("{*}");
        glob.setExtension("");
        glob.setClassifier("");
        glob.setVersion("");

        Artifact alias = new Artifact();
        alias.setGroupId("");
        alias.setArtifactId("@1-test");
        alias.setExtension("");
        alias.setClassifier("");
        alias.setVersion("");

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob(glob);
        rule.addAlias(alias);
        artifactManagement.add(rule);

        PackagingRule effRule =
                new EffectivePackagingRule(artifactManagement, "foo", "bar", "jar", "", "1.2.3");

        assertThat(effRule.getAliases()).hasSize(1);
        Artifact effAlias = effRule.getAliases().iterator().next();

        assertThat(effAlias.getGroupId()).isEqualTo("foo");
        assertThat(effAlias.getArtifactId()).isEqualTo("bar-test");
        assertThat(effAlias.getVersion()).isEqualTo("1.2.3");
    }
}
