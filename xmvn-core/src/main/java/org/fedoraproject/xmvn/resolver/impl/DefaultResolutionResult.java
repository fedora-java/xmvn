/*-
 * Copyright (c) 2013-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.nio.file.Path;

import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * @author Mikolaj Izdebski
 */
class DefaultResolutionResult
    implements ResolutionResult
{
    private final Path artifactPath;

    private String provider;

    private String compatVersion;

    private String namespace;

    public DefaultResolutionResult()
    {
        this( null );
    }

    public DefaultResolutionResult( Path artifactPath )
    {
        this( artifactPath, null );
    }

    public DefaultResolutionResult( Path artifactPath, String namespace )
    {
        this.artifactPath = artifactPath;
        this.namespace = namespace;
    }

    @Override
    public Path getArtifactPath()
    {
        return artifactPath;
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
    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }
}
