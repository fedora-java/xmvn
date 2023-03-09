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
package org.fedoraproject.xmvn.tools.resolve;

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
final class ResolverCliRequest
{
    @Parameter
    private List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    private boolean debug;

    @Parameter( names = { "-c",
        "--classpath" }, description = "Use colon instead of new line to separate resolved artifacts" )
    private boolean classpath;

    @Parameter( names = {
        "--raw-request" }, description = "Read a list of raw XMvn XML requests from standard input and print the results on standard output" )
    private boolean raw;

    @DynamicParameter( names = "-D", description = "Define system property" )
    private Map<String, String> defines = new TreeMap<>();

    private final StringBuilder usage = new StringBuilder();

    public static ResolverCliRequest build( String[] args )
    {
        try
        {
            return new ResolverCliRequest( args );
        }
        catch ( ParameterException e )
        {
            System.err.println( e.getMessage() + ". Specify -h for usage." );
            return null;
        }
    }

    private ResolverCliRequest( String[] args )
    {
        JCommander jcomm = new JCommander( this );
        jcomm.setProgramName( "xmvn-resolve" );
        jcomm.parse( args );
        jcomm.getUsageFormatter().usage( usage );

        if ( raw && ( classpath || !parameters.isEmpty() ) )
        {
            throw new ParameterException( "--raw-request must be used alone" );
        }

        for ( String param : defines.keySet() )
            System.setProperty( param, defines.get( param ) );
    }

    public boolean printUsage()
    {
        if ( help )
        {
            System.out.println( "xmvn-resolve: Resolve artifacts from system repository" );
            System.out.println();
            System.out.println( usage );
            return true;
        }

        return false;
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

    public boolean isClasspath()
    {
        return classpath;
    }

    public void setClasspath( boolean classpath )
    {
        this.classpath = classpath;
    }

    public boolean isRaw()
    {
        return raw;
    }

    public void setRaw( boolean raw )
    {
        this.raw = raw;
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
