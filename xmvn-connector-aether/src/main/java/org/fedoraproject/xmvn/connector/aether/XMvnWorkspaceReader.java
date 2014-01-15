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

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
@Named( "ide" )
@Singleton
public class XMvnWorkspaceReader
    implements WorkspaceReader
{
    private final Resolver resolver;

    private static final WorkspaceRepository repository = new WorkspaceRepository();

    @Inject
    public XMvnWorkspaceReader( Resolver resolver )
    {
        this.resolver = resolver;
    }

    @Override
    public File findArtifact( Artifact artifact )
    {
        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolver.resolve( request );
        return result.getArtifactFile();
    }

    @Override
    public List<String> findVersions( Artifact artifact )
    {
        return Collections.singletonList( ArtifactUtils.DEFAULT_VERSION );
    }

    @Override
    public WorkspaceRepository getRepository()
    {
        return repository;
    }
}
