/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.locator;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class loader, which loads classes from XMvn home directory.
 * 
 * @author Mikolaj Izdebski
 */
public class XMvnHomeClassLoader
    extends IsolatedClassRealm
{
    private final Path home;

    private static Path getDefaultHome()
    {
        String home = System.getProperty( "xmvn.home" );
        if ( home == null )
            home = System.getenv( "XMVN_HOME" );
        if ( home == null )
            home = System.getenv( "M2_HOME" );
        if ( home == null )
            home = "/usr/share/xmvn";
        return Paths.get( home );
    }

    /**
     * Create an instance of the class loader with default XMvn home and specified parent class loader, from which all
     * XMvn API classes should be imported.
     * 
     * @param parent class loader to import XMvn API classes from
     */
    public XMvnHomeClassLoader( ClassLoader parent )
    {
        this( getDefaultHome(), parent );
    }

    /**
     * Create an instance of the class loader with specified XMvn home and parent class loader, from which all XMvn API
     * classes should be imported.
     * 
     * @param home a path to the root of XMvn home
     * @param parent class loader to import XMvn API classes from
     */
    public XMvnHomeClassLoader( Path home, ClassLoader parent )
    {
        super( parent );
        this.home = home;

        addJarDirectory( home.resolve( "lib" ).resolve( "core" ) );

        importPackage( "org.fedoraproject.xmvn" );
        importPackage( "org.fedoraproject.xmvn.artifact" );
        importPackage( "org.fedoraproject.xmvn.deployer" );
        importPackage( "org.fedoraproject.xmvn.locator" );
        importPackage( "org.fedoraproject.xmvn.resolver" );
    }

    /**
     * Get path to XMvn home from which this loader loads classes.
     * 
     * @return path to XMVn home
     */
    public Path getHome()
    {
        return home;
    }
}
