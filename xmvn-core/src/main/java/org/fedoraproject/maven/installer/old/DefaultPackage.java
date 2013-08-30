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
package org.fedoraproject.maven.installer.old;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.FileUtils;

/**
 * @author Mikolaj Izdebski
 */
public class DefaultPackage
    implements Package, Comparable<DefaultPackage>
{
    private final String suffix;

    public static final String MAIN = "";

    private static final String NOINSTALL_SUFFIX = "__noinstall";

    private boolean pureDevelPackage = true;

    private final InstallerSettings settings;

    public DefaultPackage( String name, InstallerSettings settings, Logger logger )
    {
        this.settings = settings;
        metadata = new FragmentFile( logger );
        suffix = name.equals( "" ) ? "" : "-" + name;
    }

    private final FragmentFile metadata;

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

    public static boolean containsNativeCode( Path jar )
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

    public void addSymlink( Path symlink, Path target )
        throws IOException
    {
        Path symlinkTarget = symlink.getParent().relativize( target );
        Path symlinkFile = FileUtils.createAnonymousSymlink( symlinkTarget );
        addFile( symlinkFile, symlink, 0644 );
    }

    private void installFiles( Installer installer )
        throws IOException
    {
        for ( TargetFile target : targetFiles )
        {
            installer.installFile( target.sourceFile, target.dirPath, target.targetName, target.mode );
        }
    }

    @Override
    public void createDepmaps( String groupId, String artifactId, String version, Path jppGroup, Path jppName,
                               PackagingRule rule )
    {
        Artifact artifact = new DefaultArtifact( groupId, artifactId, ArtifactUtils.DEFAULT_EXTENSION, version );
        Artifact jppArtifact =
            new DefaultArtifact( jppGroup.toString(), jppName.toString(), ArtifactUtils.DEFAULT_EXTENSION, version );

        getMetadata().addMapping( artifact, jppArtifact );

        for ( org.fedoraproject.maven.config.Artifact alias2 : rule.getAliases() )
        {
            Artifact alias =
                new DefaultArtifact( alias2.getGroupId(), alias2.getArtifactId(), ArtifactUtils.DEFAULT_EXTENSION,
                                     alias2.getVersion() );
            getMetadata().addMapping( alias, jppArtifact );
        }
    }

    private void installMetadata()
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
        installMetadata();
        installFiles( installer );
        createFileList();
    }

    public boolean isInstallable()
    {
        return !suffix.endsWith( NOINSTALL_SUFFIX );
    }

    @Override
    public FragmentFile getMetadata()
    {
        return metadata;
    }

    @Override
    public int compareTo( DefaultPackage rhs )
    {
        return suffix.compareTo( rhs.suffix );
    }

    public void setPureDevelPackage( boolean pureDevelPackage )
    {
        this.pureDevelPackage = pureDevelPackage;
    }
}
