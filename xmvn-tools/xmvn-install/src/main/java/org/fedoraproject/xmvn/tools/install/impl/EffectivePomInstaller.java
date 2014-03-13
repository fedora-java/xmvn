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

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;

/**
 * @author Mikolaj Izdebski
 */
@Named( "pom/effective" )
public class EffectivePomInstaller
    implements ArtifactInstaller
{
    private final Logger logger = LoggerFactory.getLogger( EffectivePomInstaller.class );

    private final RepositoryConfigurator repositoryConfigurator;

    @Inject
    public EffectivePomInstaller( RepositoryConfigurator repositoryConfigurator )
    {
        this.repositoryConfigurator = repositoryConfigurator;
    }

    @Override
    public void installArtifact( Package pkg, Artifact artifact, PackagingRule rule, String packageName )
        throws IOException
    {
        Repository repo = repositoryConfigurator.configureRepository( "install-effective-pom" );

        List<Artifact> jppArtifacts = DefaultInstaller.getJppArtifacts( artifact, rule, packageName, repo );
        if ( jppArtifacts == null )
        {
            logger.error( "No suitable repository found to store POM artifact {}", artifact );
            throw new RuntimeException( "No suitable repository found to store POM artifact" );
        }

        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        pkg.addFile( artifact.getPath(), primaryJppArtifact.getPath(), 0644 );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            Path symlink = jppSymlinkArtifact.getPath();
            pkg.addSymlink( symlink, primaryJppArtifact.getPath() );
        }

        pkg.addUserArtifact( artifact );
    }
}
