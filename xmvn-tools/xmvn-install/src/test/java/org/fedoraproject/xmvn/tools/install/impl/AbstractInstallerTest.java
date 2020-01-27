/*-
 * Copyright (c) 2014-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.assertj.XmlAssert;

import org.fedoraproject.xmvn.tools.install.Package;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractInstallerTest
{
    protected Path workdir;

    protected Path installRoot;

    protected Path descriptorRoot;

    protected final List<String> descriptors = new ArrayList<>();

    @BeforeEach
    public void setUpWorkdir( TestInfo testInfo )
        throws IOException
    {
        Path workPath = Paths.get( "target" ).resolve( "test-work" );
        workdir = workPath.resolve( testInfo.getTestMethod().get().getName() ).toAbsolutePath();
        delete( workdir );
        Files.createDirectories( workdir );
        installRoot = workdir.resolve( "install-root" );
        Files.createDirectory( installRoot );
        descriptorRoot = workdir.resolve( "descriptor-root" );
        Files.createDirectory( descriptorRoot );
    }

    private void delete( Path path )
        throws IOException
    {
        if ( Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) )
            for ( Path child : Files.newDirectoryStream( path ) )
                delete( child );

        Files.deleteIfExists( path );
    }

    protected void assertDirectoryStructure( String... expected )
        throws Exception
    {
        assertDirectoryStructure( installRoot, expected );
    }

    protected void assertDirectoryStructure( Path root, String... expected )
        throws Exception
    {
        List<String> actualList = new ArrayList<>();
        Files.walkFileTree( root, new FileSystemWalker( root, actualList ) );
        assertEqualsImpl( actualList, "directory structure", expected );
    }

    protected void assertDescriptorEquals( String... expected )
    {
        assertEqualsImpl( descriptors, "file descriptor", expected );
    }

    private void assertEqualsImpl( List<String> actualList, String what, String... expected )
    {
        List<String> expectedList = new ArrayList<>();
        for ( String string : expected )
            expectedList.add( string );

        Collections.sort( expectedList );
        Collections.sort( actualList );

        try
        {
            Iterator<String> expectedIterator = expectedList.iterator();
            Iterator<String> actualIterator = actualList.iterator();

            while ( expectedIterator.hasNext() && actualIterator.hasNext() )
                assertEquals( expectedIterator.next(), actualIterator.next() );

            assertFalse( expectedIterator.hasNext() );
            assertFalse( actualIterator.hasNext() );
        }
        catch ( AssertionError e )
        {
            System.err.println( "EXPECTED " + what + ":" );
            for ( String string : expectedList )
                System.err.println( "  " + string );

            System.err.println( "ACTUAL " + what + ":" );
            for ( String string : actualList )
                System.err.println( "  " + string );

            throw e;
        }
    }

    protected Path getResource( String name )
    {
        return Paths.get( "src/test/resources/", name ).toAbsolutePath();
    }

    protected void assertDescriptorEquals( Path mfiles, String... expected )
        throws IOException
    {
        List<String> lines = Files.readAllLines( mfiles, Charset.defaultCharset() );

        assertEqualsImpl( lines, "descriptor", expected );
    }

    protected void assertDescriptorEquals( Package pkg, String... expected )
        throws IOException
    {
        Path mfiles = descriptorRoot.resolve( ".mfiles" );
        pkg.writeDescriptor( mfiles );
        assertDescriptorEquals( mfiles, expected );
    }

    private void unifyUuids( NodeList nodes )
    {
        for ( int i = 0; i < nodes.getLength(); i++ )
        {
            nodes.item( i ).setTextContent( "uuid-placeholder" );
        }
    }

    protected void assertMetadataEqual( Path expected, Path actual )
        throws Exception
    {
        assertTrue( Files.isRegularFile( actual ) );
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document expectedXml = builder.parse( expected.toString() );
        Document actualXml = builder.parse( actual.toString() );

        NodeList nodes = expectedXml.getElementsByTagName( "path" );

        for ( int i = 0; i < nodes.getLength(); i++ )
        {
            Node pathNode = nodes.item( i );
            String path = pathNode.getTextContent();
            if ( path.startsWith( "???" ) )
                pathNode.setTextContent( getResource( path.substring( 3 ) ).toAbsolutePath().toString() );
        }

        unifyUuids( expectedXml.getElementsByTagName( "uuid" ) );
        unifyUuids( actualXml.getElementsByTagName( "uuid" ) );

        XmlAssert.assertThat( expectedXml ).and( actualXml ).ignoreComments().ignoreWhitespace().areSimilar();
    }
}
