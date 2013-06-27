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
package org.fedoraproject.maven.repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Deprecated
public class DefaultAggregatorRepository
    implements Repository
{
    private final Collection<Repository> jarRepos = new LinkedList<>();

    private final Collection<Repository> pomRepos = new LinkedList<>();

    private final File root;

    private void initRepos( Collection<Repository> repos, Iterable<String> dirs, Repository layout )
    {
        for ( String repoPath : dirs )
        {
            // This is delibaretly broken, but will be removed...
        }
    }

    public DefaultAggregatorRepository( File root, ResolverSettings settings )
    {
        this.root = root;

        initRepos( jarRepos, settings.getJarRepositories(), new JppRepository() );
        initRepos( pomRepos, settings.getPomRepositories(), new FlatRepository() );
    }

    @Override
    public Path getArtifactPath( Artifact artifact )
    {
        Iterable<Repository> repos = artifact.isPom() ? pomRepos : jarRepos;
        for ( Repository repo : repos )
        {
            Path path = repo.getArtifactPath( artifact );
            if ( path != null && Files.exists( path ) )
                return path;
        }
        artifact = artifact.clearVersion();
        for ( Repository repo : repos )
        {
            Path path = repo.getArtifactPath( artifact );
            if ( path != null && Files.exists( path ) )
                return path;
        }

        return null;
    }

    @Override
    public void configure( Properties properties, Xpp3Dom configuration )
    {
        // COnfiguration is ignored. This class is deprecated.
    }
}
