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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Configuration
{
    private static boolean skipTests;

    public static boolean areTestsSkipped()
    {
        return skipTests;
    }

    private static boolean skipJavadoc;

    public static boolean isJavadocSkipped()
    {
        return skipJavadoc;
    }

    private static String installJarDir = "usr/share/java";

    public static String getInstallJarDir()
    {
        return installJarDir;
    }

    private static String installJniDir = "usr/lib/java";

    public static String getInstallJniDir()
    {
        return installJniDir;
    }

    private static String installPomDir = "usr/share/maven-poms";

    public static String getInstallPomDir()
    {
        return installPomDir;
    }

    private static String installDepmapDir = "usr/share/maven-fragments";

    public static String getInstallDepmapDir()
    {
        return installDepmapDir;
    }

    private static List<String> resolvDepmaps = newList( "/etc/maven/maven2-versionless-depmap.xml",
                                                         "/etc/maven/fragments", "/usr/share/maven-fragments",
                                                         "depmap.xml" );

    public static Collection<String> getDepmaps()
    {
        return Collections.unmodifiableCollection( resolvDepmaps );
    }

    private static List<String> resolvJarRepos = newList( "/usr/share/maven/repository/",
                                                          "/usr/share/maven/repository-java-jni/",
                                                          "/usr/share/maven/repository-jni/" );

    public static Collection<String> getJarRepos()
    {
        return Collections.unmodifiableCollection( resolvJarRepos );
    }

    private static List<String> resolvPomRepos = newList( "/usr/share/maven2/poms/", "/usr/share/maven/poms/",
                                                          "/usr/share/maven-poms/", "/usr/share/maven2/default_poms/" );

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

    private static boolean mavenDebug;

    public static boolean isMavenDebug()
    {
        return mavenDebug;
    }

    private static boolean mavenOnline;

    public static boolean isMavenOnline()
    {
        return mavenOnline;
    }

    private static String mavenVersion = "3.0.4";

    public static String getMavenVersion()
    {
        return mavenVersion;
    }

    private static void setField( Field field, String value )
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
                throw new RuntimeException( "Cannot set field " + field.getName() + ": failed to parse '" + value
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
                        setField( field, value );
                }
            }
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( "Internal XMvn error", e );
        }
    }

    static
    {
        loadConfiguration();
    }
}
