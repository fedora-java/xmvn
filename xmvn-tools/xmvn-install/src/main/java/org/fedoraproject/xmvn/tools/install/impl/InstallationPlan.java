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
package org.fedoraproject.xmvn.tools.install.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Strings;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;

/**
 * @author Mikolaj Izdebski
 */
public class InstallationPlan
{
    private final PackageMetadata metadata;

    public InstallationPlan( Path planPath )
        throws ArtifactInstallationException
    {
        if ( !Files.exists( planPath ) )
        {
            metadata = new PackageMetadata();
        }
        else
        {
            try ( InputStream stream = Files.newInputStream( planPath ) )
            {
                metadata = new MetadataStaxReader().read( stream );
                validate( metadata );
            }
            catch ( IOException | XMLStreamException e )
            {
                throw new ArtifactInstallationException( "Unable to read reactor plan", e );
            }
        }
    }

    public List<ArtifactMetadata> getArtifacts()
    {
        return metadata.getArtifacts();
    }

    /**
     * Make sure that installation plan sets all required fields and that it doesn't contain unwanted data.
     * 
     * @param installationPlan the installation plan
     * @throws ArtifactInstallationException
     */
    private static void validate( PackageMetadata metadata )
        throws ArtifactInstallationException
    {
        if ( !Strings.isNullOrEmpty( metadata.getUuid() ) )
            throw new ArtifactInstallationException( "Installation plan must not set UUID" );

        for ( ArtifactMetadata artifactMetadata : metadata.getArtifacts() )
        {
            if ( Strings.isNullOrEmpty( artifactMetadata.getGroupId() ) )
                throw new ArtifactInstallationException( "Artifact metadata must have group ID set" );
            if ( Strings.isNullOrEmpty( artifactMetadata.getArtifactId() ) )
                throw new ArtifactInstallationException( "Artifact metadata must have artifact ID set" );
            if ( Strings.isNullOrEmpty( artifactMetadata.getVersion() ) )
                throw new ArtifactInstallationException( "Artifact metadata must have version set" );
            if ( Strings.isNullOrEmpty( artifactMetadata.getPath() ) )
                throw new ArtifactInstallationException( "Artifact metadata must have path set" );

            Path artifactPath = Paths.get( artifactMetadata.getPath() );
            if ( !artifactPath.isAbsolute() )
                throw new ArtifactInstallationException( "Artifact path is not absolute: " + artifactPath );
            if ( !Files.exists( artifactPath ) )
                throw new ArtifactInstallationException( "Artifact path points to a non-existent file: "
                    + artifactPath );
            if ( !Files.isRegularFile( artifactPath ) )
                throw new ArtifactInstallationException( "Artifact path points to a non-regular file: "
                    + artifactPath );
            if ( !Files.isReadable( artifactPath ) )
                throw new ArtifactInstallationException( "Artifact path points to a non-readable file: "
                    + artifactPath );

            if ( !Strings.isNullOrEmpty( artifactMetadata.getUuid() ) )
                throw new ArtifactInstallationException( "Installation plan must not define artifact UUID" );
            if ( !Strings.isNullOrEmpty( artifactMetadata.getNamespace() ) )
                throw new ArtifactInstallationException( "Installation plan must not define artifact namespace" );
            if ( artifactMetadata.getCompatVersions().iterator().hasNext() )
                throw new ArtifactInstallationException( "Installation plan must not define compat versions" );
            if ( artifactMetadata.getAliases().iterator().hasNext() )
                throw new ArtifactInstallationException( "Installation plan must not define aliases" );

            for ( Dependency dependency : artifactMetadata.getDependencies() )
            {
                if ( Strings.isNullOrEmpty( dependency.getGroupId() ) )
                    throw new ArtifactInstallationException( "Artifact dependency must have group ID set" );
                if ( Strings.isNullOrEmpty( dependency.getArtifactId() ) )
                    throw new ArtifactInstallationException( "Artifact dependency must have artifact ID set" );
                if ( Strings.isNullOrEmpty( dependency.getRequestedVersion() ) )
                    throw new ArtifactInstallationException( "Artifact dependency must have requested version set" );

                if ( !dependency.getResolvedVersion().equals( Artifact.DEFAULT_VERSION ) )
                    throw new ArtifactInstallationException( "Installation plan must not define resolved dependency version" );
                if ( !Strings.isNullOrEmpty( dependency.getNamespace() ) )
                    throw new ArtifactInstallationException( "Installation plan must not define dependency namespace" );

                for ( DependencyExclusion dependencyExclusion : dependency.getExclusions() )
                {
                    if ( Strings.isNullOrEmpty( dependencyExclusion.getGroupId() ) )
                        throw new ArtifactInstallationException( "Dependency exclusion must have group ID set" );
                    if ( Strings.isNullOrEmpty( dependencyExclusion.getArtifactId() ) )
                        throw new ArtifactInstallationException( "Dependency exclusion must have artifact ID set" );
                }
            }
        }

        if ( metadata.getSkippedArtifacts().iterator().hasNext() )
            throw new ArtifactInstallationException( "Installation plan must not include skipped artifacts" );
    }
}
