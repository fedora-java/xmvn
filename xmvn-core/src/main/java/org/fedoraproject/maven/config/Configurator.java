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
package org.fedoraproject.maven.config;

/**
 * Component that provides various XMvn configuration variants.
 * 
 * @author Mikolaj Izdebski
 */
public interface Configurator
{
    /**
     * Returns default XMvn configuration. This is effective configuration and hence it's always available.
     * 
     * @return default configuration
     */
    Configuration getDefaultConfiguration();

    /**
     * Returns effective system XMvn configuration. This configuration is always available.
     * 
     * @return effective system configuration
     */
    Configuration getSystemConfiguration();

    /**
     * Returns raw system XMvn configuration. This configuration may be unavailable, in which case <code>null</code> is
     * returned.
     * 
     * @return raw system configuration or <code>null</code> if not available
     */
    Configuration getRawSystemConfiguration();

    /**
     * Returns effective user XMvn configuration. This configuration is always available.
     * 
     * @return effective user configuration
     */
    Configuration getUserConfiguration();

    /**
     * Returns raw user XMvn configuration. This configuration may be unavailable, in which case <code>null</code> is
     * returned.
     * 
     * @return raw user configuration or <code>null</code> if not available
     */
    Configuration getRawUserConfiguration();

    /**
     * Returns effective reactor XMvn configuration. This configuration is always available.
     * 
     * @return effective reactor configuration
     */
    Configuration getReactorConfiguration();

    /**
     * Returns raw reactor XMvn configuration. This configuration may be unavailable, in which case <code>null</code> is
     * returned.
     * 
     * @return raw reactor configuration or <code>null</code> if not available
     */
    Configuration getRawReactorConfiguration();

    /**
     * Returns implicit XMvn configuration. This configuration is always available.
     * 
     * @return implicit configuration
     */
    Configuration getConfiguration();

    /**
     * Log information different kinds of configuration.
     * <p>
     * Configuration dumps are written through Plexus container default logger.
     */
    void dumpConfiguration();
}
