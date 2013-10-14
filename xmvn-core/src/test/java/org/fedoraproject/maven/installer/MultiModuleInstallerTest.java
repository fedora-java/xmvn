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

/**
 * @author Mikolaj Izdebski
 */
public class MultiModuleInstallerTest
    extends AbstractInstallerTest
{
    /**
     * Test installation of a two JAR artifacts where one depends on the other.
     * 
     * @throws Exception
     */
    public void testMultiModuleInstallation()
        throws Exception
    {
        addJarArtifact( "aether-api" );
        addJarArtifact( "aether-spi" );
        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/aether-api.jar" ) ) );
        assertXmlEqual( "pom/aether-api.pom", "repo/raw-pom/JPP-aether-api.pom" );
        assertXmlEqual( "pom/aether-api.pom", "repo/effective-pom/JPP-aether-api.pom" );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/aether-spi.jar" ) ) );
        assertXmlEqual( "pom/aether-spi.pom", "repo/raw-pom/JPP-aether-spi.pom" );
        assertXmlEqual( "pom/aether-spi.pom", "repo/effective-pom/JPP-aether-spi.pom" );

        StringBuilder depmap = new StringBuilder();
        depmap.append( "<dependencyMap>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.eclipse.aether</groupId>" );
        depmap.append( "      <artifactId>aether-api</artifactId>" );
        depmap.append( "      <version>0.9.0.M3</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>aether-api</artifactId>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.eclipse.aether</groupId>" );
        depmap.append( "      <artifactId>aether-spi</artifactId>" );
        depmap.append( "      <version>0.9.0.M3</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>aether-spi</artifactId>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "</dependencyMap>" );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }
}
