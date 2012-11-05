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
package org.fedoraproject.maven.resolver;

import static org.fedoraproject.maven.utils.Logger.warn;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.utils.FileUtils;

public class DefaultResolver
    extends AbstractResolver
{
    private final Collection<Resolver> resolvers = new LinkedList<>();

    public DefaultResolver()
    {
        resolvers.add( new LocalResolver() );
        
        if ( Configuration.PREFIX != null )
        {
            File prefix = new File( Configuration.PREFIX );
            if ( prefix.isDirectory() )
            {
                Resolver prefixResolver = new SystemResolver( prefix );
                resolvers.add( new CachingResolver( prefixResolver ) );
            }
        }

        Resolver rootResolver = new SystemResolver( FileUtils.ROOT );
        resolvers.add( new CachingResolver( rootResolver ) );
    }

    @Override
    public File resolve( Artifact artifact )
    {
        for ( Resolver resolver : resolvers )
        {
            File file = resolver.resolve( artifact );
            if ( file != null )
                return file;
        }

        warn( "Unresolved artifact ", artifact );
        return null;
    }
}
