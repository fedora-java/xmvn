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

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractInstallerTest
    extends PlexusTestCase
{
    protected Logger logger;

    protected Installer installer;

    private final MavenXpp3Reader modelReader = new MavenXpp3Reader();

    protected InstallationRequest request = new InstallationRequest();

    protected InstallationResult result;

    protected Path installRoot;

    @Override
    protected String getCustomConfigurationName()
    {
        return "src/test/resources/plexus/installer-components.xml";
    }

    @Override
    protected void setUp()
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

        logger.info( "Installation root is " + installRoot );
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
        try (InputStream is = new FileInputStream( modelPath.toFile() ))
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
        artifact = artifact.setFile( artifactPath.toFile() );
        artifact = ArtifactUtils.setRawModelPath( artifact, modelPath );
        artifact = ArtifactUtils.setEffectiveModelPath( artifact, modelPath );

        request.addArtifact( artifact );

        logger.info( "Added arrifact " + artifact );
        logger.info( "  POM path: " + modelPath.toAbsolutePath() );
        logger.info( "  JAR path: " + artifactPath.toAbsolutePath() );
    }

    protected void performInstallation()
    {
        result = installer.install( request );
    }

    protected void assertXmlEqual( StringBuilder expected, String generated )
        throws Exception
    {
        try (Reader expectedReader = new StringReader( expected.toString() ))
        {
            try (Reader generatedReader = new FileReader( installRoot.resolve( generated ).toFile() ))
            {
                assertXMLEqual( expectedReader, generatedReader );
            }
        }
    }

    protected void assertXmlEqual( String expected, String generated )
        throws Exception
    {
        try (Reader expectedReader = new FileReader( Paths.get( "src/test/resources" ).resolve( expected ).toFile() ))
        {
            try (Reader generatedReader = new FileReader( installRoot.resolve( generated ).toFile() ))
            {
                assertXMLEqual( expectedReader, generatedReader );
            }
        }
    }
}
