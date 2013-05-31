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

import java.io.File;

/**
 * Provides access to results of artifact resolution.
 * 
 * @author Mikolaj Izdebski
 */
public interface ResolutionResult
{
    /**
     * Get resolved artifact file.
     * 
     * @return artifact file or {@code null} if requested artifact could not be resolved
     */
    File getArtifactFile();

    /**
     * Get name of system package providing requested artifact.
     * 
     * @return name of system package providing requested artifact or {@code null} if information about artifact
     *         provider is not available
     */
    String getProvider();
}
