/*-
 * Copyright (c) 2012-2023 Red Hat, Inc.
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

import java.nio.file.Path;
import java.util.Set;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * Repository of artifacts.
 * <p>
 * Repository is a container holding repositories. Unlike in case of Maven repositories, artifacts in XMvn repository
 * don't necessarily need to have unique paths -- one artifact can be stored in one of multiple locations. Methods or
 * {@code Repository} interface support multiple artifact paths.
 * 
 * @author Mikolaj Izdebski
 */
public interface Repository
{
    /**
     * Obtain the preferred path to given artifact in this repository.
     * <p>
     * Returned path is relative to the repository base.
     * 
     * @param artifact
     * @param context TODO
     * @param pattern TODO
     * @return preferred artifact path
     */
    Path getPrimaryArtifactPath( Artifact artifact, ArtifactContext context, String pattern );

    Set<Path> getRootPaths();

    String getNamespace();
}
