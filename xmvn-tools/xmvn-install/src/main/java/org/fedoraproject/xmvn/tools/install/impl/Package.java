/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.io.Files;

import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * Class describing a binary package as a set of files with associated metadata.
 * 
 * @author Mikolaj Izdebski
 */
class Package
{
    /**
     * ID of main package.
     */
    public static String MAIN = "";

    /**
     * Package ID (a unique string).
     */
    private final String id;

    /**
     * List of files that will be installed into this package.
     */
    private final Set<File> files = new LinkedHashSet<>();

    /**
     * Metadata associated with this package.
     */
    private final PackageMetadata metadata = new PackageMetadata();

    /**
     * Create an empty package with given ID.
     * 
     * @param id package ID
     */
    public Package( String id )
    {
        this.id = id;
    }

    /**
     * Get unique string identifying this package.
     * 
     * @return package ID
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get files contained in this package.
     * 
     * @return list view of files that will be installed with this package
     */
    public Set<File> getFiles()
    {
        return Collections.unmodifiableSet( files );
    }

    /**
     * Add a file to this package.
     * 
     * @param file file to be added
     */
    public void addFile( File file )
    {
        files.add( file );
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

    /**
     * Install this package into specified root.
     * <p>
     * Package installation is equivalent to installation of all files it contains.
     * <p>
     * Target directory won't be overwritten if it already exists, which allows installation of multiple packages into
     * the same directory.
     * 
     * @param installRoot target directory where package files will be installed
     * @throws IOException
     */
    public void install( Path installRoot )
        throws IOException
    {
        for ( File file : files )
            file.install( installRoot );
    }

    /**
     * Write package descriptor (aka {@code mfiles}) into specified file.
     * <p>
     * If target file exists then it shall be overwritten.
     * 
     * @param descriptorPath path to file into which descriptor shall be written
     * @throws IOException
     */
    public void writeDescriptor( Path descriptorPath )
        throws IOException
    {
        try (Writer writer = Files.newWriter( descriptorPath.toFile(), StandardCharsets.UTF_8 ))
        {
            for ( File file : files )
            {
                writer.write( file.getDescriptor() );
                writer.write( '\n' );
            }
        }
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object rhs )
    {
        return rhs != null && getClass() == rhs.getClass() && id.equals( ( (Package) rhs ).id );
    }
}
