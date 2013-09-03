/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.tools.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.installer.InstallationRequest;
import org.fedoraproject.maven.installer.Installer;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.fedoraproject.maven.utils.LoggingUtils;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * @author Mikolaj Izdebski
 */
public class InstallerCli
{
    @Parameter
    public List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    public boolean debug = false;

    @Parameter( names = { "-r", "--relaxed" }, description = "Skip strict rule checking" )
    public boolean relaxed;

    @Parameter( names = { "-R", "--reactor" }, description = "Path to reactor descriptor" )
    public String planPath = ".xmvn/reactor.xml";

    @Parameter( names = { "-n", "--name" }, description = "Base package name" )
    public String packageName = "pkgname";

    @Parameter( names = { "-d", "--destination" }, description = "Destination directory" )
    public String destDir = ".xmvn/root";

    @DynamicParameter( names = "-D", description = "Define system property" )
    public Map<String, String> defines = new TreeMap<>();

    private void parseArgs( String[] args )
    {
        try
        {
            JCommander jcomm = new JCommander( this, args );
            jcomm.setProgramName( "xmvn-install" );

            if ( help )
            {
                System.out.println( "xmvn-install: Install artifacts" );
                System.out.println();
                jcomm.usage();
                System.exit( 0 );
            }

            for ( String param : defines.keySet() )
                System.setProperty( param, defines.get( param ) );
        }
        catch ( ParameterException e )
        {
            System.err.println( e.getMessage() + ". Specify -h for usage." );
            System.exit( 1 );
        }
    }

    private Xpp3Dom readInstallationPlan()
        throws XmlPullParserException, IOException
    {
        try (Reader reader = new FileReader( planPath ))
        {
            return Xpp3DomBuilder.build( reader );
        }
        catch ( FileNotFoundException e )
        {
            return new Xpp3Dom( "reactorInstallationPlan" );
        }
    }

    private String readValue( Xpp3Dom parent, String tag, String defaultValue, boolean optional )
        throws DataFormatException
    {
        Xpp3Dom[] childreen = parent.getChildren( tag );

        if ( childreen.length == 1 )
            return childreen[0].getValue().trim();

        if ( childreen.length == 0 && optional )
            return defaultValue;

        throw new DataFormatException( "<artifact> must have at exactly one <" + tag + "> child" );
    }

    private Artifact readArtifact( Xpp3Dom dom )
        throws DataFormatException
    {
        String groupId = readValue( dom, "groupId", null, false );
        String artifactId = readValue( dom, "artifactId", null, false );
        String extension = readValue( dom, "extension", "jar", true );
        String classifier = readValue( dom, "classifier", "", true );
        String version = readValue( dom, "version", null, false );
        Artifact artifact = new DefaultArtifact( groupId, artifactId, classifier, extension, version );

        String file = readValue( dom, "file", null, true );
        if ( file != null )
            artifact = artifact.setFile( new File( file ) );

        return artifact;
    }

    private void run()
    {
        try
        {
            DefaultPlexusContainer container = new DefaultPlexusContainer();
            LoggingUtils.configureContainerLogging( container, "xmvn-install", debug );

            InstallationRequest request = new InstallationRequest();
            request.setCheckForUnmatchedRules( !relaxed );
            request.setBasePackageName( packageName );
            request.setInstallRoot( Paths.get( destDir ) );

            Xpp3Dom dom = readInstallationPlan();

            for ( Xpp3Dom artifactDom : dom.getChildren( "artifact" ) )
            {
                Artifact artifact = readArtifact( artifactDom );

                String rawPom = readValue( artifactDom, "rawPomPath", null, true );
                Path rawModelPath = rawPom != null ? Paths.get( rawPom ) : null;
                String effectivePom = readValue( artifactDom, "effectivePomPath", null, true );
                Path effectiveModelPath = effectivePom != null ? Paths.get( effectivePom ) : null;

                artifact = ArtifactUtils.setRawModelPath( artifact, rawModelPath );
                artifact = ArtifactUtils.setEffectiveModelPath( artifact, effectiveModelPath );
                request.addArtifact( artifact );
            }

            Installer installer = container.lookup( Installer.class );
            installer.install( request );
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
            System.exit( 2 );
        }
    }

    public static void main( String[] args )
    {
        InstallerCli cli = new InstallerCli();
        cli.parseArgs( args );
        cli.run();
    }
}
