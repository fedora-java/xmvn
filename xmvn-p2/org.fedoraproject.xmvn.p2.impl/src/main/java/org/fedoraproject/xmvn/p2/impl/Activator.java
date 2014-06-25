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
package org.fedoraproject.xmvn.p2.impl;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Mikolaj Izdebski
 */
public class Activator
    implements BundleActivator
{
    private static BundleContext context;

    private static IProvisioningAgent agent;

    static BundleContext getContext()
    {
        return context;
    }

    static synchronized IProvisioningAgent getAgent()
        throws ProvisionException
    {
        if ( agent != null )
            return agent;

        ServiceReference<IProvisioningAgent> agentRef = context.getServiceReference( IProvisioningAgent.class );
        if ( agentRef != null )
        {
            agent = context.getService( agentRef );
            if ( agent != null )
                return agent;
        }

        ServiceReference<IProvisioningAgentProvider> providerRef =
            context.getServiceReference( IProvisioningAgentProvider.class );
        if ( providerRef == null )
            throw new RuntimeException( "No registered OSGi services for " + IProvisioningAgentProvider.class );

        try
        {
            IProvisioningAgentProvider provider = context.getService( providerRef );
            if ( provider == null )
                throw new RuntimeException( "Unable to get OSGi service for " + IProvisioningAgentProvider.class );

            agent = provider.createAgent( null );
            return agent;
        }
        finally
        {
            context.ungetService( providerRef );
        }
    }

    @Override
    public void start( BundleContext bundleContext )
        throws Exception
    {
        Activator.context = bundleContext;
    }

    @Override
    public void stop( BundleContext bundleContext )
        throws Exception
    {
        Activator.context = null;
    }
}
