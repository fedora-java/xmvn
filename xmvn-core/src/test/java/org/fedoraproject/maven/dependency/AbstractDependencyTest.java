/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.dependency;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractDependencyTest
    extends PlexusTestCase
{
    private Model model;

    private String expectedJavaVersion;

    private final Set<Artifact> expectedDependencyArtifacts = new HashSet<>();

    public AbstractDependencyTest()
    {
    }

    public AbstractDependencyTest( String modelName )
        throws Exception
    {
        setModel( modelName );
    }

    public void setModel( Model model )
    {
        this.model = model;
    }

    public void setModel( String name )
        throws Exception
    {
        Path path = Paths.get( "src/test/resources/model" ).resolve( name );

        try (Reader reader = new FileReader( path.toFile() ))
        {
            setModel( new MavenXpp3Reader().read( reader ) );
        }
    }

    public void expectJavaVersion( String expectedJavaVersion )
    {
        this.expectedJavaVersion = expectedJavaVersion;
    }

    public void expect( String artifactCoords )
    {
        expectedDependencyArtifacts.add( new DefaultArtifact( artifactCoords ) );
    }

    public void configureBuild()
        throws Exception
    {
        // Nothing to do, but subclasses can override this method to provide additional configuration.
    }

    public void configureRuntime()
        throws Exception
    {
        // Nothing to do, but subclasses can override this method to provide additional configuration.
    }

    public void performTests( String roleHint )
        throws Exception
    {
        if ( model == null )
            return;

        DependencyExtractionRequest request = new DependencyExtractionRequest( model );
        DependencyExtractor extractor = lookup( DependencyExtractor.class, roleHint );
        DependencyExtractionResult result = extractor.extract( request );
        assertNotNull( result );
        assertEquals( expectedJavaVersion, result.getJavaVersion() );

        Set<Artifact> expectedButNotReturned = new HashSet<>( expectedDependencyArtifacts );
        expectedButNotReturned.removeAll( result.getDependencyArtifacts() );
        for ( Artifact artifact : expectedButNotReturned )
            fail( "Dependency artifact " + artifact + " was expected but not returned" );

        Set<Artifact> notExpectedButReturned = new HashSet<>( result.getDependencyArtifacts() );
        notExpectedButReturned.removeAll( expectedDependencyArtifacts );
        for ( Artifact artifact : notExpectedButReturned )
            fail( "Dependency artifact " + artifact + " not expected but returned" );
    }

    public void testBuildDependencyExtraction()
        throws Exception
    {
        configureBuild();
        performTests( DependencyExtractor.BUILD );
    }

    public void testRuntimeDependencyExtraction()
        throws Exception
    {
        configureRuntime();
        performTests( DependencyExtractor.RUNTIME );
    }
}
