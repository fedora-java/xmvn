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
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactTypeRegistry
{
    private static final ArtifactTypeRegistry DEFAULT_REGISTRY = new ArtifactTypeRegistry();

    private final Map<String, String> EXTENSIONS = new LinkedHashMap<>();

    private final Map<String, String> CLASSIFIERS = new LinkedHashMap<>();

    private void addStereotype( String type, String extension, String classifier )
    {
        EXTENSIONS.put( type, extension );
        CLASSIFIERS.put( type, classifier );
    }

    private ArtifactTypeRegistry()
    {
        try (InputStream xmlStream = ArtifactTypeRegistry.class.getResourceAsStream( "/stereotypes.xml" ))
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse( xmlStream );
            NodeList stereotypes = doc.getElementsByTagName( "stereotype" );
            for ( int i = 0; i < stereotypes.getLength(); i++ )
            {
                Element stereotype = (Element) stereotypes.item( i );
                String type = stereotype.getAttribute( "type" );
                String extension = stereotype.getAttribute( "extension" );
                String classifier = stereotype.getAttribute( "classifier" );
                addStereotype( type, extension, classifier );
            }
        }
        catch ( ParserConfigurationException | IOException | SAXException ex )
        {
            throw new RuntimeException( "Couldnt load resource 'stereotypes.xml'", ex );
        }
    }

    private ArtifactTypeRegistry( ArtifactTypeRegistry template )
    {
        EXTENSIONS.putAll( template.EXTENSIONS );
        CLASSIFIERS.putAll( template.CLASSIFIERS );
    }

    public static ArtifactTypeRegistry getDefaultRegistry()
    {
        return DEFAULT_REGISTRY;
    }

    public ArtifactTypeRegistry registerStereotype( String type, String extension, String classifier )
    {
        ArtifactTypeRegistry newRegistry = new ArtifactTypeRegistry( this );
        newRegistry.addStereotype( type, extension, classifier );
        return newRegistry;
    }

    public Artifact createTypedArtifact( String groupId, String artifactId, String type, String customClassifier,
                                         String version )
    {
        if ( type == null || EXTENSIONS.get( type ) == null )
            return new DefaultArtifact( groupId, artifactId, type, customClassifier, version );

        String classifier = Strings.isNullOrEmpty( customClassifier ) ? CLASSIFIERS.get( type ) : customClassifier;
        return new DefaultArtifact( groupId, artifactId, EXTENSIONS.get( type ), classifier, version );
    }
}
