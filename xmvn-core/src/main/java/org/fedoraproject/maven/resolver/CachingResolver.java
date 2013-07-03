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

import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.model.Artifact;

/**
 * A caching resolver.
 * <p>
 * This resolver which forwards resolution request to other repository and caches all resolution results. The main
 * advantage of {@code CachingResolver} is faster average resolution time -- if one artifact is requested to be resolved
 * multiple times only the first request is forwarded to the backing repository -- all subsequent resolutions are
 * handled from the cache.
 * 
 * @author Mikolaj Izdebski
 */
class CachingResolver
    extends AbstractResolver
{
    private final Logger logger;

    private final Map<Artifact, ResolutionResult> cache = new TreeMap<>();

    private final Resolver provider;

    public CachingResolver( Resolver provider, Logger logger )
    {
        this.provider = provider;
        this.logger = logger;
    }

    @Override
    public ResolutionResult resolve( ResolutionRequest request )
    {
        Artifact artifact = request.getArtifact();
        ResolutionResult result = cache.get( artifact );

        if ( result != null )
        {
            logger.debug( "Artifact " + artifact + " was resolved from cache" );
        }
        else
        {
            result = provider.resolve( request );
            cache.put( artifact, result );
        }

        return result;
    }
}
