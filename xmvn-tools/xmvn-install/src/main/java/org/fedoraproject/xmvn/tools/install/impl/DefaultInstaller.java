/*-
 * Copyright (c) 2014-2020 Red Hat, Inc.
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
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.SkippedArtifactMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxWriter;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.InstallationResult;
import org.fedoraproject.xmvn.tools.install.Installer;
import org.fedoraproject.xmvn.tools.install.JavaPackage;

/**
 * @author Mikolaj Izdebski
 */
public class DefaultInstaller
    implements Installer
{
    private final Logger logger = LoggerFactory.getLogger( DefaultInstaller.class );

    private final Set<ArtifactState> reactor = new LinkedHashSet<>();

    private final Configurator configurator;

    private final Resolver resolver;

    private final ArtifactInstallerFactory installerFactory;

    private Configuration configuration;

    private PackageRegistry packageRegistry;

    public DefaultInstaller( Configurator configurator, Resolver resolver )
    {
        this( configurator, resolver, new ArtifactInstallerFactory( configurator ) );
    }

    DefaultInstaller( Configurator configurator, Resolver resolver, ArtifactInstallerFactory installerFactory )
    {
        this.configurator = configurator;
        this.resolver = resolver;
        this.installerFactory = installerFactory;
    }

    /**
     * Build initial reactor state from installation plan.
     * 
     * @param installationPlan
     * @throws ArtifactInstallationException
     */
    private void buildReactor( InstallationPlan installationPlan )
        throws ArtifactInstallationException
    {
        logger.trace( "Building reactor structure" );

        for ( ArtifactMetadata artifactMetadata : installationPlan.getArtifacts() )
        {
            Artifact artifact = artifactMetadata.toArtifact();

            if ( !reactor.add( new ArtifactState( artifact, artifactMetadata ) ) )
                throw new ArtifactInstallationException( "Installation plan contains duplicate artifact: " + artifact );
        }
    }

    /**
     * Construct effective packaging rule for artifact.
     * 
     * @param artifactState
     */
    private void constructEffectivePackagingRule( ArtifactState artifactState )
    {
        Artifact artifact = artifactState.getArtifact();

        PackagingRule rule = new EffectivePackagingRule( configuration.getArtifactManagement(), artifact.getGroupId(),
                                                         artifact.getArtifactId(), artifact.getExtension(),
                                                         artifact.getClassifier(), artifact.getVersion() );

        artifactState.setPackagingRule( rule );

        if ( logger.isDebugEnabled() )
        {
            try ( StringWriter buffer = new StringWriter() )
            {
                Configuration configuration = new Configuration();
                configuration.addArtifactManagement( rule );
                new ConfigurationStaxWriter().write( buffer, configuration );

                logger.debug( "Effective packaging rule for artifact {} is:\n{}", artifact, buffer );
            }
            catch ( IOException | XMLStreamException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    private void generateSkippedArtifactMetadata()
    {
        List<SkippedArtifactMetadata> skippedArtifacts = new ArrayList<>();

        for ( ArtifactState artifactState : reactor )
        {
            if ( artifactState.getTargetPackage() == null )
            {
                Artifact artifact = artifactState.getArtifact();
                SkippedArtifactMetadata skippedArtifact = new SkippedArtifactMetadata();
                skippedArtifacts.add( skippedArtifact );

                skippedArtifact.setGroupId( artifact.getGroupId() );
                skippedArtifact.setArtifactId( artifact.getArtifactId() );
                skippedArtifact.setExtension( artifact.getExtension() );
                skippedArtifact.setClassifier( artifact.getClassifier() );
            }
        }

        for ( ArtifactState artifactState : reactor )
        {
            JavaPackage targetPackage = artifactState.getTargetPackage();

            if ( targetPackage != null )
            {
                PackageMetadata metadata = targetPackage.getMetadata();
                metadata.setSkippedArtifacts( new ArrayList<>( skippedArtifacts ) );
            }
        }

        if ( logger.isDebugEnabled() )
        {
            try ( StringWriter buffer = new StringWriter() )
            {
                PackageMetadata meta = new PackageMetadata();
                meta.setSkippedArtifacts( skippedArtifacts );
                new MetadataStaxWriter().write( buffer, meta );

                logger.debug( "Skipped artifacts are:\n{}", buffer );
            }
            catch ( IOException | XMLStreamException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    /**
     * Decide into which package to install current artifact.
     * 
     * @param artifactState
     */
    private void assignTargetPackage( ArtifactState artifactState )
    {
        PackagingRule rule = artifactState.getPackagingRule();

        JavaPackage targetPackage = packageRegistry.getPackageById( rule.getTargetPackage() );

        artifactState.setTargetPackage( targetPackage );
    }

    /**
     * Find appropriate installer to install artifact.
     * 
     * @param artifactState
     */
    private void assignArtifactInstaller( ArtifactState artifactState )
    {
        if ( artifactState.getTargetPackage() != null )
            artifactState.setInstaller( installerFactory.getInstallerFor( artifactState.getArtifact(),
                                                                          artifactState.getMetadata().getProperties() ) );
    }

    private void installArtifact( ArtifactState artifactState, String basePackageName, String repositoryId )
        throws ArtifactInstallationException
    {
        JavaPackage targetPackage = artifactState.getTargetPackage();

        if ( targetPackage != null )
        {
            ArtifactInstaller installer = artifactState.getInstaller();
            ArtifactMetadata metadata = artifactState.getMetadata();
            PackagingRule packagingRule = artifactState.getPackagingRule();
            installer.install( targetPackage, metadata, packagingRule, basePackageName, repositoryId );
        }
    }

    /**
     * Try to resolve dependencies of all installed artifacts.
     */
    private void resolveArtifactDependencies()
    {
        Map<Artifact, ArtifactMetadata> installedArtifacts = new LinkedHashMap<>();

        for ( JavaPackage pkg : packageRegistry.getPackages() )
        {
            for ( ArtifactMetadata artifactMetadata : pkg.getMetadata().getArtifacts() )
            {
                Artifact artifact = artifactMetadata.toArtifact();

                for ( String version : artifactMetadata.getCompatVersions() )
                {
                    installedArtifacts.put( artifact.setVersion( version ), artifactMetadata );
                }

                if ( artifactMetadata.getCompatVersions().isEmpty() )
                {
                    installedArtifacts.put( artifact.setVersion( Artifact.DEFAULT_VERSION ), artifactMetadata );
                }
            }
        }

        for ( JavaPackage pkg : packageRegistry.getPackages() )
        {
            for ( ArtifactMetadata artifactMetadata : pkg.getMetadata().getArtifacts() )
            {
                for ( Dependency dependency : artifactMetadata.getDependencies() )
                {
                    resolveDependency( dependency, installedArtifacts );
                }
            }
        }
    }

    private void resolveDependency( Dependency dependency, Map<Artifact, ArtifactMetadata> installedArtifacts )
    {
        for ( String version : Arrays.asList( dependency.getRequestedVersion(), Artifact.DEFAULT_VERSION ) )
        {
            Artifact dependencyArtifact = dependency.toArtifact().setVersion( version );

            // First try to resolve dependency from installed artifact
            ArtifactMetadata resolvedMetadata = installedArtifacts.get( dependencyArtifact );
            if ( resolvedMetadata != null )
            {
                dependency.setResolvedVersion( version );
                dependency.setNamespace( resolvedMetadata.getNamespace() );
                return;
            }
        }

        for ( String version : Arrays.asList( dependency.getRequestedVersion(), Artifact.DEFAULT_VERSION ) )
        {
            Artifact dependencyArtifact = dependency.toArtifact().setVersion( version );

            // Next try system artifact resolver
            ResolutionRequest request = new ResolutionRequest( dependencyArtifact );
            ResolutionResult result = resolver.resolve( request );
            if ( result.getArtifactPath() != null )
            {
                dependency.setResolvedVersion( result.getCompatVersion() );
                dependency.setNamespace( result.getNamespace() );
                return;
            }
        }

        dependency.setResolvedVersion( "UNKNOWN" );
        dependency.setNamespace( "UNKNOWN" );
    }

    @Override
    public InstallationResult install( InstallationRequest request )
        throws ArtifactInstallationException, IOException
    {
        configuration = configurator.getConfiguration();
        InstallerSettings settings = configuration.getInstallerSettings();
        packageRegistry = new PackageRegistry( settings, request.getBasePackageName() );

        logger.debug( "Reading installation plan" );
        InstallationPlan installationPlan = new InstallationPlan( request.getInstallationPlan() );
        buildReactor( installationPlan );

        logger.debug( "Creating effective packaging rules for each artifact" );
        for ( ArtifactState artifactState : reactor )
            constructEffectivePackagingRule( artifactState );

        logger.debug( "Choosing target package for each artifact" );
        for ( ArtifactState artifactState : reactor )
        {
            assignTargetPackage( artifactState );
            logger.debug( "Artifact {} will be installed into {}", artifactState.getArtifact(),
                          artifactState.getTargetPackage() );
        }

        logger.debug( "Generating skipped artifact metadata" );
        generateSkippedArtifactMetadata();

        logger.debug( "Assigning installer for each installable artifact" );
        for ( ArtifactState artifactState : reactor )
            assignArtifactInstaller( artifactState );

        logger.debug( "Installing artifacts" );
        for ( ArtifactState artifactState : reactor )
        {
            if ( logger.isDebugEnabled() )
            {
                if ( artifactState.getInstaller() != null )
                    logger.debug( "Installing {} using {}", artifactState.getArtifact(),
                                  artifactState.getInstaller().getClass().getName() );
            }

            installArtifact( artifactState, request.getBasePackageName(), request.getRepositoryId() );
        }

        logger.debug( "Running post-installation hooks" );
        Set<ArtifactInstaller> installers = new LinkedHashSet<>();
        for ( ArtifactState artifactState : reactor )
        {
            ArtifactInstaller installer = artifactState.getInstaller();
            if ( installer != null && installers.add( installer ) )
                installer.postInstallation();
        }

        logger.debug( "Resolving artifact dependencies..." );
        resolveArtifactDependencies();

        logger.debug( "Installing packages into buildroot: {}", request.getInstallRoot() );
        for ( JavaPackage pkg : packageRegistry.getPackages() )
        {
            logger.debug( "Installing {}", pkg );
            pkg.install( request.getInstallRoot() );

            Path mfiles =
                Paths.get( pkg.getId() == null || pkg.getId().isEmpty() ? ".mfiles" : ".mfiles-" + pkg.getId() );
            if ( request.getDescriptorRoot() != null )
                mfiles = request.getDescriptorRoot().resolve( mfiles );

            logger.debug( "Writing file descriptor {}", mfiles );
            pkg.writeDescriptor( mfiles );
        }

        logger.info( "Installation successful" );
        return new InstallationResult()
        {
        };
    }
}
