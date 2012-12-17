/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven;

import static org.fedoraproject.maven.utils.Logger.debug;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.fedoraproject.maven.utils.FileUtils;

public class Configuration
{
    private static final boolean skipTests = System.getProperty( "maven.test.skip" ) != null;

    public static boolean testsSkipped()
    {
        return skipTests;
    }

    private static String installName = FileUtils.getCwd().getName();

    public static String getInstallName()
    {
        return installName;
    }

    private static String installLayout = "";

    // TODO: convert this to list of rules
    public static String getInstallLayout()
    {
        return installLayout;
    }

    private static String installFiles = "";

    public static Collection<Rule> getInstallFiles()
    {
        return Rule.parseRules( installFiles );
    }

    private static String installDepmaps = "";

    public static Collection<Rule> getInstallDepmaps()
    {
        return Rule.parseRules( installDepmaps );
    }

    private static List<String> installVersions = newList();

    public static Collection<String> getInstallVersions()
    {
        return Collections.unmodifiableCollection( installVersions );
    }

    private static String installJarDir = "usr/share/java";

    public static Path getInstallJarDir()
    {
        return Paths.get( installJarDir );
    }

    private static String installJniDir = "usr/lib/java";

    public static Path getInstallJniDir()
    {
        return Paths.get( installJniDir );
    }

    private static String installPomDir = "usr/share/maven-poms";

    public static Path getInstallPomDir()
    {
        return Paths.get( installPomDir );
    }

    private static String installDepmapDir = "usr/share/maven-fragments";

    public static Path getInstallDepmapDir()
    {
        return Paths.get( installDepmapDir );
    }

    private static List<String> resolvDepmaps = newList( "/usr/share/maven-fragments" );

    public static Collection<String> getDepmaps()
    {
        return Collections.unmodifiableCollection( resolvDepmaps );
    }

    private static List<String> resolvJarRepos = newList( "/usr/share/maven/repository/",
                                                          "/usr/share/maven/repository-jni/" );

    public static Collection<String> getJarRepos()
    {
        return Collections.unmodifiableCollection( resolvJarRepos );
    }

    private static List<String> resolvPomRepos = newList( "/usr/share/maven2/poms/", "/usr/share/maven/poms/",
                                                          "/usr/share/maven-poms/" );

    public static Collection<String> getPomRepos()
    {
        return Collections.unmodifiableCollection( resolvPomRepos );
    }

    public static List<String> resolvPrefixes = newList();

    public static Collection<String> getPrefixes()
    {
        return Collections.unmodifiableCollection( resolvPrefixes );
    }

    private static List<String> newList( String... values )
    {
        return new LinkedList<>( Arrays.asList( values ) );
    }

    private static String repoLocal = ".xm2";

    public static String getLocalRepoPath()
    {
        return repoLocal;
    }

    private static boolean debug;

    public static boolean isDebugEnabled()
    {
        return debug;
    }

    private static void setField( Field field, String value, String envName )
        throws IllegalAccessException
    {
        Class<?> type = field.getType();

        if ( type.equals( String.class ) )
        {
            field.set( null, value );
        }

        else if ( type.equals( boolean.class ) )
        {
            boolean isTrue = value.matches( "1|true|yes|on|enabled" );
            boolean isFalse = value.matches( "0|false|no|off|disabled" );
            if ( isTrue == isFalse )
                throw new RuntimeException( "Cannot set parameter " + envName + ": failed to parse '" + value
                    + "' as boolean value" );
            field.setBoolean( null, isTrue );
        }

        else if ( type.equals( List.class ) )
        {
            @SuppressWarnings( "unchecked" )
            List<String> list = (List<String>) field.get( null );

            if ( value.startsWith( "," ) )
                value = value.substring( 1 );
            else
                list.clear();

            list.addAll( Arrays.asList( value.split( "," ) ) );
        }

        else
        {
            throw new RuntimeException( "Internal XMvn error - unsupported field type" );
        }
    }

    private static void loadConfiguration()
    {
        try
        {
            Field[] fields = Configuration.class.getDeclaredFields();

            for ( Field field : fields )
            {
                String name = field.getName();

                String dotName = "xmvn." + name.replaceAll( "([A-Z])", ".$1" ).toLowerCase();
                String envName = "XMVN_" + name.replaceAll( "([A-Z])", "_$1" ).toUpperCase();

                String sysValue = System.getProperty( dotName );
                String envValue = System.getenv( envName );

                String[] values = new String[] { sysValue, envValue };
                for ( String value : values )
                {
                    if ( value != null )
                        setField( field, value, envName );
                }
            }
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( "Internal XMvn error", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static void dumpConfiguration()
    {
        try
        {
            debug( "--- BEGIN OF XMVN CONFIG DUMP ---" );

            Field[] fields = Configuration.class.getDeclaredFields();

            for ( Field field : fields )
            {
                String name = field.getName();
                String envName = "XMVN_" + name.replaceAll( "([A-Z])", "_$1" ).toUpperCase();

                Class<?> type = field.getType();

                if ( type.equals( String.class ) )
                {
                    debug( "  ", envName, " = \"", field.get( null ).toString(), "\"" );
                }

                else if ( type.equals( boolean.class ) )
                {
                    debug( "  ", envName, " = ", field.getBoolean( null ) );
                }

                else if ( type.equals( List.class ) )
                {
                    debug( "  ", envName, " = {" );
                    for ( String value : (List<String>) field.get( null ) )
                        debug( "    \"", value, "\"," );
                    debug( "  }" );
                }

                else
                {
                    throw new RuntimeException( "Internal XMvn error - unsupported field type" );
                }
            }

            debug( "--- END OF XMVN CONFIG DUMP ---" );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }

    static
    {
        loadConfiguration();
        if ( debug )
            dumpConfiguration();
    }
}
