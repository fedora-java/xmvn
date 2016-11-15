/*-
 * Copyright (c) 2016 Red Hat, Inc.
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

import com.google.inject.Binder;
import org.apache.maven.cli.internal.BootstrapCoreExtensionManager;
import org.apache.maven.internal.aether.DefaultRepositorySystemSessionFactory;
import org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

public class CoreExtensionsManagerTest
    extends InjectedTest
{
    @Override
    public void configure( Binder binder )
    {

        super.configure( binder );
        binder.bind( PluginDependenciesResolver.class ).toInstance( new DefaultPluginDependenciesResolver() );
        binder.bind( DefaultRepositorySystemSessionFactory.class ).toInstance( new DefaultRepositorySystemSessionFactory() );
    }

    @Test
    public void testInjection()
        throws Exception
    {
        lookup( BootstrapCoreExtensionManager.class );
    }

}
