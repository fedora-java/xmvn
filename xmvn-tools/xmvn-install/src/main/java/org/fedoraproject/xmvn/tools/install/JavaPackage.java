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
package org.fedoraproject.xmvn.tools.install;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

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

    private final Path sourcePath;

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

        try
        {
            this.sourcePath = Files.createTempFile( "xmvn-metadata", "xml" );
            File metadataFile = new RegularFile( metadataPath, sourcePath );
            addFile( metadataFile );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
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
        writeMetadataFile();

        super.install( installRoot );
    }

    private void writeMetadataFile()
        throws IOException
    {
        try (OutputStream stream = Files.newOutputStream( sourcePath ))
        {
            new MetadataStaxWriter().write( stream, metadata );
        }
        catch ( XMLStreamException e )
        {
            throw new IOException( "Failed to write package metadata", e );
        }
    }
}
