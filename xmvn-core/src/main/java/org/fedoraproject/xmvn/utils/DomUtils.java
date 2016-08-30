/*-
 * Copyright (c) 2016 Red Hat, Inc.
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
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * @author Mikolaj Izdebski
 */
public class DomUtils
{
    public static Element parse( Path path )
        throws SAXException, IOException, ParserConfigurationException
    {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( path.toFile() ).getDocumentElement();
    }

    public static <T extends Node> Stream<T> childrenOfType( Element dom, Class<T> type )
    {
        return IntStream.range( 0, dom.getChildNodes().getLength() ) //
                        .mapToObj( i -> dom.getChildNodes().item( i ) ) //
                        .filter( node -> type.isAssignableFrom( node.getClass() ) ) //
                        .map( node -> type.cast( node ) );
    }

    public static List<Element> parseAsParent( Element dom )
    {
        Stream<Text> notWsTextContent = DomUtils.childrenOfType( dom, Text.class ) //
                                                .filter( node -> !node.getTextContent().trim().isEmpty() );
        if ( notWsTextContent.findAny().isPresent() )
        {
            throw new RuntimeException( "XML element " + dom.getNodeName() + " doesn't allow text content." );
        }

        return childrenOfType( dom, Element.class ).collect( Collectors.toList() );
    }

    public static String parseAsText( Element dom )
    {
        if ( childrenOfType( dom, Element.class ).findAny().isPresent() )
        {
            throw new RuntimeException( "XML element " + dom.getNodeName() + " doesn't allow any children." );
        }

        return dom.getTextContent().trim();
    }

    public static void parseAsEmpty( Element dom )
    {
        if ( !parseAsText( dom ).isEmpty() )
        {
            throw new RuntimeException( "XML element " + dom.getNodeName() + " doesn't allow text content." );
        }
    }

    public static Element parseAsWrapper( Element dom )
    {
        if ( childrenOfType( dom, Element.class ).count() != 1 )
        {
            throw new RuntimeException( "XML node " + dom.getNodeName() + " must have exactly one child." );
        }

        return childrenOfType( dom, Element.class ).findAny().get();
    }
}
