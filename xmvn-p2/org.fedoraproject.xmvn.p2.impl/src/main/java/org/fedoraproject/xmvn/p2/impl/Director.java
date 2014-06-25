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
package org.fedoraproject.xmvn.p2.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.internal.repository.mirroring.Mirroring;
import org.eclipse.equinox.p2.internal.repository.tools.Repo2Runnable;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAction;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;

/**
 * @author Mikolaj Izdebski
 */
@SuppressWarnings( "restriction" )
public class Director
{
    public static void publish( Repository repository, Iterable<Path> bundles, Iterable<Path> features )
        throws ProvisionException
    {
        PublisherInfo info = new PublisherInfo();
        info.setArtifactOptions( IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX | IPublisherInfo.A_OVERWRITE );
        info.setArtifactRepository( repository.getArtifactRepository() );
        info.setMetadataRepository( repository.getMetadataRepository() );

        Collection<IPublisherAction> actions = new ArrayList<>();

        if ( bundles != null && bundles.iterator().hasNext() )
        {
            Collection<File> bundleFiles = new ArrayList<>();
            for ( Path bundle : bundles )
                bundleFiles.add( bundle.toFile() );

            IPublisherAction action = new BundlesAction( bundleFiles.toArray( new File[0] ) );
            actions.add( action );
        }

        if ( features != null && features.iterator().hasNext() )
        {
            Collection<File> featureFiles = new ArrayList<>();
            for ( Path feature : features )
                featureFiles.add( feature.toFile() );

            IPublisherAction action = new FeaturesAction( featureFiles.toArray( new File[0] ) );
            actions.add( action );
        }

        Publisher publisher = new Publisher( info );
        IStatus status = publisher.publish( actions.toArray( new IPublisherAction[0] ), new NullProgressMonitor() );
        if ( !status.isOK() )
            throw new ProvisionException( status );
    }

    public static void repo2runnable( Repository destinationRepository, Repository sourceRepository )
        throws ProvisionException
    {
        Repo2Runnable repo2Runnable = new Repo2Runnable();
        repo2Runnable.addSource( sourceRepository.getDescripror() );
        repo2Runnable.addDestination( destinationRepository.getDescripror() );
        repo2Runnable.setFlagAsRunnable( true );
        IStatus status = repo2Runnable.run( new NullProgressMonitor() );
        if ( !status.isOK() )
            throw new ProvisionException( status );
    }

    public static void mirrorMetadata( Repository destinationRepository, Set<IInstallableUnit> units )
        throws ProvisionException
    {
        IMetadataRepository destMr = destinationRepository.getMetadataRepository();

        destMr.addInstallableUnits( units );
    }

    public static void mirror( Repository destinationRepository, Repository sourceRepository,
                               Set<IInstallableUnit> units )
        throws ProvisionException
    {
        mirrorMetadata( destinationRepository, units );

        IArtifactRepository destAr = destinationRepository.getArtifactRepository();
        IArtifactRepository sourceAr = sourceRepository.getArtifactRepository();

        Mirroring mirror = new Mirroring( sourceAr, destAr, true );
        mirror.setCompare( false );
        mirror.setValidate( false );
        mirror.setTransport( (Transport) Activator.getAgent().getService( Transport.SERVICE_NAME ) );
        mirror.setIncludePacked( true );

        Collection<IArtifactKey> artifactKeys = new ArrayList<>();
        for ( IInstallableUnit iInstallableUnit : units )
            artifactKeys.addAll( iInstallableUnit.getArtifacts() );

        mirror.setArtifactKeys( artifactKeys.toArray( new IArtifactKey[0] ) );

        IStatus status = mirror.run( true, false );
        if ( !status.isOK() )
            throw new ProvisionException( status );
    }
}
