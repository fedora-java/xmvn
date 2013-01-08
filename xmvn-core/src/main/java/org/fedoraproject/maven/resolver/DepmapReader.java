/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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

import static org.fedoraproject.maven.utils.Logger.debug;
import static org.fedoraproject.maven.utils.Logger.warn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.model.Artifact;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class DepmapReader
{
    private Document buildDepmapModel( File file )
        throws IOException
    {
        try
        {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware( true );
            DocumentBuilder builder = fact.newDocumentBuilder();
            String contents = wrapFragment( file );
            try (Reader reader = new StringReader( contents ))
            {
                InputSource source = new InputSource( reader );
                return builder.parse( source );
            }
        }
        catch ( ParserConfigurationException e )
        {
            throw new IOException( e );
        }
        catch ( SAXException e )
        {
            throw new IOException( e );
        }
    }

    public void readArtifactMap( File root, DependencyMap map )
    {
        for ( String path : Configuration.getDepmaps() )
        {
            File file = new File( path );

            if ( file.isDirectory() )
            {
                String flist[] = file.list();
                if ( flist != null )
                {
                    Arrays.sort( flist );
                    for ( String fragFilename : flist )
                        tryLoadDepmapFile( map, new File( file, fragFilename ) );
                }
            }
            else
            {
                tryLoadDepmapFile( map, file );
            }
        }
    }

    private Artifact getArtifactDefinition( Element root, String childTag )
        throws IOException
    {
        NodeList jppNodeList = root.getElementsByTagName( childTag );

        if ( jppNodeList.getLength() == 0 )
            return Artifact.DUMMY;

        Element element = (Element) jppNodeList.item( 0 );

        NodeList nodes = element.getElementsByTagName( "groupId" );
        if ( nodes.getLength() != 1 )
            throw new IOException();
        String groupId = nodes.item( 0 ).getTextContent().trim();

        nodes = element.getElementsByTagName( "artifactId" );
        if ( nodes.getLength() != 1 )
            throw new IOException();
        String artifactId = nodes.item( 0 ).getTextContent().trim();

        nodes = element.getElementsByTagName( "version" );
        if ( nodes.getLength() > 1 )
            throw new IOException();
        String version = null;
        if ( nodes.getLength() != 0 )
            version = nodes.item( 0 ).getTextContent().trim();

        return new Artifact( groupId, artifactId, version );
    }

    private void tryLoadDepmapFile( DependencyMap map, File fragment )
    {
        try
        {
            loadDepmapFile( map, fragment );
        }
        catch ( IOException e )
        {
            warn( "Could not load depmap file ", fragment.getAbsolutePath(), ": ", e );
        }
    }

    private void loadDepmapFile( DependencyMap map, File file )
        throws IOException
    {
        debug( "Loading depmap file: ", file );
        Document mapDocument = buildDepmapModel( file );

        NodeList depNodes = mapDocument.getElementsByTagName( "dependency" );

        for ( int i = 0; i < depNodes.getLength(); i++ )
        {
            Element depNode = (Element) depNodes.item( i );

            Artifact from = getArtifactDefinition( depNode, "maven" );
            if ( from == Artifact.DUMMY )
                throw new IOException();

            Artifact to = getArtifactDefinition( depNode, "jpp" );
            map.addMapping( from.clearExtension(), to.clearExtension() );
            map.addMapping( from.clearVersionAndExtension(), to.clearVersionAndExtension() );
        }
    }

    private CharBuffer readFile( File fragmentFile )
        throws IOException
    {
        FileInputStream fragmentStream = new FileInputStream( fragmentFile );
        try
        {
            FileChannel channel = fragmentStream.getChannel();
            MappedByteBuffer buffer = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
            return Charset.defaultCharset().decode( buffer );
        }
        finally
        {
            fragmentStream.close();
        }
    }

    private String wrapFragment( File fragmentFile )
        throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "<dependencies>" );
        buffer.append( readFile( fragmentFile ) );
        buffer.append( "</dependencies>" );
        String docString = buffer.toString();
        return docString;
    }
}
