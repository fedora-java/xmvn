/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactTypeRegistry
{
    private static final Map<String, String> EXTENSIONS = new LinkedHashMap<>();

    private static final Map<String, String> CLASSIFIERS = new LinkedHashMap<>();

    private static void addStereotype( String type, String extension, String classifier )
    {
        EXTENSIONS.put( type, extension );
        CLASSIFIERS.put( type, classifier );
    }

    private static void loadStereotypes()
    {
        try ( InputStream xmlStream = ArtifactTypeRegistry.class.getResourceAsStream( "/stereotypes.xml" ) )
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

    static
    {
        loadStereotypes();
    }

    public static boolean isRegisteredType( String type )
    {
        return EXTENSIONS.get( type ) != null;
    }

    public static String getExtension( String type )
    {
        return EXTENSIONS.get( type );
    }

    public static String getClassifier( String type )
    {
        return CLASSIFIERS.get( type );
    }
}
