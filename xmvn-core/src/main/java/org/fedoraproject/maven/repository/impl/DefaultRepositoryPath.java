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
package org.fedoraproject.maven.repository.impl;

import java.nio.file.Path;

import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryPath;

/**
 * @author Mikolaj Izdebski
 */
class DefaultRepositoryPath
    implements RepositoryPath
{
    private Path path;

    private Repository repository;

    public DefaultRepositoryPath()
    {
    }

    public DefaultRepositoryPath( Path path, Repository repository )
    {
        this.path = path;
        this.repository = repository;
    }

    public DefaultRepositoryPath( RepositoryPath path )
    {
        this( path.getPath(), path.getRepository() );
    }

    @Override
    public Path getPath()
    {
        return path;
    }

    public void setPath( Path path )
    {
        this.path = path;
    }

    @Override
    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }
}
