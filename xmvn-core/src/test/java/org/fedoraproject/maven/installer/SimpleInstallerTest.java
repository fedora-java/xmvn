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
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Mikolaj Izdebski
 */
public class SimpleInstallerTest
    extends AbstractInstallerTest
{
    /**
     * Test installation of a single JAR artifact without any dependencies.
     * 
     * @throws Exception
     */
    public void testSimpleInstallation()
        throws Exception
    {
        addJarArtifact( "aether-api" );
        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/aether-api.jar" ) ) );
        assertXmlEqual( "pom/aether-api.pom", "repo/raw-pom/JPP-aether-api.pom" );
        assertXmlEqual( "pom/aether-api.pom", "repo/effective-pom/JPP-aether-api.pom" );

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
        depmap.append( "</dependencyMap>" );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }

    /**
     * Test if manifests are not injected unless the JAR previously contained a manifest.
     * 
     * @throws Exception
     */
    public void testNewManifestInjection()
        throws Exception
    {
        addJarArtifact( "aether-api", "no-manifest" );
        performInstallation();

        Path jarPath = installRoot.resolve( "repo/jar/aether-api.jar" );
        assertTrue( Files.isRegularFile( jarPath ) );

        try (JarFile jarFile = new JarFile( jarPath.toFile() ))
        {
            Manifest mf = jarFile.getManifest();
            assertNull( mf );
        }
    }
}
