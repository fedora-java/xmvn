/*-
 * Copyright (c) 2012-2015 Red Hat, Inc.
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
class ResolverCliRequest
{
    @Parameter
    private List<String> parameters = new LinkedList<>();

    @Parameter( names = { "-h", "--help" }, help = true, description = "Display usage information" )
    private boolean help;

    @Parameter( names = { "-X", "--debug" }, description = "Display debugging information" )
    private boolean debug = false;

    @Parameter( names = { "-c", "--classpath" }, description = "Use colon instead of new line to separate resolved artifacts" )
    private boolean classpath = false;

    @Parameter( names = { "--raw-request" }, description = "Read a list of raw XMvn XML requests from standard input and print the results on standard output" )
    private boolean raw = false;

    @DynamicParameter( names = "-D", description = "Define system property" )
    private Map<String, String> defines = new TreeMap<>();

    public ResolverCliRequest( String[] args )
    {
        try
        {
            JCommander jcomm = new JCommander( this, args );
            jcomm.setProgramName( "xmvn-resolve" );

            if ( help )
            {
                System.out.println( "xmvn-resolve: Resolve artifacts from system repository" );
                System.out.println();
                jcomm.usage();
                System.exit( 0 );
            }

            if ( raw && ( classpath || parameters.size() > 0 ) )
            {
                throw new ParameterException( "--raw-request must be used alone" );
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
