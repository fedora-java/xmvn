/*-
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
    public static boolean containsNativeCode( Path jarPath )
    {
        try ( ZipFile jar = new ZipFile( jarPath.toFile() ) )
        {
            Enumeration<ZipArchiveEntry> entries = jar.getEntries();
            while ( entries.hasMoreElements() )
            {
                ZipArchiveEntry entry = entries.nextElement();
                if ( entry.isDirectory() )
                    continue;
                try ( InputStream jis = jar.getInputStream( entry ) )
                {
                    if ( jis.read() == ELFMAG0 && jis.read() == ELFMAG1 && jis.read() == ELFMAG2
                        && jis.read() == ELFMAG3 )
                    {
                        LOGGER.debug( "Native code found inside {}: {}", jarPath, entry.getName() );
                        return true;
                    }
                }
            }

            LOGGER.trace( "Native code not found inside {}", jarPath );
            return false;
        }
        catch ( IOException e )
        {
            LOGGER.debug( "I/O exception caught when trying to determine whether JAR contains native code: {}", jarPath,
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
    public static boolean usesNativeCode( Path jarPath )
    {
        try ( ZipFile jar = new ZipFile( jarPath.toFile() ) )
        {
            Enumeration<ZipArchiveEntry> entries = jar.getEntries();
            while ( entries.hasMoreElements() )
            {
                ZipArchiveEntry entry = entries.nextElement();
                final String entryName = entry.getName();
                if ( entry.isDirectory() || !entryName.endsWith( ".class" ) )
                    continue;

                try ( InputStream jis = jar.getInputStream( entry ) )
                {
                    new ClassReader( jis ).accept( new ClassVisitor( Opcodes.ASM4 )
                    {
                        @Override
                        public MethodVisitor visitMethod( int flags, String name, String desc, String sig,
                                                          String[] exc )
                        {
                            if ( ( flags & Opcodes.ACC_NATIVE ) != 0 )
                                throw new NativeMethodFound( entryName, name, sig );

                            return super.visitMethod( flags, name, desc, sig, exc );
                        }
                    }, ClassReader.SKIP_CODE );
                }
            }

            return false;
        }
        catch ( NativeMethodFound e )
        {
            LOGGER.debug( "Native method {}({}) found in {}: {}", e.methodName, e.methodSignature, jarPath,
                          e.className );
            return true;
        }
        catch ( IOException e )
        {
            LOGGER.debug( "I/O exception caught when trying to determine whether JAR uses native code: {}", jarPath,
                          e );
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

    static String getBackupNameOf( String filename )
    {
        int end = filename.lastIndexOf( ".jar" );

        if ( end > 0 )
        {
            filename = filename.substring( 0, end ) + "-backup.jar";
        }
        else
        {
            filename += "-backup";
        }

        return filename;
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
                    Path backupPath = Paths.get( getBackupNameOf( targetJar.toString() ) );

                    if ( Files.notExists( backupPath ) )
                    {
                        try
                        {
                            Files.copy( targetJar, backupPath, StandardCopyOption.COPY_ATTRIBUTES );
                        }
                        catch ( IOException e )
                        {
                            throw new RuntimeException( "When attempting to copy into a backup file "
                                + backupPath.toString(), e );
                        }

                        LOGGER.trace( "Created backup file: {}", backupPath );
                    }
                    else
                    {
                        LOGGER.trace( "Backup file: {} already exists, keeping the file", backupPath );
                    }

                    try ( InputStream mfIs = jar.getInputStream( manifestEntry );
                                    ZipArchiveOutputStream os = new ZipArchiveOutputStream( targetJar.toFile() ) )
                    {
                        Manifest mf = new Manifest( mfIs );
                        updateManifest( artifact, mf );
                        // write manifest
                        ZipArchiveEntry newManifestEntry = new ZipArchiveEntry( MANIFEST_PATH );
                        os.putArchiveEntry( newManifestEntry );
                        mf.write( os );
                        os.closeArchiveEntry();
                        // copy the rest of content
                        jar.copyRawEntries( os, entry -> !entry.equals( manifestEntry ) );
                    }
                    catch ( Exception e )
                    {
                        // Re-throw exceptions that occur when processing JAR file after reading header and
                        // manifest.
                        throw new RuntimeException( "A backup of file " + targetJar.toString() + " is stored in "
                            + backupPath, e );
                    }
                    LOGGER.trace( "Manifest injected successfully" );

                    try
                    {
                        Files.delete( backupPath );
                    }
                    catch ( IOException e )
                    {
                        throw new RuntimeException( "When attempting to delete backup file " + backupPath.toString(),
                                                    e );
                    }
                    LOGGER.trace( "Deleted backup file: {}", backupPath );
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
