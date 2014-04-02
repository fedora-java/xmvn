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
package org.fedoraproject.xmvn.resolver.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataReaderTest
{
    private MetadataReader reader;

    @Before
    public void setUp()
    {
        reader = new MetadataReader();
    }

    /**
     * Test if trying to read metadata from empty list of directories returns empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingEmptyList()
        throws Exception
    {
        List<PackageMetadata> list = reader.readMetadata( Collections.<String> emptyList() );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }

    /**
     * Test if trying to read metadata from empty directory returns empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingEmptyDirectory()
        throws Exception
    {
        Path dir = Files.createTempDirectory( "xmvn-test" );
        List<PackageMetadata> list = reader.readMetadata( Collections.singletonList( dir.toString() ) );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }

    /**
     * Test if trying to read metadata from depmap file empty result.
     * 
     * @throws Exception
     */
    @Test
    public void testReadingDepmap()
        throws Exception
    {
        List<PackageMetadata> list =
            reader.readMetadata( Collections.singletonList( "src/test/resources/xml-depmap.xml" ) );
        assertNotNull( list );
        assertTrue( list.isEmpty() );
    }
}
