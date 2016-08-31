/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.gradle;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepositoryAccess;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.DescriptorParseContext;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.GradlePomModuleDescriptorParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.MetaDataParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionSelectorScheme;
import org.gradle.api.internal.artifacts.repositories.AbstractArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.internal.component.external.model.DefaultMavenModuleResolveMetaData;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactMetaData;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetaData;
import org.gradle.internal.component.external.model.MutableModuleComponentResolveMetaData;
import org.gradle.internal.component.model.ComponentArtifactMetaData;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetaData;
import org.gradle.internal.component.model.ComponentUsage;
import org.gradle.internal.component.model.DefaultIvyArtifactName;
import org.gradle.internal.component.model.DependencyMetaData;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleSource;
import org.gradle.internal.resolve.ArtifactResolveException;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;
import org.gradle.internal.resource.local.DefaultLocallyAvailableExternalResource;
import org.gradle.internal.resource.local.DefaultLocallyAvailableResource;
import org.gradle.internal.resource.local.LocallyAvailableExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.locator.IsolatedXMvnServiceLocator;
import org.fedoraproject.xmvn.locator.XMvnHomeClassLoader;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * @author Mikolaj Izdebski
 */
public class GradleResolver
    extends AbstractArtifactRepository
    implements ResolutionAwareRepository, ConfiguredModuleComponentRepository, ModuleComponentRepositoryAccess,
    DescriptorParseContext
{
    private final Logger logger = LoggerFactory.getLogger( GradleResolver.class );

    static class LazyLocatorProvider
    {
        static final IsolatedXMvnServiceLocator locator;

        static
        {
            XMvnHomeClassLoader realm = new XMvnHomeClassLoader( LazyLocatorProvider.class.getClassLoader() );
            realm.importAllPackages( "org.slf4j" );
            realm.importAllPackages( "org.gradle.api.logging" );
            locator = new IsolatedXMvnServiceLocator( realm );
        }
    }

    static class LazyResolverProvider
    {
        static final Resolver resolver = LazyLocatorProvider.locator.getService( Resolver.class );
    }

    private Path resolve( Artifact artifact )
    {
        logger.debug( "Trying to resolve artifact {}", artifact );
        ResolutionRequest request = new ResolutionRequest( artifact );
        Resolver resolver = LazyResolverProvider.resolver;
        ResolutionResult result = resolver.resolve( request );
        return result.getArtifactPath();
    }

    @Override
    public ConfiguredModuleComponentRepository createResolver()
    {
        return this;
    }

    @Override
    public String getId()
    {
        return "XMvn";
    }

    @Override
    public ModuleComponentRepositoryAccess getLocalAccess()
    {
        return this;
    }

    @Override
    public ModuleComponentRepositoryAccess getRemoteAccess()
    {
        return this;
    }

    @Override
    public boolean isDynamicResolveMode()
    {
        return false;
    }

    @Override
    public boolean isLocal()
    {
        return true;
    }

    @Override
    public void listModuleVersions( DependencyMetaData arg0, BuildableModuleVersionListingResolveResult arg1 )
    {
        logger.debug( "listModuleVersions() called, but it is NOT IMPLEMENTED" );
    }

    @Override
    public void resolveArtifact( ComponentArtifactMetaData artifact, ModuleSource module,
                                 BuildableArtifactResolveResult result )
    {
        ModuleVersionIdentifier moduleId =
            ( (DefaultModuleComponentArtifactMetaData) artifact ).toArtifactIdentifier().getModuleVersionIdentifier();
        String groupId = moduleId.getGroup();
        String artifactId = artifact.getName().getName();
        String extension = artifact.getName().getExtension();
        String classifier = artifact.getName().getClassifier();
        String version = moduleId.getVersion();

        Artifact artifact2 = new DefaultArtifact( groupId, artifactId, extension, classifier, version );
        Path path = resolve( artifact2 );

        if ( path == null )
        {
            logger.debug( "Unable to resolve artifact {}", artifact2 );
            result.failed( new ArtifactResolveException( artifact.getId(),
                                                         "XMvn was unable to resolve artifact " + artifact2 ) );
            return;
        }

        logger.debug( "Artifact {} was resolved to {}", artifact2, path );
        result.resolved( path.toFile() );
    }

    @Override
    public void resolveComponentMetaData( ModuleComponentIdentifier id, ComponentOverrideMetadata request,
                                          BuildableModuleComponentMetaDataResolveResult result )
    {
        logger.debug( "Trying to resolve model for {}:{}:{}", id.getGroup(), id.getModule(), id.getVersion() );

        Artifact artifact2 = new DefaultArtifact( id.getGroup(), id.getModule(), "pom", id.getVersion() );
        Path pomPath = resolve( artifact2 );

        if ( pomPath != null )
        {
            logger.debug( "Found Maven POM: {}", pomPath );

            MetaDataParser<DefaultMavenModuleResolveMetaData> parser =
                new GradlePomModuleDescriptorParser( new DefaultVersionSelectorScheme( new DefaultVersionComparator() ) );
            MutableModuleComponentResolveMetaData metaData = parser.parseMetaData( this, pomPath.toFile() );

            result.resolved( metaData );
            return;
        }
        else
        {
            logger.debug( "POM not found, trying non-POM artifacts" );
            for ( IvyArtifactName artifact : getDependencyArtifactNames( id, request ) )
            {
                String groupId = id.getGroup();
                String artifactId = artifact.getName();
                String extension = artifact.getExtension();
                String classifier = artifact.getClassifier();
                String version = id.getVersion();

                Artifact artifact3 = new DefaultArtifact( groupId, artifactId, extension, classifier, version );
                Path path = resolve( artifact3 );

                if ( path != null )
                {
                    logger.debug( "Artifact {} found, returning minimal model", artifact3 );
                    MutableModuleComponentResolveMetaData metaData =
                        new DefaultMavenModuleResolveMetaData( id, request.getArtifacts() );
                    result.resolved( metaData );
                    return;
                }
            }
        }

        logger.debug( "No POM and no artifact found, failing" );
        result.failed( new ModuleVersionResolveException( id, "XMvn was unable to resolve artifact " + artifact2 ) );
    }

    private Set<IvyArtifactName> getDependencyArtifactNames( ModuleComponentIdentifier id,
                                                             ComponentOverrideMetadata request )
    {
        Set<IvyArtifactName> artifactSet = new LinkedHashSet<>();
        artifactSet.addAll( request.getArtifacts() );

        if ( artifactSet.isEmpty() )
        {
            artifactSet.add( new DefaultIvyArtifactName( id.getModule(), "jar", "jar",
                                                         Collections.<String, String> emptyMap() ) );
        }

        return artifactSet;
    }

    @Override
    public void resolveModuleArtifacts( ComponentResolveMetaData component, ComponentUsage usage,
                                        BuildableArtifactSetResolveResult result )
    {
        result.resolved( Collections.singleton( ( (ModuleComponentResolveMetaData) component ).artifact( "jar", "jar",
                                                                                                         null ) ) );
    }

    @Override
    public void resolveModuleArtifacts( ComponentResolveMetaData component, ArtifactType type,
                                        BuildableArtifactSetResolveResult result )
    {
        if ( type != ArtifactType.MAVEN_POM )
        {
            logger.debug( "resolveModuleArtifacts() called for artifact type {}", type );
            result.failed( new ArtifactResolveException( "resolveModuleArtifacts() is implemended only for Maven POMs" ) );
            return;
        }

        ModuleComponentResolveMetaData metaData = (ModuleComponentResolveMetaData) component;
        ModuleComponentIdentifier id = metaData.getComponentId();
        DefaultIvyArtifactName name = new DefaultIvyArtifactName( id.getModule(), "pom", "pom" );
        DefaultModuleComponentArtifactMetaData resolvedMetaData =
            new DefaultModuleComponentArtifactMetaData( id, name );
        result.resolved( Collections.singleton( resolvedMetaData ) );
    }

    @Override
    public LocallyAvailableExternalResource getMetaDataArtifact( ModuleComponentIdentifier id, ArtifactType type )
    {
        Path pomPath = resolve( new DefaultArtifact( id.getGroup(), id.getModule(), "pom", id.getVersion() ) );

        if ( pomPath == null )
            return null;

        return new DefaultLocallyAvailableExternalResource( pomPath.toUri(),
                                                            new DefaultLocallyAvailableResource( pomPath.toFile() ) );
    }
}
