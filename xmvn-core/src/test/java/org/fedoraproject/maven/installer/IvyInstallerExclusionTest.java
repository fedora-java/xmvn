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
import java.nio.file.Paths;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
public class IvyInstallerExclusionTest
    extends AbstractInstallerTest
{
    /**
     * Test installation of Ivy modules.
     * 
     * @throws Exception
     */
    public void testComponentLookup()
        throws Exception
    {
        Path artifactPath = Paths.get( "src/test/resources/jar/empty.jar" );
        Path modelPath = Paths.get( "src/test/resources/ivy/dependency-exclusion.ivy" );

        Artifact artifact = new DefaultArtifact( "org.apache", "hello-ivy", "jar", "1.2.3" );
        artifact = artifact.setFile( artifactPath.toFile() );
        artifact = ArtifactUtils.setRawModelPath( artifact, modelPath );
        artifact = ArtifactUtils.setEffectiveModelPath( artifact, modelPath );

        request.addArtifact( artifact );

        logger.info( "Added arrifact " + artifact );
        logger.info( "  POM path: " + modelPath.toAbsolutePath() );
        logger.info( "  JAR path: " + artifactPath.toAbsolutePath() );

        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/hello-ivy.jar" ) ) );
        assertXmlEqual( "ivy/dependency-exclusion.ivy", "repo/raw-pom/JPP-hello-ivy.pom" );
        assertXmlEqual( "ivy/dependency-exclusion.pom", "repo/effective-pom/JPP-hello-ivy.pom" );

        StringBuilder depmap = new StringBuilder();
        depmap.append( "<dependencyMap>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.apache</groupId>" );
        depmap.append( "      <artifactId>hello-ivy</artifactId>" );
        depmap.append( "      <version>1.2.3</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>hello-ivy</artifactId>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "  <autoRequires>" );
        depmap.append( "    <namespace>UNKNOWN</namespace>" );
        depmap.append( "    <groupId>junit</groupId>" );
        depmap.append( "    <artifactId>junit</artifactId>" );
        depmap.append( "    <version>UNKNOWN</version>" );
        depmap.append( "  </autoRequires>" );

        depmap.append( "</dependencyMap>" );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }
}
