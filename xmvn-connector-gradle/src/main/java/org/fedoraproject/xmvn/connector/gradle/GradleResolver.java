/*-
 * Copyright (c) 2014-2019 Red Hat, Inc.
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
import java.util.Map;
import java.util.Set;

import org.gradle.api.artifacts.ComponentMetadataSupplier;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.ExperimentalFeatures;
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepositoryAccess;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.DescriptorParseContext;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.MetaDataParser;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact;
import org.gradle.api.internal.artifacts.repositories.AbstractArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository;
import org.gradle.api.internal.artifacts.repositories.resolver.MetadataFetchingCost;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.api.internal.model.NamedObjectInstantiator;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactMetadata;
import org.gradle.internal.component.external.model.DefaultMutableMavenModuleResolveMetadata;
import org.gradle.internal.component.external.model.FixedComponentArtifacts;
import org.gradle.internal.component.external.model.MavenDependencyDescriptor;
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetadata;
import org.gradle.internal.component.external.model.ModuleDependencyMetadata;
import org.gradle.internal.component.external.model.MutableMavenModuleResolveMetadata;
import org.gradle.internal.component.external.model.MutableModuleComponentResolveMetadata;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.DefaultIvyArtifactName;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleSource;
import org.gradle.internal.resolve.ArtifactResolveException;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentArtifactsResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;
import org.gradle.internal.resource.local.FileResourceRepository;
import org.gradle.internal.resource.local.LocallyAvailableExternalResource;
import org.gradle.internal.resource.metadata.DefaultExternalResourceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
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
    public GradleResolver( MetaDataParser<MutableMavenModuleResolveMetadata> pomParser,
                           ImmutableModuleIdentifierFactory moduleIdentifierFactory,
                           FileResourceRepository fileRepository, ImmutableAttributesFactory immutableAttributesFactory,
                           NamedObjectInstantiator objectInstantiator, ExperimentalFeatures experimentalFeatures )
    {
        this.pomParser = pomParser;
        this.moduleIdentifierFactory = moduleIdentifierFactory;
        this.fileRepository = fileRepository;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.experimentalFeatures = experimentalFeatures;
        this.objectInstantiator = objectInstantiator;
    }

    private MetaDataParser<MutableMavenModuleResolveMetadata> pomParser;

    private ImmutableModuleIdentifierFactory moduleIdentifierFactory;

    private ImmutableAttributesFactory immutableAttributesFactory;

    private ExperimentalFeatures experimentalFeatures;

    private NamedObjectInstantiator objectInstantiator;

    private FileResourceRepository fileRepository;

    private final Logger logger = LoggerFactory.getLogger( GradleResolver.class );

    static class LazyLocatorProvider
    {
        static final ServiceLocator LOCATOR;

        static
        {
            LOCATOR = new ServiceLocatorFactory().createServiceLocator();
        }
    }

    static class LazyResolverProvider
    {
        static final Resolver RESOLVER = LazyLocatorProvider.LOCATOR.getService( Resolver.class );
    }

    private Path resolve( Artifact artifact )
    {
        logger.debug( "Trying to resolve artifact {}", artifact );
        ResolutionRequest request = new ResolutionRequest( artifact );
        Resolver resolver = LazyResolverProvider.RESOLVER;
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
    public void resolveArtifact( ComponentArtifactMetadata artifact, ModuleSource module,
                                 BuildableArtifactResolveResult result )
    {
        ModuleVersionIdentifier moduleId =
            ( (DefaultModuleComponentArtifactMetadata) artifact ).toArtifactIdentifier().getModuleVersionIdentifier();
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

            MutableModuleComponentResolveMetadata metaData = pomParser.parseMetaData( this, pomPath.toFile() );

            result.resolved( metaData.asImmutable() );
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
                    ModuleVersionIdentifier mvi =
                        moduleIdentifierFactory.moduleWithVersion( id.getGroup(), id.getModule(), id.getVersion() );
                    DefaultMutableMavenModuleResolveMetadata metaData =
                        new DefaultMutableMavenModuleResolveMetadata( mvi, id,
                                                                      Collections.<MavenDependencyDescriptor>emptyList(),
                                                                      immutableAttributesFactory, objectInstantiator,
                                                                      experimentalFeatures );
                    result.resolved( metaData.asImmutable() );
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
            artifactSet.add( new DefaultIvyArtifactName( id.getModule(), "jar", "jar", null ) );
        }

        return artifactSet;
    }

    @Override
    public void resolveArtifactsWithType( ComponentResolveMetadata component, ArtifactType type,
                                          BuildableArtifactSetResolveResult result )
    {
        if ( type != ArtifactType.MAVEN_POM )
        {
            logger.debug( "resolveModuleArtifacts() called for artifact type {}", type );
            result.failed( new ArtifactResolveException( "resolveModuleArtifacts() is implemended only for Maven POMs" ) );
            return;
        }

        ModuleComponentIdentifier id = (ModuleComponentIdentifier) component.getComponentId();
        DefaultIvyArtifactName name = new DefaultIvyArtifactName( id.getModule(), "pom", "pom" );
        DefaultModuleComponentArtifactMetadata resolvedMetaData =
            new DefaultModuleComponentArtifactMetadata( id, name );
        result.resolved( Collections.singleton( resolvedMetaData ) );
    }

    @Override
    public void resolveArtifacts( ComponentResolveMetadata component, BuildableComponentArtifactsResolveResult result )
    {
        ModuleComponentArtifactMetadata artifact =
            ( (ModuleComponentResolveMetadata) component ).artifact( "jar", "jar", null );
        result.resolved( new FixedComponentArtifacts( Collections.singleton( artifact ) ) );
    }

    @Override
    public LocallyAvailableExternalResource getMetaDataArtifact( ModuleComponentIdentifier id, ArtifactType type )
    {
        Path pomPath = resolve( new DefaultArtifact( id.getGroup(), id.getModule(), "pom", id.getVersion() ) );

        if ( pomPath == null )
            return null;

        DefaultExternalResourceMetaData metadata = new DefaultExternalResourceMetaData( pomPath.toUri(), 0, 0 );
        return fileRepository.resource( pomPath.toFile(), pomPath.toUri(), metadata );
    }

    @Override
    public ComponentMetadataSupplier createMetadataSupplier()
    {
        return null;
    }

    @Override
    public Map<ComponentArtifactIdentifier, ResolvableArtifact> getArtifactCache()
    {
        return Collections.emptyMap();
    }

    @Override
    public MetadataFetchingCost estimateMetadataFetchingCost( ModuleComponentIdentifier arg0 )
    {
        return MetadataFetchingCost.CHEAP;
    }

    @Override
    public LocallyAvailableExternalResource getMetaDataArtifact( ModuleDependencyMetadata arg0, ArtifactType arg1 )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void listModuleVersions( ModuleDependencyMetadata arg0, BuildableModuleVersionListingResolveResult arg1 )
    {
        logger.debug( "listModuleVersions() called, but it is NOT IMPLEMENTED" );
    }
}
