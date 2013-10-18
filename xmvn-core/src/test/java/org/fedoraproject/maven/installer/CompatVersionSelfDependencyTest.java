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
package org.fedoraproject.maven.installer;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;

import java.nio.file.Files;

import org.fedoraproject.maven.config.Artifact;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.PackagingRule;

/**
 * Test case for <a href="http://bugzilla.redhat.com/1019670">Red Hat bug #1019670</a>.
 * 
 * @author Mikolaj Izdebski
 */
public class CompatVersionSelfDependencyTest
    extends AbstractInstallerTest
{
    /**
     * Test if dependencies on compatibility artifacts installed as part of the same package are handled properly.
     * 
     * @throws Exception
     */
    public void testCompatVersionSelfDependency()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        assertNotNull( configurator );
        Configuration configuration = configurator.getConfiguration();

        // %mvn_compat_version : 3.6.2 3
        PackagingRule rule = new PackagingRule();
        rule.setArtifactGlob( new Artifact() );
        rule.addVersion( "3.6.2" );
        rule.addVersion( "3" );
        configuration.addArtifactManagement( rule );

        addJarArtifact( "solr-core" );
        addJarArtifact( "solr-solrj" );
        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/solr-core-3.6.2.jar" ) ) );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/jar/solr-core-3.jar" ) ) );
        assertXmlEqual( "pom/solr-core.pom", "repo/raw-pom/JPP-solr-core-3.6.2.pom" );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/raw-pom/JPP-solr-core-3.pom" ) ) );
        assertXmlEqual( "pom/solr-core.pom", "repo/effective-pom/JPP-solr-core-3.6.2.pom" );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/effective-pom/JPP-solr-core-3.pom" ) ) );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/solr-solrj-3.6.2.jar" ) ) );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/jar/solr-solrj-3.jar" ) ) );
        assertXmlEqual( "pom/solr-solrj.pom", "repo/raw-pom/JPP-solr-solrj-3.6.2.pom" );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/raw-pom/JPP-solr-solrj-3.pom" ) ) );
        assertXmlEqual( "pom/solr-solrj.pom", "repo/effective-pom/JPP-solr-solrj-3.6.2.pom" );
        assertTrue( Files.isSymbolicLink( installRoot.resolve( "repo/effective-pom/JPP-solr-solrj-3.pom" ) ) );

        StringBuilder depmap = new StringBuilder();
        depmap.append( "<dependencyMap>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.apache.solr</groupId>" );
        depmap.append( "      <artifactId>solr-core</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>solr-core</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.apache.solr</groupId>" );
        depmap.append( "      <artifactId>solr-core</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>solr-core</artifactId>" );
        depmap.append( "      <version>3</version>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.apache.solr</groupId>" );
        depmap.append( "      <artifactId>solr-solrj</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>solr-solrj</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.apache.solr</groupId>" );
        depmap.append( "      <artifactId>solr-solrj</artifactId>" );
        depmap.append( "      <version>3.6.2</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>solr-solrj</artifactId>" );
        depmap.append( "      <version>3</version>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "</dependencyMap>" );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }
}
