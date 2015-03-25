/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepositoryAccess;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.DescriptorParseContext;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.GradlePomModuleDescriptorParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.MetaDataParser;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionSelectorScheme;
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.internal.component.external.model.DefaultMavenModuleResolveMetaData;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactMetaData;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetaData;
import org.gradle.internal.component.external.model.MutableModuleComponentResolveMetaData;
import org.gradle.internal.component.model.ComponentArtifactMetaData;
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
import org.gradle.internal.resolve.result.BuildableModuleComponentVersionSelectionResolveResult;
import org.gradle.internal.resource.DefaultLocallyAvailableExternalResource;
import org.gradle.internal.resource.LocallyAvailableExternalResource;
import org.gradle.internal.resource.local.DefaultLocallyAvailableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    implements ArtifactRepository, ResolutionAwareRepository, ConfiguredModuleComponentRepository,
    ModuleComponentRepositoryAccess, DescriptorParseContext
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

    private String name;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
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
    public void listModuleVersions( DependencyMetaData arg0, BuildableModuleComponentVersionSelectionResolveResult arg1 )
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
            result.failed( new ArtifactResolveException( artifact.getId(), "XMvn was unable to resolve artifact "
                + artifact2 ) );
            return;
        }

        logger.debug( "Artifact {} was resolved to {}", artifact2, path );
        result.resolved( path.toFile() );
    }

    @Override
    public void resolveComponentMetaData( DependencyMetaData dependency, ModuleComponentIdentifier id,
                                          BuildableModuleComponentMetaDataResolveResult result )
    {
        logger.debug( "Trying to resolve model for {}:{}:{}", id.getGroup(), id.getModule(), id.getVersion() );

        Artifact artifact2 = new DefaultArtifact( id.getGroup(), id.getModule(), "pom", id.getVersion() );
        Path pomPath = resolve( artifact2 );

        if ( pomPath != null )
        {
            logger.debug( "Found Maven POM: {}", pomPath );
            Path fakePom = fakePom( pomPath, id );
            logger.debug( "Created fake POM: {}", fakePom );

            MetaDataParser parser = new GradlePomModuleDescriptorParser( new DefaultVersionSelectorScheme() );
            MutableModuleComponentResolveMetaData metaData = parser.parseMetaData( this, fakePom.toFile() );

            result.resolved( metaData );
            return;
        }
        else
        {
            logger.debug( "POM not found, trying non-POM artifacts" );
            for ( IvyArtifactName artifact : getDependencyArtifactNames( dependency ) )
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
                    MutableModuleComponentResolveMetaData metaData = new DefaultMavenModuleResolveMetaData( dependency );
                    result.resolved( metaData );
                    return;
                }
            }
        }

        logger.debug( "No POM and no artifact found, failing" );
        result.failed( new ModuleVersionResolveException( id, "XMvn was unable to resolve artifact " + artifact2 ) );
    }

    private Set<IvyArtifactName> getDependencyArtifactNames( DependencyMetaData dependency )
    {
        String moduleName = dependency.getRequested().getName();
        Set<IvyArtifactName> artifactSet = new LinkedHashSet<>();
        artifactSet.addAll( dependency.getArtifacts() );

        if ( artifactSet.isEmpty() )
        {
            artifactSet.add( new DefaultIvyArtifactName( moduleName, "jar", "jar",
                                                         Collections.<String, String> emptyMap() ) );
        }

        return artifactSet;
    }

    private static void setElement( Document doc, String name, String value )
    {
        NodeList childreen = doc.getDocumentElement().getChildNodes();
        for ( int i = 0; i < childreen.getLength(); i++ )
        {
            Node child = childreen.item( i );
            if ( child.getNodeName().equals( name ) )
            {
                child.setTextContent( value );
                return;
            }
        }

        Node child = doc.createElement( name );
        child.setTextContent( value );
        doc.getDocumentElement().appendChild( child );
    }

    private static Path fakePom( Path pom, ModuleComponentIdentifier moduleVersionIdentifier )
    {
        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            Document doc = domFactory.newDocumentBuilder().parse( pom.toFile() );
            setElement( doc, "groupId", moduleVersionIdentifier.getGroup() );
            setElement( doc, "artifactId", moduleVersionIdentifier.getModule() );
            setElement( doc, "version", moduleVersionIdentifier.getVersion() );

            NodeList dependencies = doc.getElementsByTagName( "dependency" );
            outer: for ( int i = 0; i < dependencies.getLength(); i++ )
            {
                Node dependency = dependencies.item( i );
                NodeList childreen = dependency.getChildNodes();
                for ( int j = 0; j < childreen.getLength(); j++ )
                {
                    Node child = childreen.item( j );
                    if ( child.getNodeName().equals( "version" ) )
                        continue outer;
                }

                Node child = doc.createElement( "version" );
                child.setTextContent( "SYSTEM" );
                dependency.appendChild( child );
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            DOMSource source = new DOMSource( doc );

            Path fakePom = Files.createTempFile( "xmvn-", ".gradle.pom" );
            StreamResult file = new StreamResult( fakePom.toFile() );
            transformer.transform( source, file );
            return fakePom;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
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
        DefaultModuleComponentArtifactMetaData resolvedMetaData = new DefaultModuleComponentArtifactMetaData( id, name );
        result.resolved( Collections.singleton( resolvedMetaData ) );
    }

    @Override
    public LocallyAvailableExternalResource getMetaDataArtifact( ModuleVersionIdentifier id, ArtifactType type )
    {
        Path pomPath = resolve( new DefaultArtifact( id.getGroup(), id.getName(), "pom", id.getVersion() ) );

        if ( pomPath == null )
            return null;

        return new DefaultLocallyAvailableExternalResource( pomPath.toUri(),
                                                            new DefaultLocallyAvailableResource( pomPath.toFile() ) );
    }
}
