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
package org.fedoraproject.xmvn.locator.impl;

import java.util.HashMap;
import java.util.Map;

import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.impl.DefaultConfigurator;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.impl.DefaultDeployer;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.impl.DefaultMetadataResolver;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.resolver.impl.DefaultResolver;

/**
 * Service locator for XMvn.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
public class DefaultServiceLocator
    implements ServiceLocator
{
    private final Map<Class<?>, Class<?>> knownServices = new HashMap<>();

    private final Map<Class<?>, Object> runningServices = new HashMap<>();

    public <T> void addService( Class<T> role, Class<? extends T> serviceProvider )
    {
        knownServices.put( role, serviceProvider );
    }

    public DefaultServiceLocator()
    {
        addService( Resolver.class, DefaultResolver.class );
        addService( Deployer.class, DefaultDeployer.class );
        addService( Configurator.class, DefaultConfigurator.class );
        addService( MetadataResolver.class, DefaultMetadataResolver.class );
    }

    private Object loadService( Class<?> role )
    {
        Class<?> implClass = knownServices.get( role );
        if ( implClass == null )
            return null;

        try
        {
            try
            {
                return implClass.getConstructor( ServiceLocator.class ).newInstance( this );
            }
            catch ( NoSuchMethodException e )
            {
                return implClass.getConstructor().newInstance();
            }
        }
        catch ( ReflectiveOperationException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public <T> T getService( Class<T> role )
    {
        if ( !runningServices.containsKey( role ) )
        {
            runningServices.put( role, loadService( role ) );
        }

        return role.cast( runningServices.get( role ) );
    }
}
