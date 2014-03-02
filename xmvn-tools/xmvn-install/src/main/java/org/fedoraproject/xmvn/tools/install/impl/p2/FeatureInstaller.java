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

import java.io.IOException;

import javax.inject.Named;

import org.eclipse.aether.artifact.Artifact;

import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.tools.install.impl.Package;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "jar/eclipse-feature" )
public class FeatureInstaller
    extends AbstractP2Installer
{
    @Override
    public void installArtifact( Package pkg, Artifact artifact, PackagingRule rule, String packageName )
        throws IOException
    {
        P2RepoDescriptor request = getDescriptor( pkg, packageName );
        request.addFeature( artifact.getFile().toPath() );
    }
}
