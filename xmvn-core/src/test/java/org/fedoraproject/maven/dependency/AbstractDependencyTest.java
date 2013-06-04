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
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.fedoraproject.maven.model.Artifact;

public abstract class AbstractDependencyTest
    extends PlexusTestCase
{
    private Model model;

    private String expectedJavaVersion;

    private final Set<Artifact> expectedDependencyArtifacts = new TreeSet<>();

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

    public void expect( Artifact artifact )
    {
        expectedDependencyArtifacts.add( artifact );
    }

    public void expect( String groupId, String artifactId )
    {
        expect( new Artifact( groupId, artifactId ) );
    }

    public void expect( String groupId, String artifactId, String version )
    {
        expect( new Artifact( groupId, artifactId, version ) );
    }

    public abstract void configure();

    public void testDependencyExtraction()
        throws Exception
    {
        DependencyExtractionRequest request = new DependencyExtractionRequest( model );
        DependencyExtractor extractor = lookup( DependencyExtractor.class, DependencyExtractor.RUNTIME );
        DependencyExtractionResult result = extractor.extract( request );
        assertNotNull( result );
        assertEquals( expectedJavaVersion, result.getJavaVersion() );

        Set<Artifact> expectedButNotReturned = new TreeSet<>( expectedDependencyArtifacts );
        expectedDependencyArtifacts.removeAll( result.getDependencyArtifacts() );
        assertTrue( expectedButNotReturned.isEmpty() );

        Set<Artifact> notExpectedButReturned = new TreeSet<>( result.getDependencyArtifacts() );
        notExpectedButReturned.removeAll( expectedDependencyArtifacts );
        assertTrue( notExpectedButReturned.isEmpty() );
    }
}
