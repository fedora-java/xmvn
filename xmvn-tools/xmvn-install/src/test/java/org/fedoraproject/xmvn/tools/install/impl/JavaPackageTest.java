/*-
 * Copyright (c) 2014-2023 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;
import org.fedoraproject.xmvn.tools.install.JavaPackage;
import org.fedoraproject.xmvn.tools.install.RegularFile;

/**
 * @author Michael Simacek
 */
public class JavaPackageTest
    extends AbstractFileTest
{
    @Test
    public void testJavaPackage()
        throws Exception
    {
        JavaPackage pkg = new JavaPackage( "my-id", "my-pkg", Paths.get( "usr/share/maven-metadata" ) );
        assertEquals( "my-id", pkg.getId() );

        pkg.install( installRoot );
        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/maven-metadata",
                                  "F /usr/share/maven-metadata/my-pkg-my-id.xml" );
        assertDescriptorEquals( pkg, "%attr(0644,root,root) /usr/share/maven-metadata/my-pkg-my-id.xml" );
    }

    @Test
    public void testJavaPackageMetadata()
        throws Exception
    {
        JavaPackage pkg = new JavaPackage( "my-id", "my-pkg", Paths.get( "usr/share/maven-metadata" ) );

        PackageMetadata inputMetadata = pkg.getMetadata();
        inputMetadata.getProperties().put( "foo", "bar" );

        pkg.install( installRoot );

        PackageMetadata actualMetadata =
            new MetadataStaxReader().read( installRoot.resolve( "usr/share/maven-metadata/my-pkg-my-id.xml" ).toString(),
                                           true );
        assertTrue( actualMetadata.getProperties().containsKey( "foo" ) );
    }

    @Test
    public void testJavaPackageMetadataSplit()
        throws Exception
    {
        JavaPackage pkg = new JavaPackage( "my-id", "my-pkg", Paths.get( "usr/share/maven-metadata" ) );

        ArtifactMetadata foo = new ArtifactMetadata();
        foo.setGroupId( "foo" );
        foo.setArtifactId( "foo" );
        foo.setNamespace( "foo" );
        pkg.getMetadata().addArtifact( foo );
        ArtifactMetadata bar = new ArtifactMetadata();
        bar.setGroupId( "bar" );
        bar.setArtifactId( "bar" );
        bar.setNamespace( "bar" );
        pkg.getMetadata().addArtifact( bar );

        pkg.install( installRoot );

        assertMetadataEqual( getResource( "ns-foo.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/foo-my-pkg-my-id.xml" ) );
        assertMetadataEqual( getResource( "ns-bar.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/bar-my-pkg-my-id.xml" ) );
    }

    @Test
    public void testSpacesInFileNames()
        throws Exception
    {
        JavaPackage pkg = new JavaPackage( "", "space-test", Paths.get( "usr/share/maven-metadata" ) );
        pkg.addFile( new RegularFile( Paths.get( "usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/META-INF/MANIFEST.MF" ),
                                      new byte[0] ) );
        pkg.addFile( new RegularFile( Paths.get( "usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/file with spaces" ),
                                      new byte[0] ) );
        pkg.addFile( new RegularFile( Paths.get( "usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/other\twhitespace" ),
                                      new byte[0] ) );
        pkg.addFile( new RegularFile( Paths.get( "usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/other\u000Bwhitespace" ),
                                      new byte[0] ) );
        assertDescriptorEquals( pkg,
                                "%attr(0644,root,root) /usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/META-INF/MANIFEST.MF",
                                "%attr(0644,root,root) \"/usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/file with spaces\"",
                                "%attr(0644,root,root) \"/usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/other\twhitespace\"",
                                "%attr(0644,root,root) \"/usr/share/eclipse/droplets/space-test/plugins/space-test_1.0.0/other\u000Bwhitespace\"",
                                "%attr(0644,root,root) /usr/share/maven-metadata/space-test.xml" );
    }
}
