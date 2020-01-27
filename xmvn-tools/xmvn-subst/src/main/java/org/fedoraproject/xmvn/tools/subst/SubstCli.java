/*-
 * Copyright (c) 2013-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.locator.ServiceLocator;
import org.fedoraproject.xmvn.locator.ServiceLocatorFactory;
import org.fedoraproject.xmvn.metadata.MetadataRequest;
import org.fedoraproject.xmvn.metadata.MetadataResolver;
import org.fedoraproject.xmvn.metadata.MetadataResult;

/**
 * @author Mikolaj Izdebski
 */
public class SubstCli
{
    private MetadataResolver metadataResolver;

    private ResolverSettings resolverSettings;

    public SubstCli( Configurator configurator, MetadataResolver metadataResolver )
    {
        this.metadataResolver = metadataResolver;
        resolverSettings = configurator.getConfiguration().getResolverSettings();
    }

    private MetadataResult resolveMetadata( List<String> repos )
    {
        MetadataRequest request = new MetadataRequest( repos );
        request.setIgnoreDuplicates( resolverSettings.isIgnoreDuplicateMetadata() );
        MetadataResult result = metadataResolver.resolveMetadata( request );
        return result;
    }

    private void run( SubstCliRequest cliRequest )
    {
        List<MetadataResult> metadataResults = new ArrayList<>();

        if ( cliRequest.getRoot() != null )
        {
            List<String> metadataRepos = new ArrayList<>();
            Path root = Paths.get( cliRequest.getRoot() );

            for ( String configuredRepo : resolverSettings.getMetadataRepositories() )
            {
                Path repoPath = Paths.get( configuredRepo );
                if ( repoPath.isAbsolute() )
                {
                    metadataRepos.add( root.resolve( Paths.get( "/" ).relativize( repoPath ) ).toString() );
                }
            }

            metadataResults.add( resolveMetadata( metadataRepos ) );
        }

        metadataResults.add( resolveMetadata( resolverSettings.getMetadataRepositories() ) );

        ArtifactVisitor visitor = new ArtifactVisitor( cliRequest.isDebug(), metadataResults );

        visitor.setTypes( cliRequest.getTypes() );
        visitor.setFollowSymlinks( cliRequest.isFollowSymlinks() );
        visitor.setDryRun( cliRequest.isDryRun() );

        try
        {
            for ( String path : cliRequest.getParameters() )
            {
                Files.walkFileTree( Paths.get( path ), visitor );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( cliRequest.isStrict() && visitor.getFailureCount() > 0 )
            System.exit( 1 );
    }

    public static void main( String[] args )
    {
        try
        {
            SubstCliRequest cliRequest = new SubstCliRequest( args );
            if ( cliRequest.isDebug() )
                System.setProperty( "xmvn.debug", "true" );

            ServiceLocator locator = new ServiceLocatorFactory().createServiceLocator();
            Configurator configurator = locator.getService( Configurator.class );
            MetadataResolver metadataResolver = locator.getService( MetadataResolver.class );

            SubstCli cli = new SubstCli( configurator, metadataResolver );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            // Helper exceptions used with our integration tests should be ignored
            if ( e.getClass().getName().startsWith( "org.fedoraproject.xmvn.it." ) )
                throw (RuntimeException) e;

            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
