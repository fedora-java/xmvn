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
package org.fedoraproject.maven.resolver;

import java.io.File;

import org.eclipse.aether.artifact.Artifact;

/**
 * Resolves artifacts from system repositories configured in {@code <resolverSettings>} in XMvn configuration.
 * 
 * @author Mikolaj Izdebski
 */
public interface Resolver
{
    /**
     * Resolve artifacts from system repositories configured in {@code <resolverSettings>} in XMvn configuration.
     * 
     * @param request parameters of artifact resolution
     * @return results of artifact resolution, never {@code null}
     */
    ResolutionResult resolve( ResolutionRequest request );

    /**
     * Resolve artifacts from system repositories configured in {@code <resolverSettings>} in XMvn configuration.
     * <p>
     * This method is deprecated. Clients should use {@link #resolve(ResolutionRequest)} instead.
     * 
     * @param artifact artifact which resolution is requested
     * @return resolved artifact file or {@code null} if requested artifact could not be resolved
     */
    @Deprecated
    File resolve( Artifact artifact );

    /**
     * Resolve artifacts from system repositories configured in {@code <resolverSettings>} in XMvn configuration.
     * <p>
     * This method is deprecated. Clients should use {@link #resolve(ResolutionRequest)} instead.
     * 
     * @param groupId groupId of artifact which resolution is requested
     * @param artifactId artifactId of artifact which resolution is requested
     * @param version version of artifact which resolution is requested
     * @param extension extension of artifact which resolution is requested
     * @return resolved artifact file or {@code null} if requested artifact could not be resolved
     */
    @Deprecated
    File resolve( String groupId, String artifactId, String version, String extension );
}
