/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.repository.WorkspaceReader;
import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class PlexusTest
{
    /**
     * Test if XMvn WorkspaceReader component can be loaded by Sisu Plexus shim.
     * 
     * @throws Exception
     */
    @Test
    public void testPlexusComponentLookup()
        throws Exception
    {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setAutoWiring( true );
        config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
        PlexusContainer container = new DefaultPlexusContainer( config );
        WorkspaceReader component = container.lookup( WorkspaceReader.class, "ide" );
        assertNotNull( component );
        assertEquals( XMvnWorkspaceReader.class, component.getClass() );
    }
}
