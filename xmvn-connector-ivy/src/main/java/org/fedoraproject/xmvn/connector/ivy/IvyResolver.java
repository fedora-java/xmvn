/*-
 * Copyright (c) 2013-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.ivy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorParser;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.file.FileRepository;
import org.apache.ivy.plugins.repository.file.FileResource;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;

import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;

/**
 * Resolve and publish Ivy artifacts by delegating most tasks to XMvn.
 * 
 * @author Mikolaj Izdebski
 */
public class IvyResolver
    extends AbstractResolver
{
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

    static class LazyDeployerProvider
    {
        static final Deployer DEPLOYER = LazyLocatorProvider.LOCATOR.getService( Deployer.class );
    }

    private Resolver resolver;

    private Deployer deployer;

    public IvyResolver()
    {
        setName( "XMvn" );
    }

    public Resolver getResolver()
    {
        return resolver != null ? resolver : LazyResolverProvider.RESOLVER;
    }

    public void setResolver( Resolver resolver )
    {
        this.resolver = resolver;
    }

    public Deployer getDeployer()
    {
        return deployer != null ? deployer : LazyDeployerProvider.DEPLOYER;
    }

    public void setDeployer( Deployer deployer )
    {
        this.deployer = deployer;
    }

    private static org.fedoraproject.xmvn.artifact.Artifact ivy2aether( ModuleRevisionId revision, String extension )
    {
        String groupId = revision.getOrganisation();
        String artifactId = revision.getName();
        String classifier = revision.getExtraAttribute( "classifier" );
        String version = revision.getRevision();

        return new org.fedoraproject.xmvn.artifact.DefaultArtifact( groupId, artifactId, extension, classifier,
                                                                    version );
    }

    static org.fedoraproject.xmvn.artifact.Artifact ivy2aether( org.apache.ivy.core.module.descriptor.Artifact artifact )
    {
        ModuleRevisionId revision = artifact.getModuleRevisionId();

        String groupId = revision.getOrganisation();
        String artifactId = artifact.getName();
        String extension = artifact.getExt();
        String classifier = artifact.getExtraAttribute( "classifier" );
        String version = revision.getRevision();
        String type = artifact.getType();

        if ( classifier == null || classifier.isEmpty() )
        {
            if ( extension.equals( "jar" ) && type.equals( "source" ) )
                classifier = "sources";
            else if ( extension.equals( "jar" ) && type.equals( "javadoc" ) )
                classifier = "javadoc";
        }

        return new org.fedoraproject.xmvn.artifact.DefaultArtifact( groupId, artifactId, extension, classifier,
                                                                    version );
    }

    private static String resolvedVersion( ResolutionResult resolutionResult )
    {
        String version = resolutionResult.getCompatVersion();
        return version != null ? version : "SYSTEM";
    }

    private String resolveModuleVersion( ModuleDescriptor module )
    {
        for ( Artifact artifact : module.getAllArtifacts() )
        {
            ResolutionRequest request = new ResolutionRequest( ivy2aether( artifact ) );
            ResolutionResult result = getResolver().resolve( request );
            if ( result.getArtifactPath() != null )
                return resolvedVersion( result );
        }

        return null;
    }

    private ModuleDescriptor readIvyModuleDescriptorFromPom( DependencyDescriptor depDescriptor )
        throws IOException, ParseException
    {
        ModuleRevisionId depId = depDescriptor.getDependencyRevisionId();

        ResolutionRequest request = new ResolutionRequest();
        request.setArtifact( ivy2aether( depId, "pom" ) );
        ResolutionResult result = getResolver().resolve( request );
        Path pomPath = result.getArtifactPath();

        String version;
        ModuleDescriptor module;
        if ( pomPath != null )
        {
            ModuleDescriptorParser parser = PomModuleDescriptorParser.getInstance();
            module = parser.parseDescriptor( getSettings(), pomPath.toFile().toURI().toURL(), false );
            version = resolvedVersion( result );
        }
        else
        {
            module = DefaultModuleDescriptor.newDefaultInstance( depId, depDescriptor.getAllDependencyArtifacts() );
            version = resolveModuleVersion( module );
            if ( version == null )
                return null;
        }

        module.setResolvedModuleRevisionId( ModuleRevisionId.newInstance( depId, version ) );
        return module;
    }

    @Override
    public ResolvedModuleRevision getDependency( DependencyDescriptor systemDd, ResolveData data )
        throws ParseException
    {
        try
        {
            ModuleDescriptor module = readIvyModuleDescriptorFromPom( systemDd );
            if ( module == null )
                return null;

            MetadataArtifactDownloadReport report = new MetadataArtifactDownloadReport( module.getMetadataArtifact() );
            report.setDownloadStatus( DownloadStatus.NO );
            report.setSearched( true );

            return new ResolvedModuleRevision( this, this, module, report, true );
        }
        catch ( FileNotFoundException e )
        {
            return null;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public ResolvedResource findIvyFileRef( DependencyDescriptor depDescriptor, ResolveData data )
    {
        Artifact artifact = DefaultArtifact.newIvyArtifact( depDescriptor.getDependencyRevisionId(), null );

        ResolutionRequest request = new ResolutionRequest();
        request.setArtifact( ivy2aether( artifact.getModuleRevisionId(), "pom" ) );
        ResolutionResult result = getResolver().resolve( request );
        Path pomPath = result.getArtifactPath();
        if ( pomPath == null )
            return null;

        Resource fileResource = new FileResource( new FileRepository(), pomPath.toFile() );
        return new ResolvedResource( fileResource, resolvedVersion( result ) );
    }

    @Override
    public DownloadReport download( Artifact[] artifacts, DownloadOptions options )
    {
        DownloadReport report = new DownloadReport();

        for ( Artifact artifact : artifacts )
        {
            ArtifactDownloadReport artifactReport = new ArtifactDownloadReport( artifact );
            ResolutionRequest request = new ResolutionRequest();
            request.setArtifact( ivy2aether( artifact ) );
            ResolutionResult result = getResolver().resolve( request );
            Path artifactPath = result.getArtifactPath();

            if ( artifactPath != null )
            {
                artifactReport.setArtifactOrigin( new ArtifactOrigin( artifact, false, artifactPath.toString() ) );
                artifactReport.setLocalFile( artifactPath.toFile() );
                artifactReport.setDownloadStatus( DownloadStatus.SUCCESSFUL );
            }
            else
            {
                artifactReport.setDownloadStatus( DownloadStatus.FAILED );
            }

            report.addArtifactReport( artifactReport );
        }

        return report;
    }

    private void deploy( org.fedoraproject.xmvn.artifact.Artifact artifact, String type, Path artifactPath )
        throws IOException
    {
        DeploymentRequest request = new DeploymentRequest();
        request.setArtifact( artifact.setPath( artifactPath ) );
        if ( type != null )
            request.addProperty( "type", type );
        DeploymentResult result = getDeployer().deploy( request );
        if ( result.getException() != null )
            throw new IOException( "Failed to publish artifact", result.getException() );
    }

    private void deployEffectivePom( ModuleRevisionId moduleRevisionId, Path artifactPath )
        throws IOException
    {
        try
        {
            File pomFile = artifactPath.resolveSibling( artifactPath.getName( artifactPath.getNameCount() - 1 )
                + "-xmvn.pom" ).toFile();
            ModuleDescriptorParser parser = XmlModuleDescriptorParser.getInstance();
            ModuleDescriptor module =
                parser.parseDescriptor( getSettings(), artifactPath.toFile().toURI().toURL(), false );
            PomModuleDescriptorWriter.write( module, pomFile, new PomWriterOptions() );

            org.fedoraproject.xmvn.artifact.Artifact artifact = ivy2aether( moduleRevisionId, "pom" );
            deploy( artifact, null, artifactPath );
        }
        catch ( ParseException e )
        {
            throw new IOException( e );
        }
    }

    @Override
    public void publish( Artifact artifact, File artifactFile, boolean overwrite )
        throws IOException
    {
        if ( artifact.getExt().equals( "xml" ) && artifact.getType().equals( "ivy" ) )
        {
            deployEffectivePom( artifact.getModuleRevisionId(), artifactFile.toPath() );
        }

        deploy( ivy2aether( artifact ), artifact.getType(), artifactFile.toPath() );
    }
}
