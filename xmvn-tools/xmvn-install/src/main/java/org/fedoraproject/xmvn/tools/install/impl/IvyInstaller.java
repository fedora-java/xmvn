/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.PackagingRule;

/**
 * @author Mikolaj Izdebski
 */
@Named( "ivy" )
public class IvyInstaller
    implements ArtifactInstaller
{
    private final Logger logger = LoggerFactory.getLogger( IvyInstaller.class );

    private final ArtifactInstaller rawPomInstaller;

    private final ArtifactInstaller effectivePomInstaller;

    @Inject
    public IvyInstaller( @Named( "pom/raw" ) ArtifactInstaller rawPomInstaller,
                         @Named( "pom/effective" ) ArtifactInstaller effectivePomInstaller )
    {
        this.rawPomInstaller = rawPomInstaller;
        this.effectivePomInstaller = effectivePomInstaller;
    }

    @Override
    public void installArtifact( Package pkg, Artifact artifact, PackagingRule rule, String packageName )
        throws IOException
    {
        try
        {
            Artifact pomArtifact =
                new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), "pom",
                                     artifact.getVersion() );

            File ivyPath = artifact.getFile();
            File pomPath = Files.createTempFile( "xmvn-", ".ivy.pom" ).toFile();
            ModuleDescriptorParser parser = XmlModuleDescriptorParser.getInstance();
            ModuleDescriptor module = parser.parseDescriptor( new IvySettings(), ivyPath.toURI().toURL(), false );
            PomModuleDescriptorWriter.write( module, pomPath, new PomWriterOptions() );
            logger.debug( "Converted Ivy XML file {} to Maven POM {}", ivyPath, pomPath );

            Artifact effectivePomArtifact = pomArtifact.setStereotype( "effective" );
            effectivePomArtifact = effectivePomArtifact.setFile( pomPath );
            effectivePomInstaller.installArtifact( pkg, effectivePomArtifact, rule, packageName );

            Artifact rawPomArtifact = pomArtifact.setStereotype( "raw" );
            rawPomArtifact = rawPomArtifact.setFile( ivyPath );
            rawPomInstaller.installArtifact( pkg, rawPomArtifact, rule, packageName );
        }
        catch ( IOException | ParseException e )
        {
            throw new RuntimeException( e );
        }
    }
}
