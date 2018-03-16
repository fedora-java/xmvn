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
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

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

    /**
     * Create an empty Java package with given ID.
     * 
     * @param id package ID
     * @param metadataPath installation path for metadata relative to installation root
     * @throws IOException
     */
    public JavaPackage( String id, Path metadataPath )
    {
        super( id );

        metadata.setUuid( UUID.randomUUID().toString() );

        File metadataFile = new RegularFile( metadataPath, () -> getMetadataContents() );
        addFile( metadataFile );
    }

    private byte[] getMetadataContents()
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new MetadataStaxWriter().write( bos, metadata );
            return bos.toByteArray();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to generate package metadata", e );
        }
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

    @Override
    public void install( Path installRoot )
        throws IOException
    {
        super.install( installRoot );
    }
}
