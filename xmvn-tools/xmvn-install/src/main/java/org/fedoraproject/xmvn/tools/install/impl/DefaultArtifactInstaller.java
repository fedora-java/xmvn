/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.inject.Named;

import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
@Named
public class DefaultArtifactInstaller
    implements ArtifactInstaller
{
    private void putAttribute( Manifest manifest, String key, String value, String defaultValue )
    {
        if ( defaultValue == null || !value.equals( defaultValue ) )
        {
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue( key, value );
        }
    }

    private Artifact injectManifest( Artifact artifact, String version )
        throws IOException
    {
        File targetJar = Files.createTempFile( "xmvn", ".jar" ).toFile();
        targetJar.deleteOnExit();

        try (JarInputStream jis = new JarInputStream( new FileInputStream( artifact.getFile() ) ))
        {
            Manifest mf = jis.getManifest();
            if ( mf == null )
                return artifact;

            putAttribute( mf, ArtifactUtils.MF_KEY_GROUPID, artifact.getGroupId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_ARTIFACTID, artifact.getArtifactId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_EXTENSION, artifact.getExtension(), ArtifactUtils.DEFAULT_EXTENSION );
            putAttribute( mf, ArtifactUtils.MF_KEY_CLASSIFIER, artifact.getClassifier(), "" );
            putAttribute( mf, ArtifactUtils.MF_KEY_VERSION, version, ArtifactUtils.DEFAULT_VERSION );

            try (JarOutputStream jos = new JarOutputStream( new FileOutputStream( targetJar ), mf ))
            {
                byte[] buf = new byte[512];
                JarEntry entry;
                while ( ( entry = jis.getNextJarEntry() ) != null )
                {
                    jos.putNextEntry( entry );

                    int sz;
                    while ( ( sz = jis.read( buf ) ) > 0 )
                        jos.write( buf, 0, sz );
                }
            }
        }

        return artifact.setFile( targetJar );
    }

    @Override
    public void installArtifact( Package pkg, Artifact artifact, List<Artifact> jppArtifacts )
        throws IOException
    {
        Iterator<Artifact> jppIterator = jppArtifacts.iterator();
        Artifact primaryJppArtifact = jppIterator.next();
        artifact = injectManifest( artifact, primaryJppArtifact.getVersion() );
        pkg.addFile( artifact.getFile().toPath(), primaryJppArtifact.getFile().toPath(), 0644 );

        while ( jppIterator.hasNext() )
        {
            Artifact jppSymlinkArtifact = jppIterator.next();
            Path symlink = jppSymlinkArtifact.getFile().toPath();
            pkg.addSymlink( symlink, primaryJppArtifact.getFile().toPath() );
        }
    }
}
