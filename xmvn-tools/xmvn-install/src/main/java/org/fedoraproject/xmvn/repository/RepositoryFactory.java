/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import java.util.Properties;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author Mikolaj Izdebski
 */
public interface RepositoryFactory
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
    Repository getInstance( Xpp3Dom filter, Properties properties, Xpp3Dom configuration );

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
    Repository getInstance( Xpp3Dom filter, Properties properties, Xpp3Dom configuration, String namespace );
}
