/*-
 * Copyright (c) 2014-2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository;

import java.util.Collections;
import java.util.Map;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactContext
{
    private final Artifact artifact;

    private final Map<String, String> properties;

    public ArtifactContext( Artifact artifact )
    {
        this.artifact = artifact;
        properties = Collections.emptyMap();
    }

    public ArtifactContext( Artifact artifact, Map<String, String> properties )
    {
        this.artifact = artifact;
        this.properties = properties;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public String getProperty( String key )
    {
        return properties.get( key );
    }

    @Override
    public boolean equals( Object rhs )
    {
        return rhs != null && getClass() == rhs.getClass() && artifact.equals( ( (ArtifactContext) rhs ).artifact )
            && properties.equals( ( (ArtifactContext) rhs ).properties );
    }

    @Override
    public int hashCode()
    {
        return artifact.hashCode() ^ properties.hashCode();
    }
}
