/*-
 * Copyright (c) 2013-2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Specifies parameters of artifact resolution.
 * 
 * @author Mikolaj Izdebski
 */
public class ResolutionRequest
{
    private Artifact artifact;

    private boolean isProviderNeeded;

    private boolean isPersistentFileNeeded;

    public ResolutionRequest()
    {
    }

    public ResolutionRequest( Artifact artifact )
    {
        this.artifact = artifact;
    }

    /**
     * Get artifact which resolution is requested.
     * 
     * @return artifact which resolution is requested
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Set artifact which resolution is requested.
     * 
     * @param artifact artifact which resolution is requested
     */
    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    /**
     * Determine whether information about artifact provider should be included in resolution result.
     * <p>
     * Artifact provider is name of system package providing requested artifact.
     * 
     * @return whether information about artifact provider should be included in resolution result
     */
    public boolean isProviderNeeded()
    {
        return isProviderNeeded;
    }

    /**
     * Set whether information about artifact provider should be included in resolution result.
     * <p>
     * Artifact provider is name of system package providing requested artifact.
     * 
     * @param isProviderNeeded whether information about artifact provider should be included in resolution result
     */
    public void setProviderNeeded( boolean isProviderNeeded )
    {
        this.isProviderNeeded = isProviderNeeded;
    }

    /**
     * Determine whether resolved artifact file should be persistent or not.
     * <p>
     * Non-persistent files may be removed during JVM shutdown, while persistent files are not cleaned up during JVM
     * shutdown.
     * 
     * @return whether resolved artifact file should be persistent
     */
    public boolean isPersistentFileNeeded()
    {
        return isPersistentFileNeeded;
    }

    /**
     * Set whether resolved artifact file should be persistent or not.
     * <p>
     * Non-persistent files may be removed during JVM shutdown, while persistent files are not cleaned up during JVM
     * shutdown.
     * 
     * @param isPersistentFileNeeded
     */
    public void setPersistentFileNeeded( boolean isPersistentFileNeeded )
    {
        this.isPersistentFileNeeded = isPersistentFileNeeded;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( artifact == null ? 0 : artifact.hashCode() );
        result = prime * result + ( isProviderNeeded ? 1231 : 1237 );
        result = prime * result + ( isPersistentFileNeeded ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        ResolutionRequest other = (ResolutionRequest) obj;
        if ( artifact == null )
        {
            if ( other.artifact != null )
            {
                return false;
            }
        }
        else if ( !artifact.equals( other.artifact ) )
        {
            return false;
        }
        if ( isProviderNeeded != other.isProviderNeeded )
        {
            return false;
        }
        return isPersistentFileNeeded == other.isPersistentFileNeeded;
    }

    @Override
    public String toString()
    {
        return ResolutionRequest.class + "{artifact=" + artifact + ",isProviderNeeded=" + isProviderNeeded
            + ",isPersistentFileNeeded=" + isPersistentFileNeeded + "}";
    }
}
