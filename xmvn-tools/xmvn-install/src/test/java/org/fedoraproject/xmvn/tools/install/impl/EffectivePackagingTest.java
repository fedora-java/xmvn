/*-
 * Copyright (c) 2013-2021 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.config.Artifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.PackagingRule;

/**
 * @author Mikolaj Izdebski
 */
public class EffectivePackagingTest
{
    /**
     * Test if multiple rules are correctly aggregated into single effective rule.
     * 
     * @throws Exception
     */
    @Test
    public void testRuleAggregation()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setGroupId( "foo" );
        glob.setArtifactId( "bar" );
        glob.setExtension( "the=ext" );
        glob.setClassifier( "_my_clasfr" );
        glob.setVersion( "baz" );

        PackagingRule rule1 = new PackagingRule();
        rule1.setArtifactGlob( glob );
        rule1.addFile( "file1" );
        artifactManagement.add( rule1 );

        PackagingRule rule2 = new PackagingRule();
        rule2.setArtifactGlob( glob );
        rule2.addFile( "file2" );
        artifactManagement.add( rule2 );

        PackagingRule effectiveRule =
            new EffectivePackagingRule( artifactManagement, "foo", "bar", "the=ext", "_my_clasfr", "baz" );
        assertTrue( effectiveRule.getFiles().get( 0 ).equals( "file1" ) );
        assertTrue( effectiveRule.getFiles().get( 1 ).equals( "file2" ) );
        assertEquals( effectiveRule.getFiles().size(), 2 );
    }

    /**
     * Test if wildcard matching works as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testWildcards()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setGroupId( "foo*bar" );
        glob.setArtifactId( "{lorem,ipsum}-dolor" );
        glob.setVersion( "1.2.3" );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( glob );
        rule.setTargetPackage( "pkgX" );
        artifactManagement.add( rule );

        PackagingRule effRule1 =
            new EffectivePackagingRule( artifactManagement, "foo-test-bar", "ipsum-dolor", "jar", "", "1.2.3" );
        assertNotNull( effRule1.getTargetPackage() );
        assertTrue( effRule1.getTargetPackage().equals( "pkgX" ) );

        PackagingRule effRule2 =
            new EffectivePackagingRule( artifactManagement, "foobar", "lorem-dolor", "jar", "", "1.2.3" );
        assertNotNull( effRule2.getTargetPackage() );
        assertTrue( effRule2.getTargetPackage().equals( "pkgX" ) );

        PackagingRule effRule3 =
            new EffectivePackagingRule( artifactManagement, "foobar", "lorem-dolor", "jar", "", "1.253" );
        assertNull( effRule3.getTargetPackage() );
    }

    /**
     * Test if empty glob *:*:* matches any artifact.
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyGlob()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( new Artifact() );
        rule.setTargetPackage( "somePackage" );
        artifactManagement.add( rule );

        PackagingRule effRule1 =
            new EffectivePackagingRule( artifactManagement, "maven-plugin", "com.example", "jar", "funny", "0.42" );
        assertNotNull( effRule1.getTargetPackage() );
        assertTrue( effRule1.getTargetPackage().equals( "somePackage" ) );
    }

    /**
     * Test if empty pattern matches everything.
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyPattern()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setStereotype( "" );
        glob.setGroupId( "" );
        glob.setArtifactId( "" );
        glob.setExtension( "" );
        glob.setClassifier( "" );
        glob.setVersion( "" );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( glob );
        rule.setTargetPackage( "fooBar" );
        artifactManagement.add( rule );

        PackagingRule effectiveRule =
            new EffectivePackagingRule( artifactManagement, "bar", "baz", "xy", "zzy", "1.2.3" );
        assertNotNull( effectiveRule );
        assertNotNull( effectiveRule.getTargetPackage() );
        assertTrue( effectiveRule.getTargetPackage().equals( "fooBar" ) );
    }

    /**
     * Test if explicit rules correctly override default singleton packaging rule.
     * 
     * @throws Exception
     */
    @Test
    public void testSingletonAndSpecificRule()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob1 = new Artifact();
        glob1.setGroupId( "" );
        glob1.setArtifactId( "{sisu,guice}-{*}" );
        glob1.setVersion( "" );

        PackagingRule rule1 = new PackagingRule();
        rule1.setArtifactGlob( glob1 );
        rule1.setTargetPackage( "@2" );
        artifactManagement.add( rule1 );

        Artifact glob2 = new Artifact();
        glob2.setArtifactId( "{*}" );

        PackagingRule rule2 = new PackagingRule();
        rule2.setArtifactGlob( glob2 );
        rule2.setTargetPackage( "@1" );
        artifactManagement.add( rule2 );

        PackagingRule effRule =
            new EffectivePackagingRule( artifactManagement, "org.sonatype.sisu", "sisu-parent", "pom", "", "2.3.0" );
        assertNotNull( effRule.getTargetPackage() );
        assertTrue( effRule.getTargetPackage().equals( "parent" ) );
    }

    /**
     * Test if artifact aliases work as expected.
     * 
     * @throws Exception
     */
    @Test
    public void testAliases()
        throws Exception
    {
        Configuration configuration = new Configuration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setStereotype( "" );
        glob.setGroupId( "" );
        glob.setArtifactId( "{*}" );
        glob.setExtension( "" );
        glob.setClassifier( "" );
        glob.setVersion( "" );

        Artifact alias = new Artifact();
        alias.setStereotype( "" );
        alias.setGroupId( "" );
        alias.setArtifactId( "@1-test" );
        alias.setExtension( "" );
        alias.setClassifier( "" );
        alias.setVersion( "" );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( glob );
        rule.addAlias( alias );
        artifactManagement.add( rule );

        PackagingRule effRule = new EffectivePackagingRule( artifactManagement, "foo", "bar", "jar", "", "1.2.3" );

        assertEquals( effRule.getAliases().size(), 1 );
        Artifact effAlias = effRule.getAliases().iterator().next();

        assertEquals( effAlias.getGroupId(), "foo" );
        assertEquals( effAlias.getArtifactId(), "bar-test" );
        assertEquals( effAlias.getVersion(), "1.2.3" );
    }
}
