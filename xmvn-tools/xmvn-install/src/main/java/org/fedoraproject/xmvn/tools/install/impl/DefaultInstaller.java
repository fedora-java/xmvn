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

import static org.fedoraproject.xmvn.tools.install.impl.JarUtils.containsNativeCode;
import static org.fedoraproject.xmvn.tools.install.impl.JarUtils.usesNativeCode;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.dependency.DependencyExtractionRequest;
import org.fedoraproject.xmvn.dependency.DependencyExtractionResult;
import org.fedoraproject.xmvn.dependency.DependencyExtractor;
import org.fedoraproject.xmvn.model.ModelFormatException;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryPath;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.InstallationResult;
import org.fedoraproject.xmvn.tools.install.Installer;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultInstaller
    implements Installer
{
    private final Logger logger = LoggerFactory.getLogger( DefaultInstaller.class );

    private final Configurator configurator;

    private final Resolver resolver;

    private final DependencyExtractor buildDependencyExtractor;

    private final DependencyExtractor runtimeDependencyExtractor;

    private InstallerSettings settings;

    private Configuration configuration;

    private Map<Package, Package> packages;

    private Set<Artifact> skippedArtifacts;

    private final Map<String, ArtifactInstaller> installers;

    @Inject
    public DefaultInstaller( Configurator configurator, Resolver resolver,
                             @Named( DependencyExtractor.BUILD ) DependencyExtractor buildDependencyExtractor,
                             @Named( DependencyExtractor.RUNTIME ) DependencyExtractor runtimeDependencyExtractor,
                             Map<String, ArtifactInstaller> installers )
    {
        this.configurator = configurator;
        this.resolver = resolver;
        this.buildDependencyExtractor = buildDependencyExtractor;
        this.runtimeDependencyExtractor = runtimeDependencyExtractor;
        this.installers = installers;
    }

    private PackagingRule ruleForArtifact( Artifact artifact )
    {
        return configuration.createEffectivePackagingRule( artifact.getStereotype(), artifact.getGroupId(),
                                                           artifact.getArtifactId(), artifact.getExtension(),
                                                           artifact.getClassifier(), artifact.getVersion() );
    }

    static List<Artifact> getAliasArtifacts( PackagingRule rule )
    {
        List<Artifact> aliasArtifacts = new ArrayList<>();

        for ( org.fedoraproject.xmvn.config.Artifact alias : rule.getAliases() )
        {
            Artifact aliasArtifact =
                new DefaultArtifact( alias.getGroupId(), alias.getArtifactId(), alias.getExtension(),
                                     alias.getClassifier(), alias.getVersion() );
            aliasArtifact = aliasArtifact.setStereotype( alias.getStereotype() );
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

        Package pkg = new Package( packageName, settings );
        if ( packages.containsKey( pkg ) )
            pkg = packages.get( pkg );
        else
            packages.put( pkg, pkg );

        if ( logger.isDebugEnabled() )
        {
            try (StringWriter buffer = new StringWriter())
            {
                Configuration wrapperConfiguration = new Configuration();
                wrapperConfiguration.addArtifactManagement( rule );
                ConfigurationStaxWriter configurationWriter = new ConfigurationStaxWriter();
                configurationWriter.write( buffer, wrapperConfiguration );
                logger.debug( "Effective packaging rule for {}:\n{}", artifact, buffer );
            }
            catch ( XMLStreamException e )
            {
                throw new RuntimeException( e );
            }
        }

        return pkg;
    }

    static List<Artifact> getJppArtifacts( Artifact artifact, PackagingRule rule, String packageName, Repository repo )
    {
        Set<Path> basePaths = new LinkedHashSet<>();
        for ( String fileName : rule.getFiles() )
            basePaths.add( Paths.get( fileName ) );
        if ( basePaths.isEmpty() )
            basePaths.add( Paths.get( packageName + "/" + artifact.getArtifactId() ) );

        Set<String> versions = new LinkedHashSet<>();
        for ( String version : rule.getVersions() )
            versions.add( version );
        if ( versions.isEmpty() )
            versions.add( Artifact.DEFAULT_VERSION );

        List<Artifact> jppArtifacts = new ArrayList<>();

        for ( Path basePath : basePaths )
        {
            if ( basePath.isAbsolute() )
                continue;

            Path jppName = basePath.getFileName();
            Path jppGroup = Paths.get( "JPP" );
            if ( basePath.getParent() != null )
                jppGroup = jppGroup.resolve( basePath.getParent() );

            for ( String version : versions )
            {
                Artifact jppArtifact =
                    new DefaultArtifact( jppGroup.toString(), jppName.toString(), artifact.getExtension(),
                                         artifact.getClassifier(), version );
                jppArtifact = jppArtifact.setStereotype( artifact.getStereotype() );

                RepositoryPath jppArtifactPath = repo.getPrimaryArtifactPath( jppArtifact );
                if ( jppArtifactPath == null )
                    return null;
                jppArtifact = jppArtifact.setPath( jppArtifactPath.getPath() );
                jppArtifact = jppArtifact.setScope( jppArtifactPath.getRepository().getNamespace() );

                jppArtifacts.add( jppArtifact );
            }
        }

        if ( jppArtifacts.isEmpty() )
            throw new RuntimeException( "At least one non-absolute file must be specified for artifact " + artifact );

        return jppArtifacts;
    }

    private Artifact resolvePackagedArtifact( Artifact artifact, Set<Package> packageSet )
    {
        for ( Package pkg : packageSet )
        {
            Artifact providedArtifact = pkg.getProvidedArtifact( artifact );
            if ( providedArtifact != null )
                return providedArtifact;
        }

        Artifact versionlessArtifact = artifact.setVersion( null );
        for ( Package pkg : packageSet )
        {
            Artifact providedArtifact = pkg.getProvidedArtifact( versionlessArtifact );
            if ( providedArtifact != null )
                return providedArtifact;
        }

        return null;
    }

    private Artifact resolveDependencyArtifact( Package pkg, Artifact artifact )
    {
        if ( resolvePackagedArtifact( artifact, Collections.singleton( pkg ) ) != null )
            return null;

        Artifact providedArtifact = resolvePackagedArtifact( artifact, packages.keySet() );
        if ( providedArtifact != null )
            return providedArtifact;

        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolver.resolve( request );

        if ( result.getArtifactPath() == null )
        {
            logger.warn( "Unable to resolve dependency artifact {}, generating dependencies with unknown version and namespace.",
                         artifact );

            return artifact.setVersion( ArtifactUtils.UNKNOWN_VERSION ).setScope( ArtifactUtils.UNKNOWN_NAMESPACE );
        }

        String version = result.getCompatVersion() != null ? result.getCompatVersion() : Artifact.DEFAULT_VERSION;
        artifact = artifact.setVersion( version );

        String namespace = result.getNamespace();
        artifact = artifact.setScope( namespace );

        return artifact;
    }

    private ArtifactInstaller getInstallerForArtifact( Artifact artifact )
    {
        String key = artifact.getExtension();
        if ( StringUtils.isEmpty( key ) )
            key = Artifact.DEFAULT_EXTENSION;

        String stereotype = artifact.getStereotype();
        if ( StringUtils.isNotEmpty( stereotype ) )
            key += "/" + stereotype;

        ArtifactInstaller installer = installers.get( key );
        if ( installer == null )
            installer = installers.get( "default" );

        return installer;
    }

    private void installArtifact( Artifact artifact, String packageName )
        throws IOException
    {
        ArtifactInstaller installer = getInstallerForArtifact( artifact );

        Path artifactPath = artifact.getPath();
        if ( artifactPath != null && ( containsNativeCode( artifactPath ) || usesNativeCode( artifactPath ) ) )
            artifact = artifact.setStereotype( "native" );

        PackagingRule rule = ruleForArtifact( artifact );

        Package pkg = getTargetPackageForArtifact( artifact, rule );
        if ( !pkg.isInstallable() )
        {
            skippedArtifacts.add( artifact );
            return;
        }

        installer.installArtifact( pkg, artifact, rule, packageName );
    }

    private void generateRequires( Package pkg )
        throws IOException, ModelFormatException
    {
        boolean pureDevelPackage = false;
        Set<Artifact> artifacts = pkg.getUserArtifacts();
        DependencyExtractor dependencyExtractor = runtimeDependencyExtractor;

        if ( artifacts == null )
        {
            pureDevelPackage = true;
            artifacts = pkg.getDevelArtifacts();
            dependencyExtractor = buildDependencyExtractor;
        }

        if ( artifacts == null )
            return;

        for ( Artifact artifact : artifacts )
        {
            DependencyExtractionRequest request = new DependencyExtractionRequest( artifact.getPath() );
            DependencyExtractionResult result = dependencyExtractor.extract( request );
            FragmentFile metadata = pkg.getMetadata();

            for ( Artifact dependencyArtifact : result.getDependencyArtifacts() )
            {
                Artifact resolvedDependencyArtifact = resolveDependencyArtifact( pkg, dependencyArtifact );
                if ( resolvedDependencyArtifact != null )
                    metadata.addDependency( resolvedDependencyArtifact );
            }

            String javaVersion = result.getJavaVersion();
            if ( javaVersion != null )
            {
                if ( pureDevelPackage )
                    metadata.addJavaVersionBuildDependency( javaVersion );
                else
                    metadata.addJavaVersionRuntimeDependency( javaVersion );
            }
        }
    }

    private void addSkippedArtifactsInfo()
    {
        Iterator<Package> it = packages.values().iterator();
        Package pkg = it.next();
        while ( !pkg.isInstallable() && it.hasNext() )
            pkg = it.next();

        pkg.getMetadata().addSkippedArtifacts( skippedArtifacts );
    }

    private void checkForUnmatchedRules( List<PackagingRule> artifactManagement )
    {
        boolean unmatchedRuleFound = false;

        for ( PackagingRule rule : artifactManagement )
        {
            if ( !rule.isOptional() && !rule.isMatched() )
            {
                unmatchedRuleFound = true;
                org.fedoraproject.xmvn.config.Artifact glob = rule.getArtifactGlob();
                String globString =
                    glob.getGroupId() + ":" + glob.getArtifactId() + ":" + glob.getExtension() + ":"
                        + glob.getClassifier() + ":" + glob.getVersion();
                logger.error( "Unmatched packaging rule: {}", globString );
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
        configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();

        packages = new TreeMap<>();
        skippedArtifacts = new LinkedHashSet<>();

        Package mainPackage = new Package( Package.MAIN, settings );
        packages.put( mainPackage, mainPackage );

        Set<Artifact> artifactSet = request.getArtifacts();

        try
        {
            for ( Artifact artifact : artifactSet )
                installArtifact( artifact, request.getBasePackageName() );

            addSkippedArtifactsInfo();

            if ( request.isCheckForUnmatchedRules() )
                checkForUnmatchedRules( configuration.getArtifactManagement() );

            Path root = request.getInstallRoot();

            for ( Package pkg : packages.values() )
                generateRequires( pkg );

            if ( Files.exists( root ) && !Files.isDirectory( root ) )
                throw new IOException( root + " is not a directory" );

            Files.createDirectories( root );

            for ( Package pkg : packages.values() )
                if ( pkg.isInstallable() )
                    pkg.install( request.getBasePackageName(), root );
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
