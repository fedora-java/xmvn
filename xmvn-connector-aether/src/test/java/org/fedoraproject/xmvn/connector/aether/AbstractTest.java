/*-
 * Copyright (c) 2014-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.aether;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractTest
{
    private PlexusContainer container;

    public PlexusContainer getContainer()
    {
        return container;
    }

    /**
     * Test if XMvn WorkspaceReader component can be loaded by Sisu Plexus shim.
     * 
     * @throws Exception
     */
    @BeforeEach
    public void setupPlexusContainer()
        throws Exception
    {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setAutoWiring( true );
        config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
        container = new DefaultPlexusContainer( config );
    }

    public <T> T lookup( Class<T> role )
        throws Exception
    {
        return container.lookup( role );
    }

    public <T> T lookup( Class<T> role, String hint )
        throws Exception
    {
        return container.lookup( role, hint );
    }
}
