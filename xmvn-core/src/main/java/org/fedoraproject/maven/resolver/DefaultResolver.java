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
package org.fedoraproject.maven.resolver;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;

@Component( role = Resolver.class )
class DefaultResolver
    extends AbstractResolver
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    private final Collection<Resolver> resolvers = new LinkedList<>();

    public DefaultResolver()
    {
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();

        resolvers.add( new LocalResolver() );

        for ( String prefix : settings.getPrefixes() )
        {
            File root = new File( prefix );
            if ( root.isDirectory() )
            {
                Resolver resolver = new SystemResolver( root, settings );
                resolvers.add( new CachingResolver( resolver ) );
            }
        }
    }

    @Override
    public File resolve( Artifact artifact )
    {
        logger.debug( "Trying to resolve artifact " + artifact );

        for ( Resolver resolver : resolvers )
        {
            File file = resolver.resolve( artifact );
            if ( file != null )
                return file;
        }

        logger.debug( "Unresolved artifact " + artifact );
        return null;
    }
}
