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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
class Utils
{
    public static Artifact aetherArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
    {
        String groupId = mavenArtifact.getGroupId();
        String artifactId = mavenArtifact.getArtifactId();
        String version = mavenArtifact.getVersion();
        String stereotype = mavenArtifact.getType();

        ArtifactHandler handler = mavenArtifact.getArtifactHandler();
        String extension = handler.getExtension();
        String classifier = handler.getClassifier();
        if ( StringUtils.isNotEmpty( mavenArtifact.getClassifier() ) )
            classifier = mavenArtifact.getClassifier();

        File artifactFile = mavenArtifact.getFile();

        Artifact artifact = new DefaultArtifact( groupId, artifactId, classifier, extension, version );
        artifact = ArtifactUtils.setStereotype( artifact, stereotype );
        artifact = artifact.setFile( artifactFile );
        return artifact;
    }
}
