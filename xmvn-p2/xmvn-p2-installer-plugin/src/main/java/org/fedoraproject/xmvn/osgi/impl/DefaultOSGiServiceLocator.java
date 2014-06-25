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
package org.fedoraproject.xmvn.osgi.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.osgi.OSGiFramework;
import org.fedoraproject.xmvn.osgi.OSGiServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultOSGiServiceLocator
    implements OSGiServiceLocator
{
    private final OSGiFramework framework;

    @Inject
    public DefaultOSGiServiceLocator( OSGiFramework framework )
    {
        this.framework = framework;
    }

    @Override
    public <T> T getService( Class<T> clazz )
    {
        BundleContext context = framework.getBundleContext();

        ServiceReference<T> serviceReference = context.getServiceReference( clazz );

        if ( serviceReference == null )
            throw new RuntimeException( "OSGi service for " + clazz.getName() + " was not found" );

        return context.getService( serviceReference );
    }
}
