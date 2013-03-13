/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.config;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;

public class EffectivePackagingTest
    extends PlexusTestCase
{
    /**
     * Test if multiple rules are correctly aggregated into single effective rule.
     * 
     * @throws Exception
     */
    public void testRuleAggregation()
        throws Exception
    {
        Configuration configuration = lookup( Configurator.class ).getDefaultConfiguration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setGroupId( "foo" );
        glob.setArtifactId( "bar" );
        glob.setVersion( "baz" );

        PackagingRule rule1 = new PackagingRule();
        rule1.setArtifactGlob( glob );
        rule1.addFile( "file1" );
        artifactManagement.add( rule1 );

        PackagingRule rule2 = new PackagingRule();
        rule2.setArtifactGlob( glob );
        rule2.addFile( "file2" );
        artifactManagement.add( rule2 );

        PackagingRule effectiveRule = configuration.createEffectivePackagingRule( "foo", "bar", "baz" );
        assertTrue( effectiveRule.getFiles().get( 0 ).equals( "file1" ) );
        assertTrue( effectiveRule.getFiles().get( 1 ).equals( "file2" ) );
        assertEquals( effectiveRule.getFiles().size(), 2 );

        artifactManagement.clear();
    }

    /**
     * Test if wildcard matching works as expected.
     * 
     * @throws Exception
     */
    public void testWildcards()
        throws Exception
    {
        Configuration configuration = lookup( Configurator.class ).getDefaultConfiguration();
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

        PackagingRule effRule1 = configuration.createEffectivePackagingRule( "foo-test-bar", "ipsum-dolor", "1.2.3" );
        assertNotNull( effRule1.getTargetPackage() );
        assertTrue( effRule1.getTargetPackage().equals( "pkgX" ) );

        PackagingRule effRule2 = configuration.createEffectivePackagingRule( "foobar", "lorem-dolor", "1.2.3" );
        assertNotNull( effRule2.getTargetPackage() );
        assertTrue( effRule2.getTargetPackage().equals( "pkgX" ) );

        PackagingRule effRule3 = configuration.createEffectivePackagingRule( "foobar", "lorem-dolor", "1.253" );
        assertNull( effRule3.getTargetPackage() );

        artifactManagement.clear();
    }

    /**
     * Test if empty glob *:*:* matches any artifact.
     * 
     * @throws Exception
     */
    public void testEmptyGlob()
        throws Exception
    {
        Configuration configuration = lookup( Configurator.class ).getDefaultConfiguration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( new Artifact() );
        rule.setTargetPackage( "somePackage" );
        artifactManagement.add( rule );

        PackagingRule effRule1 = configuration.createEffectivePackagingRule( "com.example", "some-test", "0.42" );
        assertNotNull( effRule1.getTargetPackage() );
        assertTrue( effRule1.getTargetPackage().equals( "somePackage" ) );

        artifactManagement.clear();
    }

    /**
     * Test if empty pattern matches everything.
     * 
     * @throws Exception
     */
    public void testEmptyPattern()
        throws Exception
    {
        Configuration configuration = lookup( Configurator.class ).getDefaultConfiguration();
        List<PackagingRule> artifactManagement = configuration.getArtifactManagement();
        assertTrue( artifactManagement.isEmpty() );

        Artifact glob = new Artifact();
        glob.setGroupId( "" );
        glob.setArtifactId( "" );
        glob.setVersion( "" );

        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( glob );
        rule.setTargetPackage( "fooBar" );
        artifactManagement.add( rule );

        PackagingRule effectiveRule = configuration.createEffectivePackagingRule( "foo", "bar", "baz" );
        assertNotNull( effectiveRule );
        assertNotNull( effectiveRule.getTargetPackage() );
        assertTrue( effectiveRule.getTargetPackage().equals( "fooBar" ) );

        artifactManagement.clear();
    }
}
