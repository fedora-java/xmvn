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
package org.fedoraproject.xmvn.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public final class ArtifactUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ArtifactUtils.class );

    public static final String UNKNOWN_VERSION = "UNKNOWN";

    public static final String UNKNOWN_NAMESPACE = "UNKNOWN";

    public static final String MF_KEY_GROUPID = "JavaPackages-GroupId";

    public static final String MF_KEY_ARTIFACTID = "JavaPackages-ArtifactId";

    public static final String MF_KEY_EXTENSION = "JavaPackages-Extension";

    public static final String MF_KEY_CLASSIFIER = "JavaPackages-Classifier";

    public static final String MF_KEY_VERSION = "JavaPackages-Version";

    private ArtifactUtils()
    {
        // Avoid generating default public constructor
    }

    /**
     * Convert a collection of artifacts to a human-readable string. This function uses single-line representation.
     * 
     * @param collection collection of artifacts
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> set )
    {
        return collectionToString( set, false );
    }

    /**
     * Convert a collection of artifacts to a human-readable string.
     * 
     * @param collection collection of artifacts
     * @param multiLine if multi-line representation should be used instead of single-line
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> collection, boolean multiLine )
    {
        if ( collection.isEmpty() )
            return "[]";

        String separator = multiLine ? System.lineSeparator() : " ";
        String indent = multiLine ? "  " : "";

        String str = collection.stream().map( a -> indent + a ).collect( Collectors.joining( "," + separator ) );

        return "[" + separator + str + separator + "]";
    }

    private static Artifact getArtifactFromManifest( Path path )
        throws IOException
    {
        try ( JarFile jarFile = new JarFile( path.toFile() ) )
        {
            Manifest mf = jarFile.getManifest();
            if ( mf == null )
                return null;

            String groupId = mf.getMainAttributes().getValue( ArtifactUtils.MF_KEY_GROUPID );
            String artifactId = mf.getMainAttributes().getValue( ArtifactUtils.MF_KEY_ARTIFACTID );
            String extension = mf.getMainAttributes().getValue( ArtifactUtils.MF_KEY_EXTENSION );
            String classifier = mf.getMainAttributes().getValue( ArtifactUtils.MF_KEY_CLASSIFIER );
            String version = mf.getMainAttributes().getValue( ArtifactUtils.MF_KEY_VERSION );

            if ( groupId == null || artifactId == null )
                return null;

            return new DefaultArtifact( groupId, artifactId, extension, classifier, version );
        }
    }

    private static Artifact getArtifactFromPomProperties( Path path, String extension )
        throws IOException
    {
        try ( ZipInputStream zis = new ZipInputStream( Files.newInputStream( path ) ) )
        {
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry() ) != null )
            {
                String name = entry.getName();
                if ( name.startsWith( "META-INF/maven/" ) && name.endsWith( "/pom.properties" ) )
                {
                    Properties properties = new Properties();
                    properties.load( zis );

                    String groupId = properties.getProperty( "groupId" );
                    String artifactId = properties.getProperty( "artifactId" );
                    String version = properties.getProperty( "version" );
                    return new DefaultArtifact( groupId, artifactId, extension, version );
                }
            }

            return null;
        }
    }

    public static Artifact readArtifactDefinition( Path path, String extension )
    {
        try
        {
            Artifact artifact = getArtifactFromManifest( path );
            if ( artifact != null )
                return artifact;

            artifact = getArtifactFromPomProperties( path, extension );
            if ( artifact != null )
                return artifact;

            return null;
        }
        catch ( IOException e )
        {
            LOGGER.error( "Failed to get artifact definition from file {}", path, e );
            return null;
        }
    }

    public static void log( Logger logger, Artifact artifact )
    {
        logger.info( "    groupId: {}", artifact.getGroupId() );
        logger.info( " artifactId: {}", artifact.getArtifactId() );
        logger.info( "  extension: {}", artifact.getExtension() );
        logger.info( " classifier: {}", artifact.getClassifier() );
        logger.info( "    version: {}", artifact.getVersion() );
        logger.info( "       file: {}", artifact.getPath() );
    }

    public static Properties convertProperties( Map<String, String> map )
    {
        Properties properties = new Properties();

        map.forEach( ( key, value ) -> properties.setProperty( key, value ) );

        return properties;
    }
}
