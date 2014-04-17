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
package org.fedoraproject.xmvn.tools.install.impl;

import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.repository.RepositoryPath;

@Named
@Singleton
public class DefaultArtifactInstaller
    implements ArtifactInstaller
{
    private final Logger logger = LoggerFactory.getLogger( DefaultArtifactInstaller.class );

    @Inject
    private RepositoryConfigurator repositoryConfigurator;

    @Override
    public void install( JavaPackage targetPackage, ArtifactMetadata am, PackagingRule rule )
        throws ArtifactInstallationException
    {
        Artifact artifact =
            new DefaultArtifact( am.getGroupId(), am.getArtifactId(), am.getExtension(), am.getClassifier(),
                                 am.getVersion() );

        logger.info( "Installing artifact {}", artifact );

        Repository repo = repositoryConfigurator.configureRepository( "install" );
        if ( repo == null )
            throw new ArtifactInstallationException( "Unable to configure installation repository" );

        RepositoryPath repoPath = repo.getPrimaryArtifactPath( artifact );
        if ( repoPath == null )
            throw new ArtifactInstallationException( "Installation repository is incapable of holding artifact "
                + artifact );

        // Artifact path
        File artifactFile = new RegularFile( repoPath.getPath(), Paths.get( am.getPath() ) );
        targetPackage.addFile( artifactFile );
        am.setPath( Paths.get( "/" ).resolve( artifactFile.getTargetPath() ).toString() );

        // Namespace
        am.setNamespace( repoPath.getRepository().getNamespace() );

        // UUID
        am.setUuid( UUID.randomUUID().toString() );

        // Compat version
        for ( String version : rule.getVersions() )
            am.addCompatVersion( version );

        // Aliases
        for ( org.fedoraproject.xmvn.config.Artifact aliasArtifact : rule.getAliases() )
        {
            ArtifactAlias alias = new ArtifactAlias();
            alias.setGroupId( aliasArtifact.getGroupId() );
            alias.setArtifactId( aliasArtifact.getArtifactId() );
            alias.setExtension( aliasArtifact.getExtension() );
            alias.setClassifier( aliasArtifact.getClassifier() );
            am.addAlias( alias );
        }

        targetPackage.getMetadata().addArtifact( am );
    }
}
