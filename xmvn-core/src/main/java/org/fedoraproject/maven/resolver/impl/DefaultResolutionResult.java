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
package org.fedoraproject.maven.resolver.impl;

import java.io.File;

import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.resolver.ResolutionResult;

/**
 * @author Mikolaj Izdebski
 */
class DefaultResolutionResult
    implements ResolutionResult
{
    private final File artifactFile;

    private String provider;

    private String compatVersion;

    private Repository repository;

    public DefaultResolutionResult()
    {
        this( null );
    }

    public DefaultResolutionResult( File artifactFile )
    {
        this( artifactFile, null );
    }

    public DefaultResolutionResult( File artifactFile, Repository repository )
    {
        this.artifactFile = artifactFile;
        this.repository = repository;
    }

    @Override
    public File getArtifactFile()
    {
        return artifactFile;
    }

    @Override
    public String getProvider()
    {
        return provider;
    }

    public void setProvider( String provider )
    {
        this.provider = provider;
    }

    @Override
    public String getCompatVersion()
    {
        return compatVersion;
    }

    public void setCompatVersion( String compatVersion )
    {
        this.compatVersion = compatVersion;
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
