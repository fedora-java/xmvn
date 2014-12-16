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
package org.fedoraproject.xmvn.connector.aether;

import java.util.List;

import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.plugin.version.PluginVersionResult;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.ArtifactRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = PluginVersionResolver.class )
public class XMvnPluginVersionResolver
    implements PluginVersionResolver
{
    @Override
    public PluginVersionResult resolve( PluginVersionRequest request )
    {
        RepositorySystemSession session = request.getRepositorySession();
        WorkspaceReader reader = session.getWorkspaceReader();
        List<String> versions =
            reader.findVersions( new DefaultArtifact( request.getGroupId(), request.getArtifactId(),
                                                      Artifact.DEFAULT_EXTENSION, Artifact.DEFAULT_VERSION ) );
        final String version = versions.isEmpty() ? Artifact.DEFAULT_VERSION : versions.iterator().next();
        final ArtifactRepository repository = reader.getRepository();

        return new PluginVersionResult()
        {
            @Override
            public ArtifactRepository getRepository()
            {
                return repository;
            }

            @Override
            public String getVersion()
            {
                return version;
            }
        };
    }
}
