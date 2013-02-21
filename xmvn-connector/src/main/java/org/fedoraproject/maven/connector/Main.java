/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author Mikolaj Izdebski
 */
public class Main
    extends MavenCli
{
    @Override
    protected void customizeContainer( PlexusContainer container )
    {
        super.customizeContainer( container );

        try
        {
            LoggerProvider loggerProvider = container.lookup( LoggerProvider.class );
            org.fedoraproject.maven.utils.Logger.setProvider( loggerProvider );
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static int main( String[] args, ClassWorld world )
    {
        System.out.println( "[INFO] Initializing..." );
        return MavenCli.main( args, world );
    }
}
