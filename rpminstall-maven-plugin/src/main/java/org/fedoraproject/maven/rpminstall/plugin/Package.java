package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.fedoraproject.maven.resolver.DependencyMap;

public class Package
    implements Comparable<Package>
{
    private final String name;

    public Package( String name )
    {
        this.name = name;
    }

    private final DependencyMap depmap = new DependencyMap();

    class TargetFile
    {
        File sourceFile;

        String dirPath;

        String targetName;
    }

    private final List<TargetFile> targetFiles = new LinkedList<>();

    public void addFile( File file, String dirPath, String fileName )
    {
        TargetFile target = new TargetFile();
        target.sourceFile = file;
        target.dirPath = dirPath;
        target.targetName = fileName;
        targetFiles.add( target );
    }

    public void addPomFile( File file, Artifact artifact )
    {
        String jppGroupId = "JPP/" + name;
        String jppArtifactId = artifact.getArtifactId();
        String pomName = jppGroupId.replace( '/', '.' ) + "-" + jppArtifactId + ".pom";
        addFile( file, Installer.POM_DIR, pomName );

        addDepmap( artifact );
    }

    private static boolean containsNativeCode( File jar )
    {
        // TODO: implement
        return false;
    }

    public void addJarFile( File file, Artifact artifact )
    {
        String jarDir = containsNativeCode( file ) ? Installer.JNI_DIR : Installer.JAR_DIR;
        addFile( file, jarDir + "/" + name, artifact.getArtifactId() + ".jar" );
    }

    public void addDocumentation( File docFile )
    {

    }

    private void installFiles( Installer installer )
        throws IOException
    {
        for ( TargetFile target : targetFiles )
        {
            installer.installFile( target.sourceFile, target.dirPath, target.targetName );
        }
    }

    public void addDepmap( Artifact artifact )
    {
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();

        depmap.addMapping( groupId, artifactId, version, "JPP/" + name, artifactId );
    }

    private void installDepmap( Installer installer )
        throws IOException
    {
        if ( !depmap.isEmpty() )
        {
            File file = File.createTempFile( "maven-fedora-packager", ".xml" );
            depmap.writeToFile( file );
            String depmapName = name + ".xml";
            addFile( file, Installer.DEPMAP_DIR, depmapName );
        }
    }

    private void createFileList()
        throws IOException
    {
        Set<String> targetNames = new TreeSet<>();
        for ( TargetFile target : targetFiles )
        {
            File file = new File( target.dirPath, target.targetName );
            targetNames.add( file.getPath() );
        }

        PrintStream ps = new PrintStream( ".mfiles-" + name );
        for ( String path : targetNames )
        {
            ps.println( path );
        }
        ps.close();
    }

    public void install( Installer installer )
        throws IOException
    {
        installDepmap( installer );
        installFiles( installer );
        createFileList();
    }

    @Override
    public int compareTo( Package rhs )
    {
        return name.compareTo( rhs.name );
    }
}
