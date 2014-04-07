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
package org.fedoraproject.xmvn.deployer;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class DeploymentRequest
{
    private Artifact artifact;

    private final Map<Artifact, List<Artifact>> dependencies = new LinkedHashMap<>();

    private final Map<String, String> properties = new LinkedHashMap<>();

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public Map<Artifact, List<Artifact>> getDependencies()
    {
        return Collections.unmodifiableMap( dependencies );
    }

    public void addDependency( Artifact dependencyArtifact, Artifact... exclusions )
    {
        addDependency( dependencyArtifact, Arrays.asList( exclusions ) );
    }

    public void addDependency( Artifact dependencyArtifact, List<Artifact> exclusions )
    {
        dependencies.put( dependencyArtifact, Collections.unmodifiableList( exclusions ) );
    }

    public void removeDependency( Artifact dependencyArtifact )
    {
        dependencies.remove( dependencyArtifact );
    }

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap( properties );
    }

    public String getProperty( String key )
    {
        return properties.get( key );
    }

    public void addProperty( String key, String value )
    {
        properties.put( key, value );
    }

    public void removeProperty( String key )
    {
        properties.remove( key );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( artifact == null ) ? 0 : artifact.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        DeploymentRequest other = (DeploymentRequest) obj;
        if ( artifact == null )
        {
            if ( other.artifact != null )
                return false;
        }
        else if ( !artifact.equals( other.artifact ) )
            return false;
        return true;
    }
}
