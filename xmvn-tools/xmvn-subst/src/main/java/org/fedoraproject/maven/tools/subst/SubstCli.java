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
package org.fedoraproject.maven.tools.subst;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.fedoraproject.maven.utils.LoggingUtils;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * @author Mikolaj Izdebski
 */
public class SubstCli
{
    @Parameter
    public List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    public boolean debug = false;

    @Parameter( names = { "-s", "--strict" }, description = "Fail if any artifact cannot be symlinked" )
    public boolean strict = false;

    @Parameter( names = { "-d", "--dry-run" }, description = "Do not symlink anything but report what would have been symlinked" )
    public boolean dryRun = false;

    @Parameter( names = { "-L", "--follow-symlinks" }, description = "Follow symbolic links when traversing directory structure" )
    public boolean followSymlinks = false;

    @Parameter( names = { "-t", "--type" }, description = "Consider artifacts with given type" )
    public List<String> types = new ArrayList<>( Arrays.asList( "jar", "war" ) );

    @DynamicParameter( names = "-D", description = "Define system property" )
    public Map<String, String> defines = new TreeMap<>();

    private void parseArgs( String[] args )
    {
        try
        {
            JCommander jcomm = new JCommander( this, args );
            jcomm.setProgramName( "xmvn-subst" );

            if ( help )
            {
                System.out.println( "xmvn-subst: Substitute artifact files with symbolic links" );
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

    private void run()
    {
        try
        {
            DefaultPlexusContainer container = new DefaultPlexusContainer();
            LoggingUtils.configureContainerLogging( container, "xmvn-subst", debug );

            ArtifactVisitor visitor = container.lookup( ArtifactVisitor.class );
            visitor.setTypes( types );
            visitor.setFollowSymlinks( followSymlinks );
            visitor.setDryRun( dryRun );

            for ( String path : parameters )
            {
                Files.walkFileTree( Paths.get( path ), visitor );
            }

            if ( strict && visitor.getFailureCount() > 0 )
                System.exit( 1 );
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
            System.exit( 2 );
        }
    }

    public static void main( String[] args )
    {
        SubstCli cli = new SubstCli();
        cli.parseArgs( args );
        cli.run();
    }
}
