/*-
 * Copyright (c) 2013-2019 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;

/**
 * @author Mikolaj Izdebski
 */
class InstallerCliRequest
{
    @Parameter
    private List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    private boolean debug = false;

    @Parameter( names = { "-r", "--relaxed" }, description = "Skip strict rule checking" )
    private boolean relaxed;

    @Parameter( names = { "-R", "--reactor" }, description = "Path to reactor descriptor" )
    private String planPath = ".xmvn/reactor.xml";

    @Parameter( names = { "-n", "--name" }, description = "Base package name" )
    private String packageName = "pkgname";

    @Parameter( names = { "-d", "--destination" }, description = "Destination directory" )
    private String destDir = ".xmvn/root";

    @Parameter( names = { "-i", "--repository" }, description = "Installation repository ID" )
    private String repoId = ArtifactInstaller.DEFAULT_REPOSITORY_ID;

    @DynamicParameter( names = "-D", description = "Define system property" )
    private Map<String, String> defines = new TreeMap<>();

    public InstallerCliRequest( String[] args )
    {
        try
        {
            JCommander jcomm = new JCommander( this );
            jcomm.setProgramName( "xmvn-install" );
            jcomm.parse( args );

            if ( help )
            {
                System.out.println( "xmvn-install: Install artifacts" );
                System.out.println();
                jcomm.usage();
                System.exit( 0 );
            }

            if ( debug )
                System.setProperty( "org.slf4j.simpleLogger.defaultLogLevel", "trace" );
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

    public boolean isRelaxed()
    {
        return relaxed;
    }

    public void setRelaxed( boolean relaxed )
    {
        this.relaxed = relaxed;
    }

    public String getPlanPath()
    {
        return planPath;
    }

    public void setPlanPath( String planPath )
    {
        this.planPath = planPath;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public String getDestDir()
    {
        return destDir;
    }

    public void setDestDir( String destDir )
    {
        this.destDir = destDir;
    }

    public String getRepoId()
    {
        return repoId;
    }

    public void setRepoId( String repoId )
    {
        this.repoId = repoId;
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
