/*-
 * Copyright (c) 2015-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.it.maven.AbstractMavenIntegrationTest;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxWriter;

/**
 * Abstract base class MOJO integration tests.
 * 
 * @author Mikolaj Izdebski
 */
public class AbstractMojoIntegrationTest
    extends AbstractMavenIntegrationTest
{
    @BeforeEach
    public void addMetadata()
        throws Exception
    {
        PackageMetadata md = new PackageMetadata();
        md.setUuid( UUID.randomUUID().toString() );

        for ( String module : Arrays.asList( "xmvn-mojo", "xmvn-core", "xmvn-api", "xmvn-parent" ) )
        {
            Path moduleDir = Paths.get( "../../.." ).resolve( module );
            Path pomPath = moduleDir.resolve( "pom.xml" );
            Path jarPath = moduleDir.resolve( "target/classes" );

            assertTrue( Files.exists( pomPath ) );
            ArtifactMetadata pomMd = new ArtifactMetadata();
            pomMd.setUuid( UUID.randomUUID().toString() );
            pomMd.setGroupId( "org.fedoraproject.xmvn" );
            pomMd.setArtifactId( module );
            pomMd.setVersion( "DUMMY_IGNORED" );
            pomMd.addProperty( "xmvn.resolver.disableEffectivePom", "true" );
            pomMd.setExtension( "pom" );
            pomMd.setPath( pomPath.toString() );
            md.addArtifact( pomMd );

            if ( Files.exists( jarPath ) )
            {
                ArtifactMetadata jarMd = pomMd.clone();
                jarMd.setUuid( UUID.randomUUID().toString() );
                jarMd.setExtension( "jar" );
                jarMd.setPath( jarPath.toString() );
                md.addArtifact( jarMd );
            }
        }

        MetadataStaxWriter mdWriter = new MetadataStaxWriter();
        try ( OutputStream os = Files.newOutputStream( Paths.get( "mojo-metadata.xml" ) ) )
        {
            mdWriter.write( os, md );
        }

        Configuration conf = new Configuration();
        conf.setResolverSettings( new ResolverSettings() );
        conf.getResolverSettings().addMetadataRepository( "mojo-metadata.xml" );

        Files.createDirectories( Paths.get( ".xmvn/config.d" ) );
        ConfigurationStaxWriter confWriter = new ConfigurationStaxWriter();
        try ( OutputStream os = Files.newOutputStream( Paths.get( ".xmvn/config.d/mojo-it-conf.xml" ) ) )
        {
            confWriter.write( os, conf );
        }
    }
}
