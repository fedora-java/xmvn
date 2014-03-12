/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class InstallerCli
{
    private final Logger logger = LoggerFactory.getLogger( InstallerCli.class );

    private final Installer installer;

    @Inject
    public InstallerCli( Installer installer )
    {
        this.installer = installer;
    }

    private Xpp3Dom readInstallationPlan( String planPath )
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
        {
            String value = childreen[0].getValue();
            if ( value == null )
                value = "";
            return value.trim();
        }

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

        String stereotype = readValue( dom, "stereotype", null, true );
        artifact = artifact.setStereotype( stereotype );

        return artifact;
    }

    private void readReactor( InstallationRequest request, InstallerCliRequest cliRequest )
    {
        try
        {
            Xpp3Dom dom = readInstallationPlan( cliRequest.getPlanPath() );

            for ( Xpp3Dom artifactDom : dom.getChildren( "artifact" ) )
            {
                Artifact artifact = readArtifact( artifactDom );
                request.addArtifact( artifact );
            }
        }
        catch ( IOException | DataFormatException | XmlPullParserException e )
        {
            logger.error( "Unable to read reactor installation plan", e );
        }
    }

    private void run( InstallerCliRequest cliRequest )
    {
        InstallationRequest request = new InstallationRequest();
        request.setCheckForUnmatchedRules( !cliRequest.isRelaxed() );
        request.setBasePackageName( cliRequest.getPackageName() );
        request.setInstallRoot( Paths.get( cliRequest.getDestDir() ) );

        readReactor( request, cliRequest );

        installer.install( request );
    }

    public static void main( String[] args )
    {
        try
        {
            InstallerCliRequest cliRequest = new InstallerCliRequest( args );

            Module module =
                new WireModule( new SpaceModule( new URLClassSpace( InstallerCli.class.getClassLoader() ) ) );
            Injector injector = Guice.createInjector( module );
            InstallerCli cli = injector.getInstance( InstallerCli.class );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
