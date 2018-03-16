/*-
 * Copyright (c) 2016-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.bisect;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class that helps determining path to XMvn home.
 * 
 * @author Mikolaj Izdebski
 */
final class XMvnHomeLocator
{
    private XMvnHomeLocator()
    {
        // Disable public constructor
    }

    /**
     * Get path to XMvn home.
     * 
     * @return path to XMVn home
     */
    public static Path getHome()
    {
        Path home;

        if ( System.getProperty( "xmvn.home" ) != null )
        {
            home = Paths.get( System.getProperty( "xmvn.home" ) );
        }
        else
        {
            Path jarPath = Paths.get( XMvnHomeLocator.class.getProtectionDomain() //
                                                           .getCodeSource().getLocation().getPath() );

            if ( !Files.exists( jarPath ) )
            {
                throw new RuntimeException( "Unable to locate XMvn home: bisect JAR does not exist: " + jarPath );
            }

            if ( !jarPath.getFileName().toString().matches( "^xmvn-bisect.*\\.jar$" ) )
            {
                throw new RuntimeException( "Unable to locate XMvn home: bisect JAR has suprious name: " + jarPath );
            }

            home = jarPath.toAbsolutePath().getParent().getParent().getParent();
        }

        if ( !Files.isDirectory( home ) )
        {
            throw new RuntimeException( "XMvn home is not a directory: " + home );
        }

        return home;
    }
}
