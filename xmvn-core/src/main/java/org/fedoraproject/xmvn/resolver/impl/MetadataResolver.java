/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.metadata.ArtifactAlias;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * @author Mikolaj Izdebski
 */
class MetadataResolver
{
    private final Logger logger = LoggerFactory.getLogger( MetadataResolver.class );

    private final Map<Artifact, ArtifactMetadata> artifactMap = new LinkedHashMap<>();

    private final List<String> depmapLocations;

    public MetadataResolver( List<String> depmapLocations )
    {
        this.depmapLocations = depmapLocations;
    }

    private synchronized void initArtifactMap()
    {
        MetadataReader reader = new MetadataReader();
        List<PackageMetadata> metadataList = reader.readMetadata( depmapLocations );

        PathInterpolator interpolator = new PathInterpolator();

        for ( PackageMetadata metadata : metadataList )
        {
            for ( ArtifactMetadata installedArtifact : metadata.getArtifacts() )
            {
                processArtifactMetadata( installedArtifact );
                interpolator.interpolate( installedArtifact );
            }
        }
    }

    private void processArtifactMetadata( ArtifactMetadata metadata )
    {
        Artifact baseArtifact =
            new DefaultArtifact( metadata.getGroupId(), metadata.getArtifactId(), metadata.getExtension(),
                                 metadata.getClassifier(), metadata.getVersion() );

        List<String> versions = metadata.getCompatVersions();
        if ( versions.isEmpty() )
            versions = Collections.singletonList( Artifact.DEFAULT_VERSION );

        Set<Artifact> artifactSet = new LinkedHashSet<>();

        for ( String version : versions )
        {
            artifactSet.add( baseArtifact.setVersion( version ) );
        }

        for ( ArtifactAlias alias : metadata.getAliases() )
        {
            Artifact aliasArtifact =
                new DefaultArtifact( alias.getGroupId(), alias.getArtifactId(), alias.getExtension(),
                                     alias.getClassifier(), metadata.getVersion() );

            for ( String version : versions )
            {
                artifactSet.add( aliasArtifact.setVersion( version ) );
            }
        }

        Set<Artifact> ignoredArtifacts = new LinkedHashSet<>();

        for ( Artifact artifact : artifactSet )
        {
            if ( ignoredArtifacts.contains( artifact ) )
            {
                logger.debug( "Ignoring metadata for artifact {} as it was already excluded", artifact );
                continue;
            }

            ArtifactMetadata otherMetadata = artifactMap.get( artifact );
            if ( otherMetadata != null )
            {
                artifactMap.remove( artifact );

                logger.warn( "Ignoring metadata for artifact {} as it has duplicate metadata", artifact );
                ignoredArtifacts.add( artifact );
                continue;
            }

            artifactMap.put( artifact, metadata );
        }
    }

    public ArtifactMetadata resolveArtifactMetadata( Artifact artifact )
    {
        initArtifactMap();
        return artifactMap.get( artifact );
    }
}
