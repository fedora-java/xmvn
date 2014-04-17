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
import java.nio.file.Path;
import java.nio.file.Paths;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Michael Simacek
 */
public class JavaPackageTest
        extends AbstractFileTest
{
    @Test
    public void testJavaPackage()
            throws Exception
    {
        JavaPackage pkg = new JavaPackage( "my-id", Paths.get( "usr/share/maven-metadata/my-id.xml" ) );
        assertEquals( "my-id", pkg.getId() );

        performInstallation( pkg );
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/maven-metadata",
                "F /usr/share/maven-metadata/my-id.xml" );
        assertDescriptorEquals( pkg, "%attr(0644,root,root) /usr/share/maven-metadata/my-id.xml" );
    }

    @Test
    public void testJavaPackageMetadata()
            throws Exception
    {
        Path metadataPath = Paths.get( "usr/share/maven-metadata/my-id.xml" );
        JavaPackage pkg = new JavaPackage( "my-id", metadataPath );

        PackageMetadata inputMetadata = pkg.getMetadata();
        inputMetadata.setUuid( "test-uuid" );

        Path root = performInstallation( pkg );

        PackageMetadata actualMetadata = new MetadataStaxReader().read( root.resolve( metadataPath ).toString(), true );
        assertEquals( "test-uuid", actualMetadata.getUuid() );
    }
}
