/*-
 * Copyright (c) 2014-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxWriter;

/**
 * Class describing a Java package as a package which besides other files files also installs Java metadata as an
 * additional file.
 * 
 * @author Mikolaj Izdebski
 */
public class JavaPackage
    extends Package
{
    /**
     * Metadata associated with this package.
     */
    private final PackageMetadata metadata = new PackageMetadata();

    private final String basePackageName;

    private final Path metadataDir;

    /**
     * Create an empty Java package with given ID.
     * 
     * @param id package ID
     * @param basePackageName name of the source package
     * @param metadataDir installation directory for metadata relative to installation root
     */
    public JavaPackage( String id, String basePackageName, Path metadataDir )
    {
        super( id );
        this.basePackageName = basePackageName;
        this.metadataDir = metadataDir;
        metadata.setUuid( UUID.randomUUID().toString() );
    }

    /**
     * Create metadata contents split by namespace, so that artifacts with different namespaces don't have conflicting
     * metadata files.
     * 
     * @param namespace namespace name
     * @return new metadata with subset of artifacts
     */
    private PackageMetadata getSplitMetadata( String namespace )
    {
        PackageMetadata splitMetadata = new PackageMetadata();
        splitMetadata.setUuid( UUID.randomUUID().toString() );
        splitMetadata.setProperties( metadata.getProperties() );
        List<ArtifactMetadata> allArtifacts = metadata.getArtifacts();
        List<ArtifactMetadata> splitArtifacts =
            allArtifacts.stream().filter( a -> namespace.equals( a.getNamespace() ) ).collect( Collectors.toList() );
        splitMetadata.setArtifacts( splitArtifacts );
        splitMetadata.setSkippedArtifacts( metadata.getSkippedArtifacts() );
        return splitMetadata;
    }

    private byte[] getMetadataContents( String namespace )
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new MetadataStaxWriter().write( bos, getSplitMetadata( namespace ) );
            return bos.toByteArray();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to generate package metadata", e );
        }
    }

    private Set<String> getNamespaces()
    {
        Set<String> namespaces = new LinkedHashSet<>();
        for ( ArtifactMetadata am : getMetadata().getArtifacts() )
        {
            namespaces.add( am.getNamespace() );
        }
        if ( namespaces.isEmpty() )
        {
            namespaces.add( "" );
        }
        return namespaces;
    }

    @Override
    public Set<File> getFiles()
    {
        Set<File> allFiles = new LinkedHashSet<>( super.getFiles() );
        for ( String namespace : getNamespaces() )
        {
            String metadataName = namespace + ( namespace.isEmpty() ? "" : "-" ) + basePackageName
                + ( getId().isEmpty() ? "" : "-" ) + getId();
            Path metadataPath = metadataDir.resolve( metadataName + ".xml" );
            File metadataFile = new RegularFile( metadataPath, () -> getMetadataContents( namespace ) );
            allFiles.add( metadataFile );
        }
        return Collections.unmodifiableSet( allFiles );
    }

    /**
     * Get metadata associated with this package.
     * 
     * @return package metadata object
     */
    public PackageMetadata getMetadata()
    {
        return metadata;
    }
}
