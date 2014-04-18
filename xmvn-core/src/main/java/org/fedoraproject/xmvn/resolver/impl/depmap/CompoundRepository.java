/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl.depmap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.fedoraproject.xmvn.artifact.Artifact;

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
@Deprecated
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

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifacts, boolean ignoreType )
    {
        List<RepositoryPath> paths = new ArrayList<>();
        for ( Repository repository : slaveRepositories )
        {
            for ( RepositoryPath path : repository.getArtifactPaths( artifacts, ignoreType ) )
            {
                DefaultRepositoryPath newPath = new DefaultRepositoryPath( path );
                if ( prefix != null )
                    newPath.setPath( prefix.resolve( path.getPath() ) );
                paths.add( newPath );
            }
        }
        return Collections.unmodifiableList( paths );
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType )
    {
        Iterator<RepositoryPath> it = getArtifactPaths( artifact, ignoreType ).iterator();
        return it.hasNext() ? it.next() : null;
    }
}
