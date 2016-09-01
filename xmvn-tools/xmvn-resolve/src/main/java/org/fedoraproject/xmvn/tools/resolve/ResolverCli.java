/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.resolve.xml.CompoundRequest;
import org.fedoraproject.xmvn.tools.resolve.xml.CompoundResult;

/**
 * Resolve artifacts given on command line.
 * <p>
 * Return 0 when all artifacts are successfully resolved, 1 on failure to resolve one or more artifacts and 2 when some
 * other error occurs. In the last case a stack trace is printed too.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class ResolverCli
{
    private final Logger logger = LoggerFactory.getLogger( ResolverCli.class );

    private final Resolver resolver;

    @Inject
    public ResolverCli( Resolver resolver )
    {
        this.resolver = resolver;
    }

    private List<ResolutionRequest> parseRequests( ResolverCliRequest cli )
        throws JAXBException
    {
        if ( cli.isRaw() )
        {
            JAXBContext jaxbContext = JAXBContext.newInstance( CompoundRequest.class );
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            CompoundRequest compoundRequest = (CompoundRequest) jaxbUnmarshaller.unmarshal( System.in );
            List<ResolutionRequest> requests = compoundRequest.getRequests();
            return requests != null ? requests : Collections.<ResolutionRequest>emptyList();
        }

        List<ResolutionRequest> requests = new ArrayList<>();

        for ( String s : cli.getParameters() )
        {
            if ( s.indexOf( ':' ) > 0 && s.indexOf( ':' ) == s.lastIndexOf( ':' ) )
                s += ":";
            if ( s.endsWith( ":" ) )
                s += "SYSTEM";

            Artifact artifact = new DefaultArtifact( s );
            ResolutionRequest request = new ResolutionRequest( artifact );
            request.setPersistentFileNeeded( true );
            requests.add( request );
        }

        return requests;
    }

    private void printResults( ResolverCliRequest cli, List<ResolutionResult> results )
        throws JAXBException
    {
        if ( cli.isRaw() )
        {
            JAXBContext jaxbContext = JAXBContext.newInstance( CompoundResult.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            jaxbMarshaller.marshal( new CompoundResult( results ), System.out );
        }
        else if ( cli.isClasspath() )
        {
            System.out.println( results.stream().map( r -> r.getArtifactPath().toString() ).collect( Collectors.joining( ":" ) ) );
        }
        else
        {
            results.forEach( r -> System.out.println( r.getArtifactPath() ) );
        }
    }

    private void run( ResolverCliRequest cliRequest )
        throws JAXBException
    {
        try
        {
            boolean error = false;

            List<ResolutionRequest> requests = parseRequests( cliRequest );
            List<ResolutionResult> results = new ArrayList<>();

            for ( ResolutionRequest request : requests )
            {
                ResolutionResult result = resolver.resolve( request );
                results.add( result );

                if ( result.getArtifactPath() == null )
                {
                    error = true;
                    logger.error( "Unable to resolve artifact {}", request.getArtifact() );
                }
            }

            if ( error && !cliRequest.isRaw() )
                System.exit( 1 );

            printResults( cliRequest, results );
        }
        catch ( IllegalArgumentException e )
        {
            logger.error( "{}", e.getMessage() );
            System.exit( 1 );
        }
    }

    public static void main( String[] args )
    {
        try
        {
            ResolverCliRequest cliRequest = new ResolverCliRequest( args );

            Module module =
                new WireModule( new SpaceModule( new URLClassSpace( ResolverCli.class.getClassLoader() ) ) );
            Injector injector = Guice.createInjector( module );
            ResolverCli cli = injector.getInstance( ResolverCli.class );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
