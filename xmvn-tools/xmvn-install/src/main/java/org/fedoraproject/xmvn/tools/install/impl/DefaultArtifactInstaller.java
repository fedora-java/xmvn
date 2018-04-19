/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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

import static org.fedoraproject.xmvn.tools.install.JarUtils.containsNativeCode;
import static org.fedoraproject.xmvn.tools.install.JarUtils.injectManifest;
import static org.fedoraproject.xmvn.tools.install.JarUtils.usesNativeCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.repository.ArtifactContext;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.repository.impl.DefaultRepositoryConfigurator;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.Directory;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.JavaPackage;
import org.fedoraproject.xmvn.tools.install.RegularFile;
import org.fedoraproject.xmvn.tools.install.SymbolicLink;

/**
 * @author Mikolaj Izdebski
 */
class DefaultArtifactInstaller
    implements ArtifactInstaller
{
    private final Logger logger = LoggerFactory.getLogger( DefaultArtifactInstaller.class );

    private final RepositoryConfigurator repositoryConfigurator;

    public DefaultArtifactInstaller( Configurator configurator )
    {
        this( new DefaultRepositoryConfigurator( configurator ) );
    }

    DefaultArtifactInstaller( RepositoryConfigurator repositoryConfigurator )
    {
        this.repositoryConfigurator = repositoryConfigurator;
    }

    @Override
    public void install( JavaPackage targetPackage, ArtifactMetadata am, PackagingRule rule, String basePackageName, String repositoryId )
        throws ArtifactInstallationException
    {
        Artifact artifact = am.toArtifact();

        // Handle native JARs/WARs etc
        Path artifactPath = Paths.get( am.getPath() );
        if ( usesNativeCode( artifactPath ) || containsNativeCode( artifactPath ) )
            am.getProperties().setProperty( "native", "true" );

        // Inject Javapackages manifests
        injectManifest( artifactPath, artifact );

        Map<String, String> properties = new LinkedHashMap<>();
        for ( String name : am.getProperties().stringPropertyNames() )
            properties.put( name, am.getProperties().getProperty( name ) );

        logger.info( "Installing artifact {}", artifact );

        Repository repo = repositoryConfigurator.configureRepository( repositoryId );
        if ( repo == null )
            throw new ArtifactInstallationException( "Unable to configure installation repository: " + repositoryId );

        Set<Path> basePaths = new LinkedHashSet<>();
        for ( String fileName : rule.getFiles() )
            basePaths.add( Paths.get( fileName ) );
        if ( basePaths.isEmpty() )
            basePaths.add( Paths.get( basePackageName ).resolve( artifact.getArtifactId() ) );

        Set<Path> relativePaths = new LinkedHashSet<>();
        Set<Path> absolutePaths = new LinkedHashSet<>();

        for ( Path path : basePaths )
        {
            if ( path.isAbsolute() )
                absolutePaths.add( path );
            else
                relativePaths.add( path );
        }
        if ( relativePaths.isEmpty() )
            throw new RuntimeException( "At least one non-absolute file must be specified for artifact " + artifact );

        String installedVersion = rule.getVersions().isEmpty() ? null : rule.getVersions().iterator().next();
        Artifact versionedArtifact = artifact.setVersion( installedVersion );
        ArtifactContext context = new ArtifactContext( versionedArtifact, properties );
        List<Path> repoPaths = new ArrayList<>();
        for ( Path path : relativePaths )
        {
            Path repoPath = repo.getPrimaryArtifactPath( versionedArtifact, context, path.toString() );
            if ( repoPath == null )
                throw new ArtifactInstallationException( "Installation repository is incapable of holding artifact "
                    + versionedArtifact );
            repoPaths.add( repoPath );

            Set<Path> repoRoots = repo.getRootPaths();
            for ( Path dir = repoPath.getParent(); dir != null && !repoRoots.contains( dir ); dir = dir.getParent() )
                targetPackage.addFileIfNotExists( new Directory( dir ) );
        }
        Iterator<Path> repoPathIterator = repoPaths.iterator();

        // Artifact path
        File artifactFile = new RegularFile( repoPathIterator.next(), artifactPath );
        targetPackage.addFile( artifactFile );
        Path primaryPath = Paths.get( "/" ).resolve( artifactFile.getTargetPath() );
        am.setPath( primaryPath.toString() );

        // Relative symlinks
        while ( repoPathIterator.hasNext() )
        {
            File symlink = new SymbolicLink( repoPathIterator.next(), primaryPath );
            targetPackage.addFile( symlink );
        }

        // Absolute symlinks
        for ( Path path : absolutePaths )
        {
            StringBuilder sb = new StringBuilder( Paths.get( "/" ).relativize( path ).toString() );
            if ( !versionedArtifact.getVersion().equals( Artifact.DEFAULT_VERSION ) )
                sb.append( '-' ).append( versionedArtifact.getVersion() );
            if ( versionedArtifact.getClassifier() != null && !versionedArtifact.getClassifier().isEmpty() )
                sb.append( '-' ).append( versionedArtifact.getClassifier() );
            if ( !versionedArtifact.getExtension().equals( Artifact.DEFAULT_VERSION ) )
                sb.append( '.' ).append( versionedArtifact.getExtension() );
            File symlink = new SymbolicLink( Paths.get( sb.toString() ), primaryPath );
            targetPackage.addFile( symlink );
        }

        // Namespace
        am.setNamespace( repo.getNamespace() );

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

    @Override
    public void postInstallation()
    {
        // Nothing to do
    }
}
