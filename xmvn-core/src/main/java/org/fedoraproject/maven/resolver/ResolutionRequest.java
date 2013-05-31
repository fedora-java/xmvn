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
package org.fedoraproject.maven.resolver;

import org.fedoraproject.maven.model.Artifact;

/**
 * Specifies parameters of artifact resolution.
 * 
 * @author Mikolaj Izdebski
 */
public interface ResolutionRequest
{
    /**
     * Get artifact which resolution is requested.
     * 
     * @return artifact which resolution is requested
     */
    Artifact getArtifact();

    /**
     * Determine whether information about artifact provider should be included in resolution result.
     * <p>
     * Artifact provider is name of system package providing requested artifact.
     * 
     * @return whether information about artifact provider should be included in resolution result
     */
    boolean isProviderNeeded();
}
