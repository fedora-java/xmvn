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
package org.fedoraproject.maven.resolver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.resolver.DependencyMap;
import org.fedoraproject.maven.utils.ArtifactUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Mikolaj Izdebski
 */
class DepmapReader
{
    private final ThreadPoolExecutor executor;

    public DepmapReader()
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        int nThread = 2 * Math.min( Math.max( Runtime.getRuntime().availableProcessors(), 1 ), 8 );
        executor = new ThreadPoolExecutor( nThread, nThread, 1, TimeUnit.MINUTES, queue );
    }

    public void readMappings( DependencyMap depmap, List<String> depmapLocations )
    {
        List<Future<List<Mapping>>> futures = new ArrayList<>();

        for ( String path : depmapLocations )
        {
            File file = Paths.get( path ).toFile();

            if ( file.isDirectory() )
            {
                String flist[] = file.list();
                if ( flist != null )
                {
                    Arrays.sort( flist );
                    for ( String fragFilename : flist )
                        futures.add( executor.submit( new Task( new File( file, fragFilename ) ) ) );
                }
            }
            else
            {
                futures.add( executor.submit( new Task( file ) ) );
            }
        }

        try
        {
            for ( Future<List<Mapping>> future : futures )
            {
                try
                {
                    for ( Mapping mapping : future.get() )
                        mapping.addToDepmap( depmap );
                }
                catch ( ExecutionException e )
                {
                    // Ignore
                }
            }
        }
        catch ( InterruptedException e )
        {
            throw new RuntimeException( e );
        }
    }

    class Mapping
    {
        private final Artifact from;

        private final Artifact to;

        public Mapping( String namespace, Artifact from, Artifact to )
        {
            from = new DefaultArtifact( from.getGroupId(), from.getArtifactId(), ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION );
            to = new DefaultArtifact( to.getGroupId(), to.getArtifactId(), ArtifactUtils.DEFAULT_EXTENSION, ArtifactUtils.DEFAULT_VERSION );
            this.from = ArtifactUtils.setScope( from, namespace );
            this.to = ArtifactUtils.setScope( to, namespace );
        }

        public void addToDepmap( DependencyMap depmap )
        {
            depmap.addMapping( from, to );
        }
    }

    class Task
        implements Callable<List<Mapping>>
    {
        private final File file;

        public Task( File file )
        {
            this.file = file;
        }

        @Override
        public List<Mapping> call()
            throws Exception
        {
            Document mapDocument = buildDepmapModel( file );
            List<Mapping> mappings = new ArrayList<>();

            NodeList depNodes = mapDocument.getElementsByTagName( "dependency" );

            for ( int i = 0; i < depNodes.getLength(); i++ )
            {
                Element depNode = (Element) depNodes.item( i );

                Artifact from = getArtifactDefinition( depNode, "maven" );
                if ( from.equals( ArtifactUtils.DUMMY ) )
                    throw new IOException();

                Artifact to = getArtifactDefinition( depNode, "jpp" );

                NodeList nodes = depNode.getElementsByTagName( "namespace" );
                if ( nodes.getLength() > 1 )
                    throw new IOException();
                String namespace = null;
                if ( nodes.getLength() != 0 )
                    namespace = nodes.item( 0 ).getTextContent().trim();

                mappings.add( new Mapping( namespace, from, to ) );
            }

            return mappings;
        }

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

        private String wrapFragment( File fragmentFile )
            throws IOException
        {
            CharBuffer contents = readFile( fragmentFile );

            if ( contents.length() >= 5 && contents.subSequence( 0, 5 ).toString().equalsIgnoreCase( "<?xml" ) )
            {
                return contents.toString();
            }

            StringBuilder buffer = new StringBuilder();
            buffer.append( "<dependencies>" );
            buffer.append( contents );
            buffer.append( "</dependencies>" );
            return buffer.toString();
        }

        private CharBuffer readFile( File file )
            throws IOException
        {
            try (FileInputStream fragmentStream = new FileInputStream( file ))
            {
                try (FileChannel channel = fragmentStream.getChannel())
                {
                    MappedByteBuffer buffer = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
                    return Charset.defaultCharset().decode( buffer );
                }
            }
        }

        private Artifact getArtifactDefinition( Element root, String childTag )
            throws IOException
        {
            NodeList jppNodeList = root.getElementsByTagName( childTag );

            if ( jppNodeList.getLength() == 0 )
                return ArtifactUtils.DUMMY;

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

            return new DefaultArtifact( groupId, artifactId, ArtifactUtils.DEFAULT_EXTENSION, version );
        }
    }
}
