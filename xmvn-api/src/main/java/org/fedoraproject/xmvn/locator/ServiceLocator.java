/*-
 * Copyright (c) 2014-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.locator;

/**
 * A service locator for services provided by XMvn.
 * <p>
 * This service locator uses a separate class loader to load XMvn classes and all of its dependencies.
 * 
 * @author Mikolaj Izdebski
 */
public interface ServiceLocator
{
    /**
     * Load an instance of XMvn service.
     * 
     * @param role interface class identifying requested service
     * @return instance of XMvn service, never {@code null}.
     */
    <T> T getService( Class<T> role );
}
