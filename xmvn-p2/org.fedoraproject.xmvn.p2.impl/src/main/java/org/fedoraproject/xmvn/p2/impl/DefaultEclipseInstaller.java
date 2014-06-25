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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.fedoraproject.xmvn.p2.EclipseInstallationRequest;
import org.fedoraproject.xmvn.p2.EclipseInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
public class DefaultEclipseInstaller
    implements EclipseInstaller
{
    private final Logger logger = LoggerFactory.getLogger( DefaultEclipseInstaller.class );

    @Override
    public void performInstallation( EclipseInstallationRequest request )
        throws Exception
    {
        logger.info( "Indexing system bundles and features..." );
        SystemIndex index = new SystemIndex();
        index.index();

        logger.info( "Populating all artifacts..." );
        Repository reactorRepo = Repository.createTemp();
        Director.publish( reactorRepo, request.getPlugins(), request.getFeatures() );
        Set<IInstallableUnit> reactorUnits = Resolver.resolveAll( reactorRepo );
        dump( "Reactor contents", reactorUnits );

        Repository unionMetadataRepo = Repository.createTemp();
        Director.mirrorMetadata( unionMetadataRepo, index.getPlatformUnits() );
        Director.mirrorMetadata( unionMetadataRepo, index.gitInternalUnits() );
        Director.mirrorMetadata( unionMetadataRepo, index.getExternalUnits() );
        Director.mirrorMetadata( unionMetadataRepo, reactorUnits );

        Map<String, Package> packages = new LinkedHashMap<>();

        for ( Entry<String, String> entry : request.getPackageMappings().entrySet() )
        {
            String unitId = entry.getKey();
            String packageId = entry.getValue();

            Package pkg = packages.get( packageId );
            if ( pkg == null )
            {
                pkg = new Package( packageId );
                packages.put( packageId, pkg );
            }

            IInstallableUnit unit = Resolver.resolveOne( reactorRepo, unitId, null );
            if ( unit == null )
                throw new RuntimeException( "Unresolvable unit present in package mappings: " + unitId );

            pkg.addContent( unit );
        }

        for ( Package pkg : packages.values() )
        {
            dump( "Initlal contents", pkg.getContents() );

            logger.info( "Resolving dependencies for package {}...", pkg.getId() );
            Resolver.resolveDependencies( pkg.getContents(), pkg.getDependencies(),
                                          unionMetadataRepo.getMetadataRepository(), reactorUnits,
                                          index.getPlatformUnits(), index.gitInternalUnits(), index.getExternalUnits() );
            dump( "packageContents", pkg.getContents() );
            dump( "externalDeps", pkg.getDependencies() );
        }

        for ( Package pkg : packages.values() )
        {
            logger.info( "Creating runnable repository for package {}...", pkg.getId() );
            Repository packageRepo = Repository.createTemp();
            Director.mirror( packageRepo, reactorRepo, pkg.getContents() );
            Path installationPath = request.getTargetDropinDirectory().resolve( pkg.getId() ).resolve( "eclipse" );
            Repository runnableRepo = Repository.create( request.getBuildRoot().resolve( installationPath ) );
            Director.repo2runnable( runnableRepo, packageRepo );
            Files.deleteIfExists( installationPath.resolve( "artifacts.jar" ) );
            Files.deleteIfExists( installationPath.resolve( "content.jar" ) );

            if ( logger.isInfoEnabled() && !pkg.getDependencies().isEmpty() )
                logger.info( "Symlinking external dependencies..." );

            Path pluginsDir = runnableRepo.getLocation().resolve( "plugins" );
            for ( IInstallableUnit iu : pkg.getDependencies() )
            {
                Path path = index.lookupBundle( iu );
                if ( path == null )
                {
                    logger.error( "Unable to locate dependency in index: {}", iu );
                }
                else
                {
                    String baseName = iu.getId() + "_" + iu.getVersion();
                    String suffix = Files.isDirectory( path ) ? "" : ".jar";
                    Files.createSymbolicLink( pluginsDir.resolve( baseName + suffix ), path );
                    logger.debug( "Linked external dependency {} => {}", baseName + suffix, path );
                }
            }

            logger.info( "Done." );
        }
    }

    private void dump( String message, Set<IInstallableUnit> units )
    {
        logger.debug( "{}:", message );
        Set<String> sorted = new TreeSet<>();
        for ( IInstallableUnit unit : units )
            sorted.add( unit.toString() );
        for ( String unit : sorted )
            logger.debug( "  * {}", unit );
        if ( sorted.isEmpty() )
            logger.debug( "  (none)" );
    }
}
