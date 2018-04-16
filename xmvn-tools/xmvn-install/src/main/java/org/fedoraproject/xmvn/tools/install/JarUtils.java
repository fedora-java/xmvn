/*-
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public final class JarUtils
{
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private static final Logger LOGGER = LoggerFactory.getLogger( JarUtils.class );

    // From /usr/include/linux/elf.h
    private static final int ELFMAG0 = 0x7F;

    private static final int ELFMAG1 = 'E';

    private static final int ELFMAG2 = 'L';

    private static final int ELFMAG3 = 'F';

    private JarUtils()
    {
        // Avoid generating default public constructor
    }

    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file contains native (architecture-dependent)
     * code.
     * <p>
     * Currently this code only checks only for ELF binaries, but that behavior can change in future.
     * 
     * @return {@code true} if native code was found inside given JAR
     */
    public static boolean containsNativeCode( Path jar )
    {
        try ( ZipInputStream jis = new ZipInputStream( Files.newInputStream( jar ) ) )
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() )
                    continue;
                if ( jis.read() == ELFMAG0 && jis.read() == ELFMAG1 && jis.read() == ELFMAG2 && jis.read() == ELFMAG3 )
                {
                    LOGGER.debug( "Native code found inside {}: {}", jar, ent.getName() );
                    return true;
                }
            }

            LOGGER.trace( "Native code not found inside {}", jar );
            return false;
        }
        catch ( IOException e )
        {
            LOGGER.debug( "I/O exception caught when trying to determine whether JAR contains native code: {}", jar,
                          e );
            return false;
        }
    }

    static class NativeMethodFound
        extends RuntimeException
    {
        private static final long serialVersionUID = 1;

        final String className;

        final String methodName;

        final String methodSignature;

        NativeMethodFound( String className, String methodName, String methodSignature )
        {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
        }
    }

    /**
     * Heuristically try to determine whether given JAR (or WAR, EAR, ...) file is using native (architecture-dependent)
     * code.
     * <p>
     * Currently this code only checks if any class file declares Java native methods, but that behavior can change in
     * future.
     * 
     * @return {@code true} given JAR as found inside to use native code
     */
    public static boolean usesNativeCode( Path jar )
    {
        try ( ZipInputStream jis = new ZipInputStream( Files.newInputStream( jar ) ) )
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                final String entryName = ent.getName();
                if ( ent.isDirectory() || !entryName.endsWith( ".class" ) )
                    continue;

                new ClassReader( jis ).accept( new ClassVisitor( Opcodes.ASM4 )
                {
                    @Override
                    public MethodVisitor visitMethod( int flags, String name, String desc, String sig, String[] exc )
                    {
                        if ( ( flags & Opcodes.ACC_NATIVE ) != 0 )
                            throw new NativeMethodFound( entryName, name, sig );

                        return super.visitMethod( flags, name, desc, sig, exc );
                    }
                }, ClassReader.SKIP_CODE );
            }

            return false;
        }
        catch ( NativeMethodFound e )
        {
            LOGGER.debug( "Native method {}({}) found in {}: {}", e.methodName, e.methodSignature, jar, e.className );
            return true;
        }
        catch ( IOException e )
        {
            LOGGER.debug( "I/O exception caught when trying to determine whether JAR uses native code: {}", jar, e );
            return false;
        }
        catch ( RuntimeException e )
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
            LOGGER.trace( "Injected field {}: {}", key, value );
        }
        else
        {
            LOGGER.trace( "Not injecting field {} (it has default value \"{}\")", key, defaultValue );
        }
    }

    private static void updateManifest( Artifact artifact, Manifest mf )
    {
        putAttribute( mf, Artifact.MF_KEY_GROUPID, artifact.getGroupId(), null );
        putAttribute( mf, Artifact.MF_KEY_ARTIFACTID, artifact.getArtifactId(), null );
        putAttribute( mf, Artifact.MF_KEY_EXTENSION, artifact.getExtension(), Artifact.DEFAULT_EXTENSION );
        putAttribute( mf, Artifact.MF_KEY_CLASSIFIER, artifact.getClassifier(), "" );
        putAttribute( mf, Artifact.MF_KEY_VERSION, artifact.getVersion(), Artifact.DEFAULT_VERSION );
    }

    /**
     * Inject artifact coordinates into manifest of specified JAR (or WAR, EAR, ...) file. The file is modified
     * in-place.
     * 
     * @param targetJar
     * @param artifact
     */
    public static void injectManifest( Path targetJar, Artifact artifact )
    {
        LOGGER.trace( "Trying to inject manifest to {}", artifact );
        try
        {
            try ( ZipFile jar = new ZipFile( targetJar.toFile() ) )
            {
                ZipArchiveEntry manifestEntry = jar.getEntry( MANIFEST_PATH );
                if ( manifestEntry != null )
                {
                    Manifest mf = new Manifest( jar.getInputStream( manifestEntry ) );
                    updateManifest( artifact, mf );
                    Path tempJar = Files.createTempFile( "xmvn", ".tmp" );
                    try ( ZipArchiveOutputStream os = new ZipArchiveOutputStream( tempJar.toFile() ) )
                    {
                        // write manifest
                        ZipArchiveEntry newManifestEntry = new ZipArchiveEntry( MANIFEST_PATH );
                        os.putArchiveEntry( newManifestEntry );
                        mf.write( os );
                        os.closeArchiveEntry();
                        // copy the rest of content
                        jar.copyRawEntries( os, entry -> !entry.equals( manifestEntry ) );
                    }
                    catch ( IOException e )
                    {
                        // Re-throw exceptions that occur when processing JAR file after reading header and manifest.
                        throw new RuntimeException( e );
                    }
                    Files.move( tempJar, targetJar, StandardCopyOption.REPLACE_EXISTING );
                    LOGGER.trace( "Manifest injected successfully" );
                }
                else
                {
                    LOGGER.trace( "Manifest injection skipped: no pre-existing manifest found to update" );
                    return;
                }
            }
        }
        catch ( IOException e )
        {
            LOGGER.debug( "I/O exception caught when trying to read JAR: {}", targetJar );
        }
    }
}
