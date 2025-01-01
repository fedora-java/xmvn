/*-
 * Copyright (c) 2024-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config;

import io.kojan.xml.Getter;
import io.kojan.xml.Property;
import io.kojan.xml.Setter;
import io.kojan.xml.XMLDumper;
import io.kojan.xml.XMLException;
import io.kojan.xml.XMLParser;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class DOM<EnclosingType, EnclosingBean> extends Property<EnclosingType, EnclosingBean, Element> {

    public static <EnclosingType, EnclosingBean> DOM<EnclosingType, EnclosingBean> of(
            String tag,
            Getter<EnclosingType, Element> getter,
            Setter<EnclosingBean, Element> setter) {
        return new DOM<>(tag, x -> List.of(getter.get(x)), setter, true, true);
    }

    private DOM(
            String tag,
            Getter<EnclosingType, Iterable<Element>> getter,
            Setter<EnclosingBean, Element> setter,
            boolean optional,
            boolean unique) {
        super(tag, getter, setter, optional, unique);
    }

    @Override
    protected void dump(XMLDumper dumper, Element value) throws XMLException {
        throw new UnsupportedOperationException("DOM dump is not implemented yet");
    }

    private Element parseElement(XMLParser parser, Document doc) throws XMLException {
        Element element = doc.createElement(parser.parseStartElement());
        if (parser.hasStartElement()) {
            do {
                element.appendChild(parseElement(parser, doc));
            } while (parser.hasStartElement());
        } else {
            element.setTextContent(parser.parseText());
        }
        parser.parseEndElement(element.getTagName());
        return element;
    }

    @Override
    protected Element parse(XMLParser parser) throws XMLException {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return parseElement(parser, docBuilder.newDocument());
        } catch (ParserConfigurationException e) {
            throw new XMLException(e);
        }
    }
}
