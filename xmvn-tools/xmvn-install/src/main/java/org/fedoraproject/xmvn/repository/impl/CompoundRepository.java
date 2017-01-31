/*-
 * Copyright (c) 2013-2017 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository.impl;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.Repository;

/**
 * Compound repository.
 * <p>
 * This repository aggregates zero or more other repositories. The repositories are ordered by preference.
 * <p>
 * All requests are forwarded to repositories backing this compound repository. If no repositories are aggregated then
 * this repository is equivalent to empty repository.
 * 
 * @author Mikolaj Izdebski
 */
class CompoundRepository
    extends AbstractRepository
{
    private final Path prefix;

    private final List<Repository> slaveRepositories;

    public CompoundRepository( String namespace, Path prefix, List<Repository> slaveRepositories )
    {
        super( namespace );
        this.prefix = prefix;
        this.slaveRepositories = slaveRepositories;
    }

    private Path prefix( Path path )
    {
        return prefix == null ? path : prefix.resolve( path );
    }

    @Override
    public Path getPrimaryArtifactPath( Artifact artifact, ArtifactContext context, String pattern )
    {
        for ( Repository repository : slaveRepositories )
        {
            Path path = repository.getPrimaryArtifactPath( artifact, context, pattern );
            if ( path != null )
            {
                return prefix( path );
            }
        }

        return null;
    }

    @Override
    public Set<Path> getRootPaths()
    {
        Set<Path> rootPaths = new LinkedHashSet<>();

        for ( Repository repository : slaveRepositories )
        {
            for ( Path rootPath : repository.getRootPaths() )
            {
                rootPaths.add( prefix( rootPath ) );
            }
        }

        return Collections.unmodifiableSet( rootPaths );
    }
}
