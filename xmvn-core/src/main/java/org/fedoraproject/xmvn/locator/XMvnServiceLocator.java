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

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * Service locator for XMvn.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@EagerSingleton
public class XMvnServiceLocator
{
    private static XMvnServiceLocator instance;

    private static synchronized XMvnServiceLocator getInstance()
    {
        if ( instance == null )
        {
            ClassLoader classRealm = XMvnServiceLocator.class.getClassLoader();
            ClassSpace classSpace = new URLClassSpace( classRealm );
            Module spaceModule = new SpaceModule( classSpace, BeanScanning.CACHE );
            Module wireModule = new WireModule( spaceModule );
            Injector injector = Guice.createInjector( wireModule );
            instance = injector.getInstance( XMvnServiceLocator.class );
        }

        return instance;
    }

    private final Map<String, Iterable<?>> knownServices = new HashMap<>();

    private <T> void addService( Class<T> service, Iterable<T> serviceProviders )
    {
        knownServices.put( service.getCanonicalName(), serviceProviders );
    }

    @Inject
    public XMvnServiceLocator( List<Resolver> resolvers, List<Deployer> deployers )
    {
        addService( Resolver.class, resolvers );
        addService( Deployer.class, deployers );
    }

    public static Object getService( Class<?> role )
    {
        Iterator<?> iterator = getInstance().knownServices.get( role.getCanonicalName() ).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}
