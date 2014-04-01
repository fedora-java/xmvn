package org.fedoraproject.xmvn.resolver.impl;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.metadata.Alias;
import org.fedoraproject.xmvn.metadata.InstalledArtifact;
import org.fedoraproject.xmvn.metadata.Metadata;

class MetadataResolver
{
    private final Logger logger = LoggerFactory.getLogger( MetadataResolver.class );

    private Set<Artifact> ignoredArtifacts;

    private Map<Artifact, InstalledArtifact> map;

    public MetadataResolver( List<Path> depmapLocations )
    {
        MetadataReader reader = new MetadataReader();
        List<Metadata> metadataList = reader.readMetadata( depmapLocations );

        for ( Metadata metadata : metadataList )
        {
            for ( InstalledArtifact installedArtifact : metadata.getInstalledArtifacts() )
            {
                processInstalledArtifact( installedArtifact );
            }
        }
    }

    private void processInstalledArtifact( InstalledArtifact installedArtifact )
    {
        Artifact baseArtifact =
            new DefaultArtifact( installedArtifact.getGroupId(), installedArtifact.getArtifactId(),
                                 installedArtifact.getExtension(), installedArtifact.getClassifier(),
                                 installedArtifact.getVersion() );

        Set<Artifact> artifactSet = new LinkedHashSet<>();
        artifactSet.add( baseArtifact );
        artifactSet.add( baseArtifact.setVersion( Artifact.DEFAULT_VERSION ) );

        for ( Alias alias : installedArtifact.getAliases() )
        {
            Artifact aliasArtifact =
                new DefaultArtifact( alias.getGroupId(), alias.getArtifactId(), alias.getExtension(),
                                     alias.getClassifier(), installedArtifact.getVersion() );

            artifactSet.add( aliasArtifact );
            artifactSet.add( aliasArtifact.setVersion( Artifact.DEFAULT_VERSION ) );
        }

        for ( Artifact artifact : artifactSet )
        {
            if ( ignoredArtifacts.contains( artifact ) )
            {
                logger.debug( "Ignoring metadata for artifact {} as it was already excluded", artifact );
                continue;
            }

            InstalledArtifact otherInstalledArtifact = map.get( artifact );
            if ( otherInstalledArtifact != null )
            {
                map.remove( artifact );

                logger.warn( "Ignoring metadata for artifact {} as it has duplicate metadata", artifact );
                ignoredArtifacts.add( artifact );
                continue;
            }

            map.put( artifact, installedArtifact );
        }
    }

    public InstalledArtifact resolveArtifactMetadata( Artifact artifact )
    {
        InstalledArtifact versionedArtifact = map.get( artifact );
        if ( versionedArtifact != null )
            return versionedArtifact;

        InstalledArtifact versionlessArtifact = map.get( artifact );
        if ( versionlessArtifact != null )
            return versionlessArtifact;

        return null;
    }
}
