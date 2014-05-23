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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
class JarUtils
{
    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file contains native (architecture-dependant)
     * code.
     * <p>
     * Currently this code only checks only for ELF binaries, but that behavior can change in future.
     * 
     * @return {@code true} if native code was found inside given JAR
     */
    public static boolean containsNativeCode( Path jar )
    {
        // From /usr/include/linux/elf.h
        final int ELFMAG0 = 0x7F;
        final int ELFMAG1 = 'E';
        final int ELFMAG2 = 'L';
        final int ELFMAG3 = 'F';

        try (ZipInputStream jis = new ZipInputStream( Files.newInputStream( jar ) ))
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() )
                    continue;
                if ( jis.read() == ELFMAG0 && jis.read() == ELFMAG1 && jis.read() == ELFMAG2 && jis.read() == ELFMAG3 )
                    return true;
            }

            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file is using native (architecture-dependant)
     * code.
     * <p>
     * Currently this code only checks if any class file declares Java native methods, but that behavior can change in
     * future.
     * 
     * @return {@code true} given JAR as found inside to use native code
     */
    public static boolean usesNativeCode( Path jar )
    {
        try (ZipInputStream jis = new ZipInputStream( Files.newInputStream( jar ) ))
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() || !ent.getName().endsWith( ".class" ) )
                    continue;

                final boolean[] usesNativeCode = new boolean[1];

                new ClassReader( jis ).accept( new ClassVisitor( Opcodes.ASM4 )
                {
                    @Override
                    public MethodVisitor visitMethod( int flags, String name, String desc, String sig, String[] exc )
                    {
                        usesNativeCode[0] = ( flags & Opcodes.ACC_NATIVE ) != 0;
                        return super.visitMethod( flags, name, desc, sig, exc );
                    }
                }, ClassReader.SKIP_CODE );

                if ( usesNativeCode[0] )
                    return true;
            }

            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    private static void putAttribute( Manifest manifest, String key, String value, String defaultValue )
    {
        if ( defaultValue == null || !value.equals( defaultValue ) )
        {
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue( key, value );
        }
    }

    /**
     * Inject artifact coordinates into manifest of specified JAR (or WAR, EAR, ...) file. The file is modified
     * in-place.
     * 
     * @param targetJar
     * @param artifact
     * @throws IOException
     */
    public static void injectManifest( Path targetJar, Artifact artifact )
        throws IOException
    {
        targetJar = targetJar.toRealPath();

        try (JarInputStream jis = new JarInputStream( Files.newInputStream( targetJar ) ))
        {
            Manifest mf = jis.getManifest();
            if ( mf == null )
                return;

            putAttribute( mf, ArtifactUtils.MF_KEY_GROUPID, artifact.getGroupId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_ARTIFACTID, artifact.getArtifactId(), null );
            putAttribute( mf, ArtifactUtils.MF_KEY_EXTENSION, artifact.getExtension(), Artifact.DEFAULT_EXTENSION );
            putAttribute( mf, ArtifactUtils.MF_KEY_CLASSIFIER, artifact.getClassifier(), "" );
            putAttribute( mf, ArtifactUtils.MF_KEY_VERSION, artifact.getVersion(), Artifact.DEFAULT_VERSION );

            Files.delete( targetJar );

            try (JarOutputStream jos = new JarOutputStream( Files.newOutputStream( targetJar ), mf ))
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
    }
}
