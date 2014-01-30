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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.sisu.space.asm.ClassReader;
import org.eclipse.sisu.space.asm.ClassVisitor;
import org.eclipse.sisu.space.asm.MethodVisitor;
import org.eclipse.sisu.space.asm.Opcodes;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.dependency.DependencyExtractionRequest;
import org.fedoraproject.xmvn.dependency.DependencyExtractionResult;
import org.fedoraproject.xmvn.dependency.DependencyExtractor;
import org.fedoraproject.xmvn.model.ModelFormatException;
import org.fedoraproject.xmvn.model.ModelReader;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.repository.RepositoryPath;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.InstallationResult;
import org.fedoraproject.xmvn.tools.install.Installer;
import org.fedoraproject.xmvn.utils.ArtifactUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final RepositoryConfigurator repositoryConfigurator;

    private final Resolver resolver;

    private final DependencyExtractor buildDependencyExtractor;

    private final DependencyExtractor runtimeDependencyExtractor;

    private final ModelReader modelReader;

    private Repository installRepo;

    private Repository rawPomRepo;

    private Repository effectivePomRepo;

    private InstallerSettings settings;

    private Configuration configuration;

    private Map<Package, Package> packages;

    /** map package => installed devel artifacts (no aliases) */
    private Map<Package, Set<Artifact>> packageDevelArtifacts;

    /** map package => installed user artifacts (no aliases) */
    private Map<Package, Set<Artifact>> packageUserArtifacts;

    /** map package => provided artifacts and aliases */
    private Map<Package, Set<Artifact>> packagedArtifacts;

    /** map generic artifact => provided artifact */
    private Map<Artifact, Artifact> providedArtifacts;

    private Set<Artifact> skippedArtifacts;

    @Inject
    public DefaultInstaller( Configurator configurator, RepositoryConfigurator repositoryConfigurator,
                             Resolver resolver, @Named( DependencyExtractor.BUILD )
                             DependencyExtractor buildDependencyExtractor, @Named( DependencyExtractor.RUNTIME )
                             DependencyExtractor runtimeDependencyExtractor, ModelReader modelReader )
    {
        this.configurator = configurator;
        this.repositoryConfigurator = repositoryConfigurator;
        this.resolver = resolver;
        this.buildDependencyExtractor = buildDependencyExtractor;
        this.runtimeDependencyExtractor = runtimeDependencyExtractor;
        this.modelReader = modelReader;
    }

    private void putAttribute( Manifest manifest, String key, String value, String defaultValue )
    {
        if ( defaultValue == null || !value.equals( defaultValue ) )
        {
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue( key, value );
        }
    }

    private Artifact injectManifest( Artifact artifact, String version )
        throws IOException
    {
        File targetJar = Files.createTempFile( "xmvn", ".jar" ).toFile();
        targetJar.deleteOnExit();

        try (JarInputStream jis = new JarInputStream( new FileInputStream( artifact.getFile() ) ))
        {
            Manifest mf = jis.getManifest();
            if ( mf == null )
                return artifact;

            putAttribute( mf, ArtifactUtils.MF_KEY_GROUPID, artifact.getGroupId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_ARTIFACTID, artifact.getArtifactId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_EXTENSION, artifact.getExtension(), ArtifactUtils.DEFAULT_EXTENSION );
            putAttribute( mf, ArtifactUtils.MF_KEY_CLASSIFIER, artifact.getClassifier(), "" );
            putAttribute( mf, ArtifactUtils.MF_KEY_VERSION, version, ArtifactUtils.DEFAULT_VERSION );

            try (JarOutputStream jos = new JarOutputStream( new FileOutputStream( targetJar ), mf ))
            {
                byte[] buf = new byte[512];
                JarEntry entry;
                while ( ( entry = jis.getNextJarEntry() ) != null )
                {
                    jos.putNextEntry( entry );

                    int sz;
                    while ( ( sz = jis.read( buf ) ) > 0 )
                        jos.write( buf, 0, sz );
                }
            }
        }

        return artifact.setFile( targetJar );
    }

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

        for ( org.fedoraproject.xmvn.config.Artifact alias : rule.getAliases() )
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
                logger.debug( "Effective packaging rule for {}:\n", artifact, buffer );
            }
            catch ( XMLStreamException e )
            {
                throw new RuntimeException( e );
            }
        }

        return pkg;
    }

    private List<Artifact> getJppArtifacts( Artifact artifact, PackagingRule rule, String packageName )
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
            versions.add( "SYSTEM" );

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
                    new DefaultArtifact( jppGroup.toString(), jppName.toString(), artifact.getClassifier(),
                                         artifact.getExtension(), version );
                jppArtifact = ArtifactUtils.copyStereotype( jppArtifact, artifact );

                RepositoryPath jppArtifactPath = installRepo.getPrimaryArtifactPath( jppArtifact );
                if ( jppArtifactPath == null )
                    return null;
                jppArtifact = jppArtifact.setFile( jppArtifactPath.getPath().toFile() );
                jppArtifact = ArtifactUtils.setScope( jppArtifact, jppArtifactPath.getRepository().getNamespace() );

                jppArtifacts.add( jppArtifact );
            }
        }

        if ( jppArtifacts.isEmpty() )
            throw new RuntimeException( "At least one non-absolute file must be specified for artifact " + artifact );

        return jppArtifacts;
    }

    private void installAbsoluteSymlinks( Package pkg, Artifact artifact, PackagingRule rule, Path symlinkTarget )
        throws IOException
    {
        List<String> versionSuffixes = new ArrayList<>();
        for ( String version : rule.getVersions() )
            versionSuffixes.add( "-" + version );
        if ( rule.getVersions().isEmpty() )
            versionSuffixes.add( "" );

        for ( String filePath : rule.getFiles() )
        {
            String classifierSuffix = artifact.getClassifier().isEmpty() ? "" : "-" + artifact.getClassifier();
            String extensionSuffix = artifact.getExtension().isEmpty() ? "" : "." + artifact.getExtension();

            for ( String versionSuffix : versionSuffixes )
            {
                Path symlink = Paths.get( filePath + versionSuffix + classifierSuffix + extensionSuffix );
                if ( symlink.isAbsolute() )
                    pkg.addSymlink( Paths.get( "/" ).relativize( symlink ), symlinkTarget );
            }
        }
    }

    private void installArtifact( Package pkg, Artifact artifact, List<Artifact> aliases, List<Artifact> jppArtifacts )
        throws IOException
    {
        logger.info( "===============================================" );
        logger.info( "SOURCE ARTIFACT:" );
        logger.info( "    groupId: {}", artifact.getGroupId() );
        logger.info( " artifactId: {}", artifact.getArtifactId() );
        logger.info( "  extension: {}", artifact.getExtension() );
        logger.info( " classifier: {}", artifact.getClassifier() );
        logger.info( "    version: {}", artifact.getVersion() );
        logger.info( " stereotype: {}", ArtifactUtils.getStereotype( artifact ) );
        logger.info( "  namespace: {}", ArtifactUtils.getScope( artifact ) );
        logger.info( "       file: {}", artifact.getFile() );
        for ( Artifact jppArtifact : jppArtifacts )
        {
            logger.info( "-----------------------------------------------" );
            logger.info( "TARGET ARTIFACT:" );
            logger.info( "    groupId: {}", jppArtifact.getGroupId() );
            logger.info( " artifactId: {}", jppArtifact.getArtifactId() );
            logger.info( "  extension: {}", jppArtifact.getExtension() );
            logger.info( " classifier: {}", jppArtifact.getClassifier() );
            logger.info( "    version: {}", jppArtifact.getVersion() );
            logger.info( " stereotype: {}", ArtifactUtils.getStereotype( jppArtifact ) );
            logger.info( "  namespace: {}", ArtifactUtils.getScope( jppArtifact ) );
            logger.info( "       file: {}", jppArtifact.getFile() );
        }
        logger.info( "===============================================" );

        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        artifact = injectManifest( artifact, primaryJppArtifact.getVersion() );
        pkg.addFile( artifact.getFile().toPath(), primaryJppArtifact.getFile().toPath(), 0644 );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            Path symlink = jppSymlinkArtifact.getFile().toPath();
            pkg.addSymlink( symlink, primaryJppArtifact.getFile().toPath() );
        }

        Set<Artifact> packaged = packagedArtifacts.get( pkg );
        if ( packaged == null )
        {
            packaged = new LinkedHashSet<>();
            packagedArtifacts.put( pkg, packaged );
        }

        for ( Artifact jppArtifact : jppArtifacts )
        {
            String providedVersion = jppArtifact.getVersion();
            Artifact providedArtifact = artifact.setVersion( providedVersion ).setFile( null ).setProperties( null );

            String scope = ArtifactUtils.getScope( jppArtifact );
            Artifact scopedArtifact = ArtifactUtils.setScope( artifact, scope );
            Artifact scopedProvidedArtifact = ArtifactUtils.setScope( providedArtifact, scope );

            packaged.add( providedArtifact );
            providedArtifacts.put( providedArtifact, scopedProvidedArtifact );
            pkg.getMetadata().addMapping( scopedArtifact, jppArtifact );

            for ( Artifact alias : aliases )
            {
                Artifact providedAlias = alias.setVersion( providedVersion ).setProperties( null );
                Artifact scopedAlias = ArtifactUtils.setScope( alias, scope );
                Artifact scopedProvidedAlias = ArtifactUtils.setScope( providedAlias, scope );

                packaged.add( providedAlias );
                providedArtifacts.put( providedAlias, scopedProvidedAlias );
                pkg.getMetadata().addMapping( scopedAlias, jppArtifact );
            }
        }
    }

    private Artifact resolveDependencyArtifact( Package pkg, Artifact artifact )
    {
        Artifact versionlessArtifact = artifact.setVersion( ArtifactUtils.DEFAULT_VERSION );
        Set<Artifact> packaged = packagedArtifacts.get( pkg );
        if ( packaged.contains( artifact ) || packaged.contains( versionlessArtifact ) )
            return null;

        Artifact providedArtifact = providedArtifacts.get( artifact );
        if ( providedArtifact == null )
            providedArtifact = providedArtifacts.get( versionlessArtifact );
        if ( providedArtifact != null )
            return providedArtifact;

        ResolutionRequest request = new ResolutionRequest( artifact );
        ResolutionResult result = resolver.resolve( request );

        if ( result.getArtifactFile() == null )
        {
            logger.warn( "Unable to resolve dependency artifact {}, generating dependencies with unknown version and namespace.",
                         artifact );

            artifact = artifact.setVersion( ArtifactUtils.UNKNOWN_VERSION );
            artifact = ArtifactUtils.setScope( artifact, ArtifactUtils.UNKNOWN_NAMESPACE );

            return artifact;
        }

        String version = result.getCompatVersion() != null ? result.getCompatVersion() : ArtifactUtils.DEFAULT_VERSION;
        artifact = artifact.setVersion( version );

        String namespace = result.getNamespace();
        artifact = ArtifactUtils.setScope( artifact, namespace );

        return artifact;
    }

    private Path writeModel( Model model )
        throws IOException
    {
        Path modelPath = Files.createTempFile( "xmvn", ".pom" );

        try (Writer fileWriter = new FileWriter( modelPath.toFile() ))
        {
            MavenXpp3Writer modelWriter = new MavenXpp3Writer();
            modelWriter.write( fileWriter, model );
            return modelPath;
        }
    }

    private Model simplifyEffectiveModel( Model model )
    {
        Model m = new Model();
        m.setModelEncoding( model.getModelEncoding() );
        m.setModelVersion( model.getModelVersion() );
        m.setGroupId( model.getGroupId() );
        m.setArtifactId( model.getArtifactId() );
        m.setVersion( model.getVersion() );

        for ( Dependency dep : model.getDependencies() )
        {
            String scope = dep.getScope();
            if ( scope != null )
            {
                if ( scope.equals( "system" ) )
                    throw new RuntimeException( "Unsupported system-scoped dependency found" );
                if ( scope.equals( "provided" ) || scope.equals( "test" ) )
                    continue;
                if ( scope.equals( "compile" ) )
                    dep.setScope( null );
            }
            m.addDependency( dep );
        }

        return m;
    }

    private void installPomFiles( Package pkg, Artifact artifact, List<Artifact> jppArtifacts )
        throws IOException, ModelFormatException
    {
        Set<Artifact> jppPomArtifacts = new LinkedHashSet<>();
        for ( Artifact jppArtifact : jppArtifacts )
        {
            Artifact jppPomArtifact =
                new DefaultArtifact( jppArtifact.getGroupId(), jppArtifact.getArtifactId(),
                                     jppArtifact.getClassifier(), "pom", jppArtifact.getVersion() );
            jppPomArtifacts.add( jppPomArtifact );
        }

        Path rawModelPath = ArtifactUtils.getRawModelPath( artifact );
        if ( rawModelPath != null && settings.isEnableRawPoms() )
        {
            Iterator<Artifact> it = jppPomArtifacts.iterator();
            Path target = rawPomRepo.getPrimaryArtifactPath( it.next() ).getPath();
            pkg.addFile( rawModelPath, target, 0644 );

            while ( it.hasNext() )
            {
                Path symlink = rawPomRepo.getPrimaryArtifactPath( it.next() ).getPath();
                pkg.addSymlink( symlink, target );
            }
        }

        Path effectiveModelPath = ArtifactUtils.getEffectiveModelPath( artifact );
        if ( effectiveModelPath != null && settings.isEnableEffectivePoms() )
        {
            Iterator<Artifact> it = jppPomArtifacts.iterator();
            Path target = effectivePomRepo.getPrimaryArtifactPath( it.next() ).getPath();

            Model effectiveModel = modelReader.readModel( effectiveModelPath );
            Model simplifiedEffectiveModel = simplifyEffectiveModel( effectiveModel );
            Path simplifiedEffectivePomPath = writeModel( simplifiedEffectiveModel );
            pkg.addFile( simplifiedEffectivePomPath, target, 0644 );

            while ( it.hasNext() )
            {
                Path symlink = effectivePomRepo.getPrimaryArtifactPath( it.next() ).getPath();
                pkg.addSymlink( symlink, target );
            }
        }
    }

    private boolean containsNativeCode( Artifact artifact )
    {
        // From /usr/include/linux/elf.h
        final int ELFMAG0 = 0x7F;
        final int ELFMAG1 = 'E';
        final int ELFMAG2 = 'L';
        final int ELFMAG3 = 'F';

        try (ZipInputStream jis = new ZipInputStream( new FileInputStream( artifact.getFile() ) ))
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() )
                    continue;
                if ( jis.read() == ELFMAG0 && jis.read() == ELFMAG1 && jis.read() == ELFMAG2 && jis.read() == ELFMAG3 )
                    return true;
            }

            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    private boolean usesNativeCode( Artifact artifact )
        throws IOException
    {
        try (ZipInputStream jis = new ZipInputStream( new FileInputStream( artifact.getFile() ) ))
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() || !ent.getName().endsWith( ".class" ) )
                    continue;

                final boolean[] usesNativeCode = new boolean[1];

                new ClassReader( jis ).accept( new ClassVisitor( Opcodes.ASM4 )
                {
                    @Override
                    public MethodVisitor visitMethod( int flags, String name, String desc, String sig, String[] exc )
                    {
                        usesNativeCode[0] = ( flags & Opcodes.ACC_NATIVE ) != 0;
                        return super.visitMethod( flags, name, desc, sig, exc );
                    }
                }, ClassReader.SKIP_CODE );

                if ( usesNativeCode[0] )
                    return true;
            }

            return false;
        }
    }

    private void installArtifact( Artifact artifact, String packageName )
        throws IOException, ModelFormatException
    {
        Path rawModelPath = ArtifactUtils.getRawModelPath( artifact );
        boolean isAttachedArtifact = rawModelPath == null;
        boolean isPomArtifact = artifact.getFile() == null;
        if ( isAttachedArtifact && isPomArtifact )
            throw new RuntimeException( "Attached artifact cannot be POM artifact: " + artifact );
        if ( isPomArtifact && !artifact.getExtension().equals( "pom" ) )
            throw new RuntimeException( "POM artifact has extension different from 'pom': " + artifact.getExtension() );

        if ( !isPomArtifact && ( containsNativeCode( artifact ) || usesNativeCode( artifact ) ) )
            artifact = ArtifactUtils.setStereotype( artifact, "native" );

        PackagingRule rule = ruleForArtifact( artifact );

        Package pkg = getTargetPackageForArtifact( artifact, rule );
        if ( !pkg.isInstallable() )
        {
            skippedArtifacts.add( artifact );
            return;
        }

        if ( isPomArtifact )
        {
            assert rawModelPath != null;
            artifact = artifact.setFile( rawModelPath.toFile() );
            artifact = ArtifactUtils.setStereotype( artifact, "pom" );
        }

        List<Artifact> jppArtifacts = getJppArtifacts( artifact, rule, packageName );
        if ( jppArtifacts == null )
        {
            logger.warn( "Skipping installation of artifact {}: No suitable repository found to store the artifact in.",
                         artifact );
            return;
        }

        List<Artifact> aliases = getAliasArtifacts( rule );
        installArtifact( pkg, artifact, aliases, jppArtifacts );

        if ( isPomArtifact )
        {
            Set<Artifact> develArtifacts = packageDevelArtifacts.get( pkg );
            if ( develArtifacts == null )
            {
                develArtifacts = new LinkedHashSet<>();
                packageDevelArtifacts.put( pkg, develArtifacts );
            }

            develArtifacts.add( artifact );
        }
        else
        {
            Path primaryJppArtifactPath = jppArtifacts.iterator().next().getFile().toPath();
            installAbsoluteSymlinks( pkg, artifact, rule, primaryJppArtifactPath );

            if ( !isAttachedArtifact )
            {
                installPomFiles( pkg, artifact, jppArtifacts );

                Set<Artifact> userArtifacts = packageUserArtifacts.get( pkg );
                if ( userArtifacts == null )
                {
                    userArtifacts = new LinkedHashSet<>();
                    packageUserArtifacts.put( pkg, userArtifacts );
                }

                userArtifacts.add( artifact );
            }
        }
    }

    private void generateRequires( Package pkg )
        throws IOException, ModelFormatException
    {
        boolean pureDevelPackage = false;
        Set<Artifact> artifacts = packageUserArtifacts.get( pkg );
        DependencyExtractor dependencyExtractor = runtimeDependencyExtractor;

        if ( artifacts == null )
        {
            pureDevelPackage = true;
            artifacts = packageDevelArtifacts.get( pkg );
            dependencyExtractor = buildDependencyExtractor;
        }

        if ( artifacts == null )
            return;

        for ( Artifact artifact : artifacts )
        {
            Path modelPath;

            if ( pureDevelPackage )
            {
                modelPath = ArtifactUtils.getRawModelPath( artifact );
                if ( modelPath == null )
                {
                    logger.warn( "Skipping generation of devel requires for artifact {}: raw model path is not specified",
                                 artifact );
                    return;
                }
            }
            else
            {
                modelPath = ArtifactUtils.getEffectiveModelPath( artifact );
                if ( modelPath == null )
                {
                    logger.warn( "Skipping generation of user requires for artifact {}: effective model path is not specified",
                                 artifact );
                    return;
                }
            }

            DependencyExtractionRequest request = new DependencyExtractionRequest( modelPath );
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
        installRepo = repositoryConfigurator.configureRepository( "install" );
        rawPomRepo = repositoryConfigurator.configureRepository( "install-raw-pom" );
        effectivePomRepo = repositoryConfigurator.configureRepository( "install-effective-pom" );

        configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();

        packages = new TreeMap<>();
        packageDevelArtifacts = new LinkedHashMap<>();
        packageUserArtifacts = new LinkedHashMap<>();
        packagedArtifacts = new LinkedHashMap<>();
        providedArtifacts = new LinkedHashMap<>();
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
