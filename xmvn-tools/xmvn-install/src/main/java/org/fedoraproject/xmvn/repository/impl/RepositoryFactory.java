/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository.impl;

import java.util.Properties;

import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.Repository;

/**
 * @author Mikolaj Izdebski
 */
interface RepositoryFactory
{
    /**
     * Create a resolver instance configured with given set of properties and repository-specific XML configuration.
     * <p>
     * The meaning of properties and XML configuration is dependent on particular repository implementation.
     * 
     * @param filter
     * @param properties
     * @param configuration
     */
    Repository getInstance( Element filter, Properties properties, Element configuration );

    /**
     * Create a resolver instance configured with given set of properties and repository-specific XML configuration.
     * <p>
     * The meaning of properties and XML configuration is dependent on particular repository implementation.
     * 
     * @param filter
     * @param properties
     * @param configuration
     * @param namespace
     */
    Repository getInstance( Element filter, Properties properties, Element configuration, String namespace );
}
