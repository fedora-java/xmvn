/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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

/**
 * A service locator for services provided by XMvn.
 * <p>
 * This service locator uses a separate class loader to load XMvn classes and all of its dependencies.
 * 
 * @author Mikolaj Izdebski
 */
public final class ServiceLocatorFactory
{
    /**
     * Load an instance of XMvn service.
     * 
     * @return instance of XMvn service, never {@code null}.
     */
    public ServiceLocator createServiceLocator()
    {
        try
        {
            return (ServiceLocator) Class.forName( "org.fedoraproject.xmvn.locator.impl.DefaultServiceLocator" ).getConstructor().newInstance();
        }
        catch ( ReflectiveOperationException e )
        {
            throw new RuntimeException( "Unable to instantiate DefaultServiceLocator, "
                + "make sure that xmvn-core.jar is available on classpath", e );
        }
    }
}
