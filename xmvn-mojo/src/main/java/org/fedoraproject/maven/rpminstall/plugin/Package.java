/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.utils.FileUtils;

public class Package
    implements Comparable<Package>
{
    private final String suffix;

    public static final String MAIN = "";

    private static final String NOINSTALL_SUFFIX = "__noinstall";

    private boolean pureDevelPackage = true;

    private final InstallerSettings settings;

    public Package( String name, InstallerSettings settings )
    {
        this.settings = settings;
        suffix = name.equals( "" ) ? "" : "-" + name;
    }

    private final FragmentFile metadata = new FragmentFile();

    class TargetFile
    {
        Path sourceFile;

        Path dirPath;

        Path targetName;

        int mode;
    }

    private final List<TargetFile> targetFiles = new LinkedList<>();

    public void addFile( Path file, Path dirPath, Path fileName, int mode )
    {
        TargetFile target = new TargetFile();
        target.sourceFile = file;
        target.dirPath = dirPath;
        target.targetName = fileName;
        target.mode = mode;
        targetFiles.add( target );
    }

    public void addFile( Path file, Path target, int mode )
    {
        addFile( file, target.getParent(), target.getFileName(), mode );
    }

    public void addPomFile( Path file, Path jppGroupId, Path jppArtifactId )
    {
        Path pomName = Paths.get( jppGroupId.toString().replace( '/', '.' ) + "-" + jppArtifactId + ".pom" );
        Path pomDir = Paths.get( settings.getPomDir() );
        addFile( file, pomDir, pomName, 0644 );
    }

    private static boolean containsNativeCode( Path jar )
        throws IOException
    {
        // From /usr/include/linux/elf.h
        final int ELFMAG0 = 0x7F;
        final int ELFMAG1 = 'E';
        final int ELFMAG2 = 'L';
        final int ELFMAG3 = 'F';

        try (ZipInputStream jis = new ZipInputStream( new FileInputStream( jar.toFile() ) ))
        {
            ZipEntry ent;
            while ( ( ent = jis.getNextEntry() ) != null )
            {
                if ( ent.isDirectory() )
                    continue;
                if ( jis.read() == ELFMAG0 && jis.read() == ELFMAG1 && jis.read() == ELFMAG2 && jis.read() == ELFMAG3 )
                    return true;
            }
        }

        return false;
    }

    public void addJarFile( Path file, Path baseName, Collection<Path> symlinks )
        throws IOException
    {
        pureDevelPackage = false;

        Path jarDir = Paths.get( containsNativeCode( file ) ? settings.getJniDir() : settings.getJarDir() );
        Path jarFile = jarDir.resolve( Paths.get( baseName + ".jar" ) );
        addFile( file, jarFile, 0644 );

        for ( Path symlink : symlinks )
        {
            symlink = Paths.get( symlink + ".jar" );
            if ( symlink.isAbsolute() )
                symlink = Paths.get( "/" ).relativize( symlink );
            else
                symlink = jarDir.resolve( symlink );

            Path symlinkTarget = symlink.getParent().relativize( jarFile );
            Path symlinkFile = FileUtils.createAnonymousSymlink( symlinkTarget );
            addFile( symlinkFile, symlink, 0644 );
        }
    }

    private void installFiles( Installer installer )
        throws IOException
    {
        for ( TargetFile target : targetFiles )
        {
            installer.installFile( target.sourceFile, target.dirPath, target.targetName, target.mode );
        }
    }

    public void createDepmaps( String groupId, String artifactId, String version, Path jppGroup, Path jppName,
                               PackagingRule rule )
    {
        Artifact artifact = new Artifact( groupId, artifactId, version );
        Artifact jppArtifact = new Artifact( jppGroup.toString(), jppName.toString(), version );

        getMetadata().addMapping( artifact, jppArtifact );

        for ( org.fedoraproject.maven.config.Artifact alias2 : rule.getAliases() )
        {
            Artifact alias = new Artifact( alias2.getGroupId(), alias2.getArtifactId(), alias2.getVersion() );
            getMetadata().addMapping( alias, jppArtifact );
        }
    }

    private void installMetadata( Installer installer )
        throws IOException
    {
        getMetadata().optimize();

        if ( !getMetadata().isEmpty() )
        {
            Path file = Files.createTempFile( "xmvn", ".xml" );
            getMetadata().write( file, pureDevelPackage, settings );
            String packageName = settings.getPackageName();
            Path depmapName = Paths.get( packageName + suffix + ".xml" );
            Path depmapDir = Paths.get( settings.getMetadataDir() );
            addFile( file, depmapDir, depmapName, 0644 );
        }
    }

    private void createFileList()
        throws IOException
    {
        Set<Path> targetNames = new TreeSet<>();
        for ( TargetFile target : targetFiles )
            targetNames.add( target.dirPath.resolve( target.targetName ) );

        try (PrintStream ps = new PrintStream( ".mfiles" + suffix ))
        {
            for ( Path path : targetNames )
                ps.println( "/" + path );
        }
    }

    public void install( Installer installer )
        throws IOException
    {
        installMetadata( installer );
        installFiles( installer );
        createFileList();
    }

    public boolean isInstallable()
    {
        return !suffix.endsWith( NOINSTALL_SUFFIX );
    }

    public FragmentFile getMetadata()
    {
        return metadata;
    }

    @Override
    public int compareTo( Package rhs )
    {
        return suffix.compareTo( rhs.suffix );
    }
}
