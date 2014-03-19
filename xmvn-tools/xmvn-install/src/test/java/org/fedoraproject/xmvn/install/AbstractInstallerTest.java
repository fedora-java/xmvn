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

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.InstallationResult;
import org.fedoraproject.xmvn.tools.install.Installer;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractInstallerTest
    extends InjectedTest
{
    private Logger logger = LoggerFactory.getLogger( AbstractInstallerTest.class );

    protected Installer installer;

    private final MavenXpp3Reader modelReader = new MavenXpp3Reader();

    protected InstallationRequest request = new InstallationRequest();

    protected InstallationResult result;

    protected Path installRoot;

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        logger = lookup( Logger.class );
        assertNotNull( logger );

        installer = lookup( Installer.class );
        assertNotNull( installer );

        installRoot = Files.createTempDirectory( "xmvn-installer-test" ).toAbsolutePath();
        installRoot.toFile().deleteOnExit();

        request.setBasePackageName( "package" );
        request.setCheckForUnmatchedRules( true );
        request.setInstallRoot( installRoot );

        logger.info( "Installation root is {}", installRoot );
    }

    protected void addJarArtifact( String pom )
        throws Exception
    {
        addJarArtifact( pom, "empty" );
    }

    protected void addJarArtifact( String pom, String jar )
        throws Exception
    {
        Path artifactPath = Paths.get( "src/test/resources/jar/" + jar + ".jar" );
        Path modelPath = Paths.get( "src/test/resources/pom/" + pom + ".pom" );

        Model model;
        try (InputStream is = Files.newInputStream( modelPath ))
        {
            model = modelReader.read( is );
        }

        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        String version = model.getVersion();

        Parent parent = model.getParent();
        if ( groupId == null )
            groupId = parent.getGroupId();
        if ( version == null )
            version = parent.getVersion();

        Artifact artifact = new DefaultArtifact( groupId, artifactId, "jar", version );
        artifact = artifact.setPath( artifactPath );
        request.addArtifact( artifact );

        Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "pom", version );
        pomArtifact = pomArtifact.setPath( modelPath );
        request.addArtifact( pomArtifact.setStereotype( "raw" ) );
        request.addArtifact( pomArtifact.setStereotype( "effective" ) );

        logger.info( "Added arrifact {}", artifact );
        logger.info( "  POM path: {}", modelPath.toAbsolutePath() );
        logger.info( "  JAR path: {}", artifactPath.toAbsolutePath() );
    }

    protected void performInstallation()
    {
        result = installer.install( request );
    }

    protected void assertXmlEqual( StringBuilder expected, String generated )
        throws Exception
    {
        Path generatedPath = installRoot.resolve( generated );
        assertTrue( Files.isRegularFile( generatedPath ) );

        try (Reader expectedReader = new StringReader( expected.toString() ))
        {
            try (Reader generatedReader = Files.newBufferedReader( generatedPath, StandardCharsets.UTF_8 ))
            {
                assertXMLEqual( expectedReader, generatedReader );
            }
        }
    }

    protected void assertXmlEqual( String expected, String generated )
        throws Exception
    {
        Path expectedPath = Paths.get( "src/test/resources" ).resolve( expected );

        Path generatedPath = installRoot.resolve( generated );
        assertTrue( Files.isRegularFile( generatedPath ) );

        try (Reader expectedReader = Files.newBufferedReader( expectedPath, StandardCharsets.UTF_8 ))
        {
            try (Reader generatedReader = Files.newBufferedReader( generatedPath, StandardCharsets.UTF_8 ))
            {
                assertXMLEqual( expectedReader, generatedReader );
            }
        }
    }
}
