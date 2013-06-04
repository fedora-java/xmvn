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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.project.MavenProject;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractProjectInstaller
    implements ProjectInstaller
{
    void installProjectPom( MavenProject project, Package targetPackage, Path jppGroup, Path jppName,
                            boolean installRawPom, boolean installEffectivePom )
        throws IOException
    {
        if ( installRawPom )
        {
            Path pomFile = project.getFile().toPath();
            targetPackage.addPomFile( pomFile, jppGroup, jppName );
        }

        if ( installEffectivePom )
        {
            Path pomFile = Files.createTempFile( "xmvn-" + project.getArtifactId() + "-", ".pom.xml" );
            DependencyExtractor.simplifyEffectiveModel( project.getModel() );
            DependencyExtractor.writeModel( project.getModel(), pomFile );
            targetPackage.addEffectivePomFile( pomFile, jppGroup, jppName );
        }
    }
}
