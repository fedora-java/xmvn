/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.install;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class BasicIvyInstallerTest
    extends AbstractInstallerTest
{
    private final Logger logger = LoggerFactory.getLogger( BasicIvyInstallerTest.class );

    /**
     * Test installation of Ivy modules.
     * 
     * @throws Exception
     */
    @Test
    public void testIvyModelInstallation()
        throws Exception
    {
        Path artifactPath = Paths.get( "src/test/resources/jar/empty.jar" );
        Path modelPath = Paths.get( "src/test/resources/ivy/simple.ivy" );

        Artifact artifact = new DefaultArtifact( "org.apache", "hello-ivy", "jar", "1.2.3" );
        artifact = artifact.setPath( artifactPath );
        request.addArtifact( artifact );

        Artifact modelArtifact = new DefaultArtifact( "org.apache", "hello-ivy", "ivy", "1.2.3" );
        modelArtifact = modelArtifact.setPath( modelPath );
        request.addArtifact( modelArtifact );

        logger.info( "Added arrifact {}", artifact );
        logger.info( "  POM path: {}", modelPath.toAbsolutePath() );
        logger.info( "  JAR path: {}", artifactPath.toAbsolutePath() );

        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/hello-ivy.jar" ) ) );
        assertXmlEqual( "ivy/simple.ivy", "repo/raw-pom/JPP-hello-ivy.pom" );
        assertXmlEqual( "ivy/simple.pom", "repo/effective-pom/JPP-hello-ivy.pom" );

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
        depmap.append( "</dependencyMap>" );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }
}
