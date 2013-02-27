/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = ProjectInstaller.class, hint = "pom" )
public class PomInstaller
    implements ProjectInstaller
{
    @Requirement
    private Configurator configurator;

    @Override
    public List<String> getSupportedPackagingTypes()
    {
        return Collections.singletonList( "pom" );
    }

    @Override
    public void installProject( MavenProject project, Package targetPackage, PackagingRule rule )
        throws IOException
    {
        Artifact artifact = project.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();

        DependencyVisitor metadata = targetPackage.getMetadata();
        InstallerSettings settings = configurator.getConfiguration().getInstallerSettings();
        String packageName = settings.getPackageName();

        Path pomFile = project.getFile().toPath();
        Path jppGroup = Paths.get( "JPP" ).resolve( packageName );
        Path jppName = Paths.get( groupId + "@" + artifactId );

        Model rawModel = DependencyExtractor.getRawModel( project );
        DependencyExtractor.generateRawRequires( rawModel, metadata );

        targetPackage.addPomFile( pomFile, jppGroup, jppName );
        targetPackage.createDepmaps( groupId, artifactId, version, jppGroup, jppName, rule );

        DependencyExtractor.generateEffectiveRuntimeRequires( project.getModel(), metadata );
    }
}
