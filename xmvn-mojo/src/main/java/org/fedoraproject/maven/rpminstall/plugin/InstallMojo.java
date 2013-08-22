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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
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
import org.fedoraproject.maven.installer.ArtifactInstaller;
import org.fedoraproject.maven.installer.DefaultPackage;
import org.fedoraproject.maven.installer.DependencyExtractor;
import org.fedoraproject.maven.installer.Installer;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.resolver.Resolver;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

/**
 * @author Mikolaj Izdebski
 */
@Mojo( name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE )
@Component( role = InstallMojo.class )
public class InstallMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject rootProject;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private List<ArtifactInstaller> installers;

    @Requirement
    private RepositoryConfigurator repositoryConfigurator;

    @Requirement
    private Resolver resolver;

    private Repository installRepo;

    private Repository rawPomRepo;

    private Repository effectivePomRepo;

    private InstallerSettings settings;

    private Configuration configuration;

    private Map<String, DefaultPackage> packages;

    private Artifact aetherArtifact( org.apache.maven.artifact.Artifact mavenArtifact )
    {
        String groupId = mavenArtifact.getGroupId();
        String artifactId = mavenArtifact.getArtifactId();
        String version = mavenArtifact.getVersion();
        String stereotype = mavenArtifact.getType();

        ArtifactHandler handler = mavenArtifact.getArtifactHandler();
        String extension = handler.getExtension();
        String classifier = handler.getClassifier();
        if ( StringUtils.isNotEmpty( mavenArtifact.getClassifier() ) )
            classifier = mavenArtifact.getClassifier();

        File artifactFile = mavenArtifact.getFile();

        Artifact artifact = new DefaultArtifact( groupId, artifactId, classifier, extension, version );
        artifact = ArtifactUtils.setStereotype( artifact, stereotype );
        artifact = artifact.setFile( artifactFile );
        return artifact;
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

    private DefaultPackage getTargetPackageForArtifact( Artifact artifact, PackagingRule rule )
        throws IOException
    {
        String packageName = rule.getTargetPackage();
        if ( StringUtils.isEmpty( packageName ) )
            packageName = DefaultPackage.MAIN;
        DefaultPackage pkg = packages.get( packageName );

        if ( pkg == null )
        {
            pkg = new DefaultPackage( packageName, settings, logger );
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
        List<Path> extraList = new ArrayList<>( rule.getFiles().size() );
        for ( String fileName : rule.getFiles() )
            extraList.add( Paths.get( fileName ) );

        if ( extraList.isEmpty() )
            extraList.add( Paths.get( settings.getPackageName() + "/" + artifact.getArtifactId() ) );

        List<Artifact> jppArtifacts = new ArrayList<>();

        for ( Path basePath : extraList )
        {
            Path jppName = basePath.getFileName();
            Path jppGroup = Paths.get( "JPP" );
            if ( basePath.getParent() != null )
                jppGroup = jppGroup.resolve( basePath.getParent() );

            // TODO: if compat package then set version to real version, not SYSTEM
            Artifact jppArtifact =
                new DefaultArtifact( jppGroup.toString(), jppName.toString(), artifact.getClassifier(),
                                     artifact.getExtension(), "SYSTEM" );

            Path jppArtifactPath = installRepo.getPrimaryArtifactPath( jppArtifact );
            if ( jppArtifactPath == null )
                return null;
            jppArtifact = jppArtifact.setFile( jppArtifactPath.toFile() );

            jppArtifacts.add( jppArtifact );
        }

        return jppArtifacts;
    }

    private void installArtifact( DefaultPackage pkg, Artifact artifact, List<Artifact> aliases,
                                  List<Artifact> jppArtifacts )
        throws IOException
    {
        logger.info( "===============================================" );
        logger.info( "SOURCE ARTIFACT:" );
        logger.info( "    groupId: " + artifact.getGroupId() );
        logger.info( " artifactId: " + artifact.getArtifactId() );
        logger.info( "  extension: " + artifact.getExtension() );
        logger.info( " classifier: " + artifact.getClassifier() );
        logger.info( "    version: " + artifact.getVersion() );
        logger.info( " stereotype: " + artifact.getProperty( "xmvn.stereotype", "" ) );
        logger.info( "       file: " + artifact.getFile() );
        for ( Artifact jppArtifact : jppArtifacts )
        {
            logger.info( "TARGET ARTIFACT:" );
            logger.info( "    groupId: " + jppArtifact.getGroupId() );
            logger.info( " artifactId: " + jppArtifact.getArtifactId() );
            logger.info( "  extension: " + jppArtifact.getExtension() );
            logger.info( " classifier: " + jppArtifact.getClassifier() );
            logger.info( "    version: " + jppArtifact.getVersion() );
            logger.info( " stereotype: " + jppArtifact.getProperty( "xmvn.stereotype", "" ) );
            logger.info( "       file: " + jppArtifact.getFile() );
        }
        logger.info( "===============================================" );

        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        pkg.addFile( artifact.getFile().toPath(), primaryJppArtifact.getFile().toPath(), 0644 );

        pkg.getMetadata().addMapping( artifact, primaryJppArtifact );
        for ( Artifact alias : aliases )
            pkg.getMetadata().addMapping( alias, primaryJppArtifact );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            pkg.addSymlink( jppSymlinkArtifact.getFile().toPath(), primaryJppArtifact.getFile().toPath() );
        }
    }

    private void generateDevelRequires( DefaultPackage pkg, MavenProject project )
        throws IOException
    {
        Model rawModel = DependencyExtractor.getRawModel( project );
        DependencyExtractor.generateRawRequires( resolver, rawModel, pkg.getMetadata() );
    }

    private void generateUserRequires( DefaultPackage pkg, MavenProject project )
    {
        DependencyExtractor.generateEffectiveRuntimeRequires( resolver, project.getModel(), pkg.getMetadata() );
    }

    private void installRawPom( DefaultPackage pkg, Path source, Artifact jppArtifact )
    {
        Path target = rawPomRepo.getPrimaryArtifactPath( jppArtifact );
        pkg.addFile( source, target, 0644 );
    }

    private void installEffectivePom( DefaultPackage pkg, Model model, Artifact jppArtifact )
        throws IOException
    {
        Path source = Files.createTempFile( "xmvn", ".pom.xml" );
        DependencyExtractor.simplifyEffectiveModel( model );
        DependencyExtractor.writeModel( model, source );

        Path target = effectivePomRepo.getPrimaryArtifactPath( jppArtifact );
        pkg.addFile( source, target, 0644 );
    }

    private void installPomFiles( DefaultPackage pkg, MavenProject project, List<Artifact> jppArtifacts )
        throws IOException
    {
        Artifact jppArtifact = jppArtifacts.iterator().next();
        Artifact jppPomArtifact =
            new DefaultArtifact( jppArtifact.getGroupId(), jppArtifact.getArtifactId(), jppArtifact.getClassifier(),
                                 "pom", jppArtifact.getVersion() );

        if ( settings.isEnableRawPoms() )
            installRawPom( pkg, project.getFile().toPath(), jppPomArtifact );

        if ( settings.isEnableEffectivePoms() )
            installEffectivePom( pkg, project.getModel(), jppPomArtifact );
    }

    private void installMainArtifact( MavenProject project )
        throws IOException
    {
        Artifact artifact = aetherArtifact( project.getArtifact() );
        boolean isPomArtifact = artifact.getFile() == null;
        PackagingRule rule = ruleForArtifact( artifact );

        DefaultPackage pkg = getTargetPackageForArtifact( artifact, rule );
        if ( pkg == null )
            return;

        if ( isPomArtifact )
        {
            artifact = new DefaultArtifact( project.getGroupId(), project.getArtifactId(), "pom", project.getVersion() );
            artifact = artifact.setFile( project.getFile() );
            artifact = ArtifactUtils.setStereotype( artifact, project.getArtifact().getType() );
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

        if ( !isPomArtifact )
        {
            installPomFiles( pkg, project, jppArtifacts );
            generateUserRequires( pkg, project );
        }
        else
        {
            generateDevelRequires( pkg, project );
        }
    }

    private void installAttachedArtifacts( MavenProject project )
        throws IOException
    {
        for ( org.apache.maven.artifact.Artifact mavenArtifact : project.getAttachedArtifacts() )
        {
            Artifact artifact = aetherArtifact( mavenArtifact );
            PackagingRule rule = ruleForArtifact( artifact );
            DefaultPackage pkg = getTargetPackageForArtifact( artifact, rule );
            List<Artifact> jppArtifacts = getJppArtifacts( artifact, rule );
            if ( jppArtifacts == null )
            {
                logger.warn( "Skipping installation of attached artifact " + artifact
                    + ": No suitable repository found to store the artifact in." );
                continue;
            }

            List<Artifact> aliases = getAliasArtifacts( rule );
            installArtifact( pkg, artifact, aliases, jppArtifacts );
        }
    }

    private void checkForUnmatchedRules( List<PackagingRule> artifactManagement )
        throws MojoFailureException
    {
        boolean unmatchedRuleFound = false;

        for ( PackagingRule rule : artifactManagement )
        {
            if ( !rule.isOptional() && !rule.isMatched() )
            {
                unmatchedRuleFound = true;
                org.fedoraproject.maven.config.Artifact glob = rule.getArtifactGlob();
                String globString = glob.getGroupId() + ":" + glob.getArtifactId() + ":" + glob.getVersion();
                logger.error( "Unmatched packaging rule: " + globString );
            }
        }

        if ( unmatchedRuleFound )
        {
            throw new MojoFailureException( "There are unmatched packaging rules" );
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        installRepo = repositoryConfigurator.configureRepository( "install" );
        rawPomRepo = repositoryConfigurator.configureRepository( "install-raw-pom" );
        effectivePomRepo = repositoryConfigurator.configureRepository( "install-effective-pom" );

        configuration = configurator.getConfiguration();
        settings = configuration.getInstallerSettings();
        LoggingUtils.setLoggerThreshold( logger, settings.isDebug() );

        packages = new TreeMap<>();

        DefaultPackage mainPackage = new DefaultPackage( DefaultPackage.MAIN, settings, logger );
        packages.put( DefaultPackage.MAIN, mainPackage );

        try
        {
            for ( MavenProject project : reactorProjects )
            {
                installMainArtifact( project );
                installAttachedArtifacts( project );
            }

            checkForUnmatchedRules( configuration.getArtifactManagement() );

            Path installRoot = Paths.get( settings.getInstallRoot() );
            Installer installer = new Installer( installRoot );

            for ( DefaultPackage pkg : packages.values() )
                if ( pkg.isInstallable() )
                    pkg.install( installer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install project", e );
        }
    }
}
