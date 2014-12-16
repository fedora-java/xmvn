/*-
 * Copyright (c) 2014 Red Hat, Inc.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.guice.plexus.config.Strategies;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * Service locator for XMvn.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = XMvnServiceLocator.class, instantiationStrategy = Strategies.LOAD_ON_START )
public class XMvnServiceLocator
    implements Initializable
{
    private static XMvnServiceLocator instance;

    private static synchronized XMvnServiceLocator getInstance()
        throws PlexusContainerException, ComponentLookupException
    {
        if ( instance == null )
        {
            PlexusContainer container = new DefaultPlexusContainer();
            instance = container.lookup( XMvnServiceLocator.class );
        }

        return instance;
    }

    private final Map<String, Iterable<?>> knownServices = new HashMap<>();

    @Requirement
    private List<Resolver> resolvers;

    @Requirement
    private List<Deployer> deployers;

    private <T> void addService( Class<T> service, Iterable<T> serviceProviders )
    {
        knownServices.put( service.getCanonicalName(), serviceProviders );
    }

    @Override
    public void initialize()
    {
        addService( Resolver.class, resolvers );
        addService( Deployer.class, deployers );
    }

    public static Object getService( Class<?> role )
    {
        try
        {
            Iterator<?> iterator = getInstance().knownServices.get( role.getCanonicalName() ).iterator();
            return iterator.hasNext() ? iterator.next() : null;
        }
        catch ( PlexusContainerException | ComponentLookupException e )
        {
            throw new RuntimeException( "Plexus exception when trying to initialize XMvn service " + role, e );
        }
    }
}
