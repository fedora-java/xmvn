/*-
 * Copyright (c) 2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.MetadataResult;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactVisitorTest
{
    @TempDir
    private Path tempDir;

    private Path writeJar( String location, String gid, String aid, String ver )
        throws Exception
    {
        Path jarPath = tempDir.resolve( location );
        Files.createDirectories( jarPath.getParent() );
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue( "Manifest-Version", "1" );
        mf.getMainAttributes().putValue( "JavaPackages-GroupId", gid );
        mf.getMainAttributes().putValue( "JavaPackages-ArtifactId", aid );
        mf.getMainAttributes().putValue( "JavaPackages-Version", ver );
        try ( OutputStream os = Files.newOutputStream( jarPath ); JarOutputStream jos = new JarOutputStream( os, mf ) )
        {
            // No files, only manifest
        }
        return jarPath;
    }

    @Test
    public void testEmptyDirectory()
        throws Exception
    {
        Logger logger = EasyMock.createNiceMock( Logger.class );

        ArtifactMetadata am = new ArtifactMetadata();
        am.setPath( "/foo/bar" );
        MetadataResult mr = EasyMock.createMock( MetadataResult.class );
        EasyMock.expect( mr.getMetadataFor( new DefaultArtifact( "gid:aid:ver" ) ) ).andReturn( null ).times( 2 );
        EasyMock.expect( mr.getMetadataFor( new DefaultArtifact( "gid:aid:SYSTEM" ) ) ).andReturn( am ).times( 2 );
        EasyMock.expect( mr.getMetadataFor( new DefaultArtifact( "gid:other:ver" ) ) ).andReturn( null ).once();
        EasyMock.expect( mr.getMetadataFor( new DefaultArtifact( "gid:other:SYSTEM" ) ) ).andReturn( null ).once();

        Path jar1 = writeJar( "my.jar", "gid", "aid", "ver" );
        Path jar2 = writeJar( "sub/dir/my.jar", "gid", "aid", "ver" );
        Path jar3 = writeJar( "other.jar", "gid", "other", "ver" );

        ArtifactVisitor visitor = new ArtifactVisitor( logger, Arrays.asList( mr ) );
        visitor.setTypes( Collections.singleton( "jar" ) );

        EasyMock.replay( logger, mr );
        Files.walkFileTree( tempDir, visitor );
        EasyMock.verify( logger, mr );

        assertTrue( Files.isSymbolicLink( jar1 ) );
        assertTrue( Files.isSymbolicLink( jar2 ) );
        assertTrue( Files.isRegularFile( jar3, LinkOption.NOFOLLOW_LINKS ) );
    }
}
