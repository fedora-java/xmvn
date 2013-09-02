/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.installer.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Writer;
import org.fedoraproject.maven.installer.InstallationRequest;
import org.fedoraproject.maven.installer.InstallationResult;
import org.fedoraproject.maven.installer.Installer;
import org.fedoraproject.maven.model.ModelFormatException;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.RepositoryPath;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Installer.class )
public class DefaultInstaller
    implements Installer
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private RepositoryConfigurator repositoryConfigurator;

    @Requirement
    private Resolver resolver;

    private Repository installRepo;

    private Repository rawPomRepo;

    private Repository effectivePomRepo;

    private InstallerSettings settings;

    private Configuration configuration;

    private Map<String, Package> packages;

    private PackagingRule ruleForArtifact( Artifact artifact )
    {
        String stereotype = ArtifactUtils.getStereotype( artifact );
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();

        return configuration.createEffectivePackagingRule( stereotype, groupId, artifactId, extension, classifier,
                                                           version );
    }

    private List<Artifact> getAliasArtifacts( PackagingRule rule )
    {
        List<Artifact> aliasArtifacts = new ArrayList<>();

        for ( org.fedoraproject.maven.config.Artifact alias : rule.getAliases() )
        {
            Artifact aliasArtifact =
                new DefaultArtifact( alias.getGroupId(), alias.getArtifactId(), alias.getClassifier(),
                                     alias.getExtension(), alias.getVersion() );
            aliasArtifact = ArtifactUtils.setStereotype( aliasArtifact, alias.getStereotype() );
            aliasArtifacts.add( aliasArtifact );
        }

        return aliasArtifacts;
    }

    private Package getTargetPackageForArtifact( Artifact artifact, PackagingRule rule )
        throws IOException
    {
        String packageName = rule.getTargetPackage();
        if ( StringUtils.isEmpty( packageName ) )
            packageName = Package.MAIN;
        Package pkg = packages.get( packageName );

        if ( pkg == null )
        {
            pkg = new Package( packageName, settings, logger );
            packages.put( packageName, pkg );
        }

        if ( !pkg.isInstallable() )
            return null;

        if ( logger.isDebugEnabled() )
        {
            try (StringWriter buffer = new StringWriter())
            {
                Configuration wrapperConfiguration = new Configuration();
                wrapperConfiguration.addArtifactManagement( rule );
                ConfigurationXpp3Writer configurationWriter = new ConfigurationXpp3Writer();
                configurationWriter.write( buffer, wrapperConfiguration );
                logger.debug( "Effective packaging rule for " + artifact + ":\n" + buffer );
            }
        }

        return pkg;
    }

    private List<Artifact> getJppArtifacts( Artifact artifact, PackagingRule rule )
    {
        Set<Path> basePaths = new LinkedHashSet<>();
        for ( String fileName : rule.getFiles() )
            basePaths.add( Paths.get( fileName ) );
        if ( basePaths.isEmpty() )
            basePaths.add( Paths.get( settings.getPackageName() + "/" + artifact.getArtifactId() ) );

        Set<String> versions = new LinkedHashSet<>();
        for ( String version : rule.getVersions() )
            versions.add( version );
        if ( versions.isEmpty() )
            versions.add( "SYSTEM" );

        List<Artifact> jppArtifacts = new ArrayList<>();

        for ( Path basePath : basePaths )
        {
            if ( basePath.isAbsolute() )
                throw new RuntimeException( "Absolute JPP artifact paths are not supported: artifact: " + artifact
                    + ", path: " + basePath );

            Path jppName = basePath.getFileName();
            Path jppGroup = Paths.get( "JPP" );
            if ( basePath.getParent() != null )
                jppGroup = jppGroup.resolve( basePath.getParent() );

            for ( String version : versions )
            {
                Artifact jppArtifact =
                    new DefaultArtifact( jppGroup.toString(), jppName.toString(), artifact.getClassifier(),
                                         artifact.getExtension(), version );

                RepositoryPath jppArtifactPath = installRepo.getPrimaryArtifactPath( jppArtifact );
                if ( jppArtifactPath == null )
                    return null;
                jppArtifact = jppArtifact.setFile( jppArtifactPath.getPath().toFile() );
                jppArtifact = ArtifactUtils.setScope( jppArtifact, jppArtifactPath.getRepository().getNamespace() );

                jppArtifacts.add( jppArtifact );
            }
        }

        return jppArtifacts;
    }

    private void installArtifact( Package pkg, Artifact artifact, List<Artifact> aliases, List<Artifact> jppArtifacts )
        throws IOException
    {
        logger.info( "===============================================" );
        logger.info( "SOURCE ARTIFACT:" );
        logger.info( "    groupId: " + artifact.getGroupId() );
        logger.info( " artifactId: " + artifact.getArtifactId() );
        logger.info( "  extension: " + artifact.getExtension() );
        logger.info( " classifier: " + artifact.getClassifier() );
        logger.info( "    version: " + artifact.getVersion() );
        logger.info( " stereotype: " + ArtifactUtils.getStereotype( artifact ) );
        logger.info( "  namespace: " + ArtifactUtils.getScope( artifact ) );
        logger.info( "       file: " + artifact.getFile() );
        for ( Artifact jppArtifact : jppArtifacts )
        {
            logger.info( "-----------------------------------------------" );
            logger.info( "TARGET ARTIFACT:" );
            logger.info( "    groupId: " + jppArtifact.getGroupId() );
            logger.info( " artifactId: " + jppArtifact.getArtifactId() );
            logger.info( "  extension: " + jppArtifact.getExtension() );
            logger.info( " classifier: " + jppArtifact.getClassifier() );
            logger.info( "    version: " + jppArtifact.getVersion() );
            logger.info( " stereotype: " + ArtifactUtils.getStereotype( jppArtifact ) );
            logger.info( "  namespace: " + ArtifactUtils.getScope( jppArtifact ) );
            logger.info( "       file: " + jppArtifact.getFile() );
        }
        logger.info( "===============================================" );

        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        pkg.addFile( artifact.getFile().toPath(), primaryJppArtifact.getFile().toPath(), 0644 );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            Path symlink = jppSymlinkArtifact.getFile().toPath();
            pkg.addSymlink( symlink, primaryJppArtifact.getFile().toPath() );
        }

        for ( Artifact jppArtifact : jppArtifacts )
        {
            String namespace = ArtifactUtils.getScope( jppArtifact );
            pkg.getMetadata().addMapping( ArtifactUtils.setScope( artifact, namespace ), jppArtifact );
            for ( Artifact alias : aliases )
                pkg.getMetadata().addMapping( ArtifactUtils.setScope( alias, namespace ), jppArtifact );
        }
    }

    private void generateDevelRequires( Package pkg, Artifact artifact )
        throws IOException, ModelFormatException
    {
        // TODO implement
    }

    private void generateUserRequires( Package pkg, Artifact artifact )
        throws IOException
    {
        // TODO implement
    }

    private void installPomFiles( Package pkg, Artifact artifact, List<Artifact> jppArtifacts )
    {
        Artifact jppArtifact = jppArtifacts.iterator().next();
        Artifact jppPomArtifact =
            new DefaultArtifact( jppArtifact.getGroupId(), jppArtifact.getArtifactId(), jppArtifact.getClassifier(),
                                 "pom", jppArtifact.getVersion() );

        String rawModelPath = artifact.getProperty( "xmvn.installer.rawModelPath", null );
        if ( rawModelPath != null && settings.isEnableRawPoms() )
        {
            Path target = rawPomRepo.getPrimaryArtifactPath( jppPomArtifact ).getPath();
            pkg.addFile( Paths.get( rawModelPath ), target, 0644 );
        }

        String effectiveModelPath = artifact.getProperty( "xmvn.installer.effectiveModelPath", null );
        if ( effectiveModelPath != null && settings.isEnableEffectivePoms() )
        {
            Path target = effectivePomRepo.getPrimaryArtifactPath( jppPomArtifact ).getPath();
            pkg.addFile( Paths.get( effectiveModelPath ), target, 0644 );
        }
    }

    private void installArtifact( Artifact artifact )
        throws IOException, ModelFormatException
    {
        String rawModelPath = artifact.getProperty( "xmvn.installer.rawModelPath", null );
        boolean isAttachedArtifact = rawModelPath == null;
        boolean isPomArtifact = artifact.getFile() == null;
        if ( isAttachedArtifact && isPomArtifact )
            throw new RuntimeException( "Attached artifact cannot be POM artifact: " + artifact );
        if ( isPomArtifact && !artifact.getExtension().equals( "pom" ) )
            throw new RuntimeException( "POM artifact has extension different from 'pom': " + artifact.getExtension() );

        PackagingRule rule = ruleForArtifact( artifact );

        Package pkg = getTargetPackageForArtifact( artifact, rule );
        if ( pkg == null )
            return;

        if ( isPomArtifact )
        {
            artifact = artifact.setFile( Paths.get( rawModelPath ).toFile() );
            artifact = ArtifactUtils.setStereotype( artifact, "pom" );
        }

        List<Artifact> jppArtifacts = getJppArtifacts( artifact, rule );
        if ( jppArtifacts == null )
        {
            logger.warn( "Skipping installation of artifact " + artifact
                + ": No suitable repository found to store the artifact in." );
            return;
        }

        List<Artifact> aliases = getAliasArtifacts( rule );
        installArtifact( pkg, artifact, aliases, jppArtifacts );

        if ( isPomArtifact )
        {
            generateDevelRequires( pkg, artifact );
        }
        else if ( !isAttachedArtifact )
        {
            pkg.setPureDevelPackage( false );
            installPomFiles( pkg, artifact, jppArtifacts );
            generateUserRequires( pkg, artifact );
        }
    }

    private void checkForUnmatchedRules( List<PackagingRule> artifactManagement )
    {
        boolean unmatchedRuleFound = false;

        for ( PackagingRule rule : artifactManagement )
        {
            if ( !rule.isOptional() && !rule.isMatched() )
            {
                unmatchedRuleFound = true;
                org.fedoraproject.maven.config.Artifact glob = rule.getArtifactGlob();
                String globString =
                    glob.getGroupId() + ":" + glob.getArtifactId() + ":" + glob.getExtension() + ":"
                        + glob.getClassifier() + ":" + glob.getVersion();
                logger.error( "Unmatched packaging rule: " + globString );
            }
        }

        if ( unmatchedRuleFound )
        {
            throw new RuntimeException( "There are unmatched packaging rules" );
        }
    }

    @Override
    public InstallationResult install( InstallationRequest request )
    {
        installRepo = repositoryConfigurator.configureRepository( "install" );
        rawPomRepo = repositoryConfigurator.configureRepository( "install-raw-pom" );
        effectivePomRepo = repositoryConfigurator.configureRepository( "install-effective-pom" );

        configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        packages = new TreeMap<>();

        Package mainPackage = new Package( Package.MAIN, settings, logger );
        packages.put( Package.MAIN, mainPackage );

        Set<Artifact> artifactSet = request.getArtifacts();

        try
        {
            for ( Artifact artifact : artifactSet )
                installArtifact( artifact );

            if ( request.isCheckForUnmatchedRules() )
                checkForUnmatchedRules( configuration.getArtifactManagement() );

            Path installRoot = Paths.get( settings.getInstallRoot() );
            org.fedoraproject.maven.installer.impl.Installer installer =
                new org.fedoraproject.maven.installer.impl.Installer( installRoot );

            for ( Package pkg : packages.values() )
                if ( pkg.isInstallable() )
                    pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to install project", e );
        }
        catch ( ModelFormatException e )
        {
            throw new RuntimeException( "Failed to install project", e );
        }

        return new DefaultInstallationResult();
    }
}
