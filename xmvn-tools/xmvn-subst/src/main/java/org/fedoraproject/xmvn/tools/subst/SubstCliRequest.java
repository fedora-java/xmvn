/*-
 * Copyright (c) 2013-2017 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * @author Mikolaj Izdebski
 */
class SubstCliRequest
{
    @Parameter
    private List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    private boolean debug = false;

    @Parameter( names = { "-s", "--strict" }, description = "Fail if any artifact cannot be symlinked" )
    private boolean strict = false;

    @Parameter( names = { "-d",
        "--dry-run" }, description = "Do not symlink anything but report what would have been symlinked" )
    private boolean dryRun = false;

    @Parameter( names = { "-L",
        "--follow-symlinks" }, description = "Follow symbolic links when traversing directory structure" )
    private boolean followSymlinks = false;

    @Parameter( names = { "-t", "--type" }, description = "Consider artifacts with given type" )
    private List<String> types = new ArrayList<>( Arrays.asList( "jar", "war" ) );

    @Parameter( names = { "-R", "--root" }, description = "Consider another root when looking for artifacts" )
    private String root;

    @DynamicParameter( names = "-D", description = "Define system property" )
    private Map<String, String> defines = new TreeMap<>();

    public SubstCliRequest( String[] args )
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

    public List<String> getParameters()
    {
        return parameters;
    }

    public void setParameters( List<String> parameters )
    {
        this.parameters = parameters;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    public boolean isStrict()
    {
        return strict;
    }

    public void setStrict( boolean strict )
    {
        this.strict = strict;
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public void setDryRun( boolean dryRun )
    {
        this.dryRun = dryRun;
    }

    public boolean isFollowSymlinks()
    {
        return followSymlinks;
    }

    public void setFollowSymlinks( boolean followSymlinks )
    {
        this.followSymlinks = followSymlinks;
    }

    public List<String> getTypes()
    {
        return types;
    }

    public void setTypes( List<String> types )
    {
        this.types = types;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot( String root )
    {
        this.root = root;
    }

    public Map<String, String> getDefines()
    {
        return defines;
    }

    public void setDefines( Map<String, String> defines )
    {
        this.defines = defines;
    }
}
