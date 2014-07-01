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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.internal.repository.tools.RepositoryDescriptor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;

/**
 * @author Mikolaj Izdebski
 */
@SuppressWarnings( "restriction" )
public class Repository
{
    private final Path location;

    private final IArtifactRepository artifactRepository;

    private final IMetadataRepository metadataRepository;

    private Repository( Path location, IArtifactRepository artifactRepository, IMetadataRepository metadataRepository )
    {
        this.location = location;
        this.artifactRepository = artifactRepository;
        this.metadataRepository = metadataRepository;
    }

    public static Repository createTemp()
        throws ProvisionException, IOException
    {
        Path tempDirectory = Files.createTempDirectory( "xmvn-p2-" );
        tempDirectory.toFile().deleteOnExit();
        return create( tempDirectory );
    }

    public static Repository create( Path location )
        throws ProvisionException
    {
        IProvisioningAgent agent = Activator.getAgent();
        URI uri = location.toUri();
        String name = "xmvn-p2-repo";

        IArtifactRepository artifactRepository = Publisher.createArtifactRepository( agent, uri, name, true, true );

        IMetadataRepository metadataRepository = Publisher.createMetadataRepository( agent, uri, name, true, true );

        return new Repository( location, artifactRepository, metadataRepository );
    }

    public static Repository load( Path location )
        throws ProvisionException
    {
        IProvisioningAgent agent = Activator.getAgent();
        URI uri = location.toUri();

        IArtifactRepository artifactRepository = Publisher.loadArtifactRepository( agent, uri, false, false );

        IMetadataRepository metadataRepository = Publisher.loadMetadataRepository( agent, uri, false, false );

        return new Repository( location, artifactRepository, metadataRepository );
    }

    public Path getLocation()
    {
        return location;
    }

    public IArtifactRepository getArtifactRepository()
    {
        return artifactRepository;
    }

    public IMetadataRepository getMetadataRepository()
    {
        return metadataRepository;
    }

    public RepositoryDescriptor getDescripror()
    {
        RepositoryDescriptor descriptor = new RepositoryDescriptor();
        descriptor.setLocation( location.toUri() );
        return descriptor;
    }

    private Set<IInstallableUnit> executeQuery( IQuery<IInstallableUnit> query )
    {
        IQueryable<IInstallableUnit> queryable = getMetadataRepository();
        IQueryResult<IInstallableUnit> result = queryable.query( query, null );
        return result.toUnmodifiableSet();
    }

    public Set<IInstallableUnit> getAllUnits()
    {
        IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
        return executeQuery( query );
    }

    public IInstallableUnit findUnit( String id, String version )
    {
        VersionRange versionRange = version != null ? new VersionRange( version ) : null;
        IQuery<IInstallableUnit> query = QueryUtil.createIUQuery( id, versionRange );
        Set<IInstallableUnit> result = executeQuery( query );
        if ( result.isEmpty() )
            return null;
        if ( result.size() > 1 )
            throw new RuntimeException( "More than one IU found for " + id + ( version != null ? "/" + version : "" ) );
        return result.iterator().next();
    }
}
