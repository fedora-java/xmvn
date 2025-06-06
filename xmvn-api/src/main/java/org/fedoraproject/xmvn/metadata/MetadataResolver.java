/*-
 * Copyright (c) 2016-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata;

/**
 * Resolves artifact metadata from specified metadata repositories.
 *
 * @author Mikolaj Izdebski
 */
public interface MetadataResolver {
    /**
     * Resolve artifact metadata from metadata repositories specified in request.
     *
     * @param request parameters of metadata resolution
     * @return results of metadata resolution, never {@code null}
     */
    MetadataResult resolveMetadata(MetadataRequest request);
}
