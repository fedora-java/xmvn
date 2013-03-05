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
     * Returns default XMvn configuration.
     * <p>
     * Default configuration is minimal correct configuration that is embedded in XMvn itself. It doesn't contain any
     * customizations coming from configuration files. Default configuration can be useful as a base on which
     * configuration is built dynamically during runtime.
     * 
     * @return default configuration
     */
    Configuration getDefaultConfiguration();

    /**
     * Returns XMvn master configuration.
     * <p>
     * Master configuration is combination of multiple configuration files coming from various sources, merged in
     * certain way. See XMvn documentation for detailed information where master configuration is read from and how it's
     * merged.
     * 
     * @return master configuration
     */
    Configuration getConfiguration();

    /**
     * Dump the master configuration.
     * <p>
     * Configuration dumps are written through Plexus container default logger.
     */
    void dumpConfiguration();
}
