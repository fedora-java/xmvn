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

import java.util.List;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public interface DependencyMap
{
    boolean isEmpty();

    @Deprecated
    void addMapping( String groupId, String artifactId, String version, String jppGroupId, String jppArtifactId );

    void addMapping( Artifact from, Artifact to );

    /**
     * Compute a list of artifacts reachable from given start artifact in a transitive closure of dependency graph. The
     * list is in depth-first order.
     * 
     * @param artifact start point of depth-first search
     * @return list of artifacts to which given artifact can be mapped
     */
    List<Artifact> translate( Artifact artifact );

    /**
     * Find all artifacts somehow related to given artifact.
     * 
     * @param artifact artifact relatives of which are to be found
     * @return related set containing all artifacts reachable from given artifact and artifacts from which given
     *         artifact is reachable
     */
    Set<Artifact> relativesOf( Artifact artifact );
}
