/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Named( "pom/effective" )
public class EffectivePomInstaller
    implements ArtifactInstaller
{
    @Override
    public void installArtifact( Package pkg, Artifact artifact, List<Artifact> aliases, List<Artifact> jppArtifacts )
        throws IOException
    {
        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        pkg.addFile( artifact.getFile().toPath(), primaryJppArtifact.getFile().toPath(), 0644 );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            Path symlink = jppSymlinkArtifact.getFile().toPath();
            pkg.addSymlink( symlink, primaryJppArtifact.getFile().toPath() );
        }

        pkg.addUserArtifact( artifact );
    }
}
