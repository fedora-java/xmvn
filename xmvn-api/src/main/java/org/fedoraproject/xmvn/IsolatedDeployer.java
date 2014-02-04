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
package org.fedoraproject.xmvn;

import java.util.concurrent.Callable;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;

/**
 * @author Mikolaj Izdebski
 */
class IsolatedDeployer
    implements Deployer
{
    static
    {
        INSTANCE = new IsolatedDeployer();
    }

    static final Deployer INSTANCE;

    @Override
    public DeploymentResult deploy( final DeploymentRequest request )
    {
        try
        {
            return IsolatedXMvnServiceLocator.getRealm().execute( new Callable<DeploymentResult>()
            {
                @Override
                public DeploymentResult call()
                {
                    return IsolatedXMvnServiceLocator.getService( Deployer.class ).deploy( request );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
