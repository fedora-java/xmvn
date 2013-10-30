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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

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

    private static class DaemonFactory
        implements ThreadFactory
    {
        @Override
        public Thread newThread( Runnable runnable )
        {
            Thread thread = new Thread( runnable );
            thread.setName( DepmapReader.class.getCanonicalName() + ".worker" );
            thread.setDaemon( true );
            return thread;
        }
    }

    public DepmapReader()
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        int nThread = 2 * Math.min( Math.max( Runtime.getRuntime().availableProcessors(), 1 ), 8 );
        executor = new ThreadPoolExecutor( nThread, nThread, 1, TimeUnit.MINUTES, queue, new DaemonFactory() );
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

        public Mapping( Artifact from, Artifact to )
        {
            this.from = from;
            this.to = to;
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
            Document mapDocument = buildDepmapModel();
            List<Mapping> mappings = new ArrayList<>();

            NodeList depNodes = mapDocument.getElementsByTagName( "dependency" );

            for ( int i = 0; i < depNodes.getLength(); i++ )
            {
                Element depNode = (Element) depNodes.item( i );

                Artifact from = getArtifactDefinition( depNode, "maven" );
                if ( from.equals( ArtifactUtils.DUMMY ) )
                    throw new IOException();

                Artifact to = getArtifactDefinition( depNode, "jpp" );

                mappings.add( new Mapping( from, to ) );
            }

            return mappings;
        }

        private Document buildDepmapModel()
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
            try (FileInputStream fis = new FileInputStream( fragmentFile ))
            {
                try (BufferedInputStream bis = new BufferedInputStream( fis, 128 ))
                {
                    try (InputStream is = isCompressed( bis ) ? new GZIPInputStream( bis ) : bis)
                    {
                        String contents = streamAsString( is );

                        if ( contents.length() >= 5 && contents.substring( 0, 5 ).equalsIgnoreCase( "<?xml" ) )
                        {
                            return contents;
                        }

                        StringBuilder buffer = new StringBuilder();
                        buffer.append( "<dependencies>" );
                        buffer.append( contents );
                        buffer.append( "</dependencies>" );
                        return buffer.toString();
                    }
                }
            }
        }

        private String streamAsString( InputStream is )
            throws IOException
        {
            StringBuilder sb = new StringBuilder();
            Charset charset = Charset.defaultCharset();
            ByteBuffer buffer = ByteBuffer.allocate( 128 );

            int sz;
            while ( ( sz = is.read( buffer.array() ) ) > 0 )
            {
                buffer.position( 0 ).limit( sz );
                sb.append( charset.decode( buffer ) );
            }

            return sb.toString();
        }

        private boolean isCompressed( BufferedInputStream bis )
            throws IOException
        {
            try
            {
                bis.mark( 2 );
                DataInputStream ois = new DataInputStream( bis );
                int magic = Short.reverseBytes( ois.readShort() ) & 0xFFFF;
                return magic == GZIPInputStream.GZIP_MAGIC;
            }
            catch ( EOFException e )
            {
                return false;
            }
            finally
            {
                bis.reset();
            }
        }

        private Artifact getArtifactDefinition( Element root, String childTag )
            throws IOException
        {
            NodeList jppNodeList = root.getElementsByTagName( childTag );
            if ( jppNodeList.getLength() == 0 )
                return ArtifactUtils.DUMMY;

            Element element = (Element) jppNodeList.item( 0 );

            String groupId = getValue( element, "groupId", null );
            String artifactId = getValue( element, "artifactId", null );
            String extension = getValue( element, "extension", "jar" );
            String classifier = getValue( element, "classifier", "" );
            String verision = "SYSTEM";

            return new DefaultArtifact( groupId, artifactId, classifier, extension, verision );
        }

        private String getValue( Element parent, String tag, String defaultValue )
            throws IOException
        {
            NodeList nodes = parent.getElementsByTagName( tag );
            if ( nodes.getLength() > 1 )
                throw new IOException( "At most one <" + tag + "> element is allowed" );

            String value = defaultValue;
            if ( nodes.getLength() > 0 )
                value = nodes.item( 0 ).getTextContent().trim();
            if ( value == null )
                throw new IOException( "Exactly one <" + tag + "> element is required" );

            return value;
        }
    }
}
