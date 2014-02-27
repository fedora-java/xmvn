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
package org.fedoraproject.xmvn.tools.install.impl.p2;

import org.fedoraproject.xmvn.tools.install.impl.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.impl.Package;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractP2Installer
    implements ArtifactInstaller
{
    protected P2RepoDescriptor getDescriptor( Package pkg, String packageName )
    {
        P2RepoDescriptor descriptor = (P2RepoDescriptor) pkg.getProperty( "p2.request" );

        if ( descriptor == null )
        {
            String prefix = packageName.replaceAll( "^eclipse-", "" );
            descriptor = new P2RepoDescriptor();
            descriptor.setTargetPackage( prefix + pkg.getSuffix() );
            pkg.setProperty( "p2.request", descriptor );
            pkg.addPreInstallHook( new P2InstallHook( descriptor ) );
        }

        return descriptor;
    }
}
