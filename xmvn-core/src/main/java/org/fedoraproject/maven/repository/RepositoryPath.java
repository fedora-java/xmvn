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
package org.fedoraproject.maven.repository;

import java.nio.file.Path;

/**
 * Artifact path within repository.
 * 
 * @author Mikolaj Izdebski
 */
public interface RepositoryPath
{
    /**
     * Return artifact path relative to highest-level repository root. Returned path includes all repository prefixes.
     * 
     * @return artifact path
     */
    Path getPath();

    /**
     * Get lowest-level repository that this path belongs to.
     * 
     * @return repository this path belongs to
     */
    Repository getRepository();
}
