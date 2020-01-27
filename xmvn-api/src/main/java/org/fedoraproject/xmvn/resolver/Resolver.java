/*-
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
}
