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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = ProjectInstaller.class, hint = "jar" )
public class JarInstaller
    extends AbstractProjectInstaller
{
    @Requirement
    private Configurator configurator;

    @Override
    public List<String> getSupportedPackagingTypes()
    {
        return Arrays.asList( "bundle", "ejb", "jar", "maven-plugin" );
    }

    @Override
    public void installProject( MavenProject project, Package targetPackage, PackagingRule rule )
        throws IOException
    {
        Artifact artifact = project.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        Path file = artifact.getFile() != null ? artifact.getFile().toPath() : null;

        DependencyVisitor metadata = targetPackage.getMetadata();
        InstallerSettings settings = configurator.getConfiguration().getInstallerSettings();
        String packageName = settings.getPackageName();

        List<Path> extraList = new ArrayList<>( rule.getFiles().size() );
        for ( String fileName : rule.getFiles() )
            extraList.add( Paths.get( fileName ) );

        Path baseFile = Paths.get( packageName + "/" + artifactId );
        if ( !extraList.isEmpty() )
            baseFile = extraList.remove( 0 );

        Path jppName = baseFile.getFileName();
        Path jppGroup = Paths.get( "JPP" );
        if ( baseFile.getParent() != null )
            jppGroup = jppGroup.resolve( baseFile.getParent() );

        targetPackage.addJarFile( file, baseFile, extraList );

        DependencyExtractor.getJavaCompilerTarget( project, metadata );

        boolean installRawPom = settings.isEnableRawPoms() && settings.isJarRawModel();
        boolean installEffectivePom = settings.isEnableEffectivePoms() && settings.isJarEffectiveModel();
        installProjectPom( project, targetPackage, jppGroup, jppName, installRawPom, installEffectivePom );

        targetPackage.createDepmaps( groupId, artifactId, version, jppGroup, jppName, rule );

        DependencyExtractor.generateEffectiveRuntimeRequires( project.getModel(), metadata );
    }
}
