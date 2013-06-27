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
package org.fedoraproject.maven.resolver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryType;
import org.fedoraproject.maven.repository.SingletonRepository;

/**
 * @author Mikolaj Izdebski
 */
class LocalResolver
    extends AbstractResolver
{
    private final List<Repository> repositories = new LinkedList<>();

    public LocalResolver( ResolverSettings settings )
    {
        for ( String localRepoDir : settings.getLocalRepositories() )
        {
            Repository repo = new SingletonRepository( new File( localRepoDir ), RepositoryType.MAVEN );
            repositories.add( repo );
        }
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        Artifact artifact = request.getArtifact();

        for ( Repository repo : repositories )
        {
            File artifactFile = repo.getArtifactPath( artifact );
            if ( artifactFile != null )
                return new DefaultResolutionResult( artifactFile, repo );
        }

        return new DefaultResolutionResult();
    }
}
