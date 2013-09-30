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
package org.fedoraproject.maven.resolver;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.ResolverSettings;

/**
 * @author Mikolaj Izdebski
 */
public class DepmapTest
    extends PlexusTestCase
{
    private DependencyMap readDepmap( Path fragment )
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        settings.getPrefixes().clear();
        settings.addPrefix( new File( "." ).getAbsolutePath() );
        settings.getMetadataRepositories().clear();
        settings.addMetadataRepository( fragment.toString() );
        return lookup( DependencyMap.class );
    }

    private DependencyMap readDepmap( String xml )
        throws Exception
    {
        Path fragment = Files.createTempFile( "depmap-", ".xml" );
        fragment.toFile().deleteOnExit();
        try (FileWriter writer = new FileWriter( fragment.toFile() ))
        {
            writer.write( xml );
        }
        return readDepmap( fragment );
    }

    /**
     * Test if empty fragment files are handled correctly.
     * 
     * @throws Exception
     */
    public void testEmptyFragment()
        throws Exception
    {
        DependencyMap depmap = readDepmap( "" );
        assertTrue( depmap.isEmpty() );
        Artifact fooBar = new DefaultArtifact( "foo:bar:SYSTEM" );
        List<Artifact> translationResult = depmap.translate( fooBar );
        assertTrue( translationResult.isEmpty() );
    }

    /**
     * Test if invalid fragment files are handled correctly.
     * 
     * @throws Exception
     */
    public void testMalformedFragment()
        throws Exception
    {
        DependencyMap depmap = readDepmap( "Lorem ipsum dolor sit amet" );
        assertTrue( depmap.isEmpty() );
    }

    /**
     * Test if nonexistent fragment files don't cause problems.
     * 
     * @throws Exception
     */
    public void testNonexistentFragmentFile()
        throws Exception
    {
        DependencyMap depmap = readDepmap( Paths.get( "/this/should/not/exist" ) );
        assertTrue( depmap.isEmpty() );
        release( depmap );
    }

    private void testDepmapSample( String file )
        throws Exception
    {
        Path path = Paths.get( "src/test/resources" ).resolve( file );
        DependencyMap depmap = readDepmap( path );
        assertFalse( depmap.isEmpty() );
        Artifact commonsIo = new DefaultArtifact( "commons-io:commons-io:SYSTEM" );
        Artifact apacheCommonsIo = new DefaultArtifact( "org.apache.commons:commons-io:SYSTEM" );
        Artifact jppCommonsIo = new DefaultArtifact( "JPP:commons-io:SYSTEM" );
        assertTrue( depmap.translate( commonsIo ).contains( jppCommonsIo ) );
        assertTrue( depmap.translate( apacheCommonsIo ).contains( jppCommonsIo ) );
        release( depmap );
    }

    /**
     * Test parsing of old style depmap (multiple concatenated fragments, not valid XML).
     * 
     * @throws Exception
     */
    public void testOldStyleDepmap()
        throws Exception
    {
        testDepmapSample( "old-style-depmap" );
    }

    /**
     * Test parsing of new style depmap (proper XML).
     * 
     * @throws Exception
     */
    public void testNewStyleDepmap()
        throws Exception
    {
        testDepmapSample( "new-style-depmap.xml" );
    }

    /**
     * Test parsing of depmap containing indirect mappings.
     * <p>
     * Effective depmap should be a transitive closure of mappings.
     * 
     * @throws Exception
     */
    public void testIndirectDepmap()
        throws Exception
    {
        testDepmapSample( "indirect-depmap.xml" );
    }

    /**
     * Test parsing of depmap containing cyclic mapping.
     * <p>
     * Depmap reader shouldn't bail on cyclic mappings (there should be no stack overflow).
     * 
     * @throws Exception
     */
    public void testCyclicDepmap()
        throws Exception
    {
        testDepmapSample( "cyclic-depmap.xml" );
    }

    /**
     * Test parsing of depmap containing XML document declaration.
     * 
     * @throws Exception
     */
    public void testXmlDepmap()
        throws Exception
    {
        testDepmapSample( "xml-depmap.xml" );
    }

    /**
     * Test parsing of depmap compressed with GNU Zip.
     * 
     * @throws Exception
     */
    public void testCompressedDepmap()
        throws Exception
    {
        testDepmapSample( "compressed-depmap.gz" );
    }

    /**
     * Test if namespaces work and if artifacts in different namespaces are matched.
     * 
     * @throws Exception
     */
    public void testNamespaceMatching()
        throws Exception
    {
        Path path = Paths.get( "src/test/resources/namespaced-depmap.xml" );
        DependencyMap depmap = readDepmap( path );
        assertFalse( depmap.isEmpty() );
        Artifact commonsIo = new DefaultArtifact( "commons-io:commons-io:SYSTEM" );
        Artifact apacheCommonsIo = new DefaultArtifact( "org.apache.commons:commons-io:SYSTEM" );
        Artifact jppCommonsIo = new DefaultArtifact( "JPP:commons-io:SYSTEM" );
        assertTrue( depmap.translate( commonsIo ).contains( jppCommonsIo ) );
        assertTrue( depmap.translate( apacheCommonsIo ).contains( jppCommonsIo ) );
        release( depmap );
    }

    /**
     * Test if corrupt compressed depmap files don't cause problems.
     * 
     * @throws Exception
     */
    public void testCorruptCompressedDepmap()
        throws Exception
    {
        DependencyMap depmap = readDepmap( Paths.get( "src/test/resources" ).resolve( "corrupt-compressed-dempap.gz" ) );
        assertTrue( depmap.isEmpty() );
        release( depmap );
    }
}
