/*-
 * Copyright (c) 2024-2026 Red Hat, Inc.
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
package org.fedoraproject.xmvn.xml;

import io.kojan.xml.Entity;
import io.kojan.xml.Getter;
import io.kojan.xml.Property;
import io.kojan.xml.Setter;
import io.kojan.xml.XMLDumper;
import io.kojan.xml.XMLException;
import io.kojan.xml.XMLParser;
import java.util.List;
import java.util.Properties;

/**
 * A {@link Property} of an {@link Entity} backed up by Java {@link Properties}.
 *
 * <p>When stored in XML form, the properties are represented by a XML element with specified tag,
 * which contains nested child element for each property. For example:
 *
 * <pre>
 * &lt;properties&gt;
 *   &lt;foo&gt;123&lt;/foo&gt;
 *   &lt;bar&gt;xyzzy&lt;/bar&gt;
 * &lt;/properties&gt;
 * </pre>
 *
 * <p><strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is
 * marked as public only for technical reasons. This class is not part of XMvn API. Client code
 * using XMvn should <strong>not</strong> reference it directly.
 *
 * @param <EnclosingType> data type of entity
 * @param <EnclosingBean> type of bean associated with the entity
 * @author Mikolaj Izdebski
 */
public class JavaProperties<EnclosingType, EnclosingBean>
        extends Property<EnclosingType, EnclosingBean, Properties> {

    /**
     * Creates a unique, optional entity property modeling Java {@link Properties}.
     *
     * @param <Type> data type of entity
     * @param <Bean> type of bean associated with the entity
     * @param tag attribute XML tag name
     * @param getter entity bean getter for getting {@link Properties}
     * @param setter entity bean setter for setting {@link Properties}
     * @return created property
     */
    public static <Type, Bean> JavaProperties<Type, Bean> of(
            String tag, Getter<Type, Properties> getter, Setter<Bean, Properties> setter) {
        return new JavaProperties<Type, Bean>(tag, x -> List.of(getter.get(x)), setter);
    }

    private JavaProperties(
            String tag,
            Getter<EnclosingType, Iterable<Properties>> getter,
            Setter<EnclosingBean, Properties> setter) {
        super(tag, getter, setter, true, true);
    }

    @Override
    protected void dump(XMLDumper dumper, Properties properties) throws XMLException {
        if (!properties.isEmpty()) {
            dumper.dumpStartElement(getTag());
            for (var entry : properties.entrySet()) {
                dumper.dumpStartElement(entry.getKey().toString());
                dumper.dumpText(entry.getValue().toString());
                dumper.dumpEndElement();
            }
            dumper.dumpEndElement();
        }
    }

    @Override
    protected Properties parse(XMLParser parser) throws XMLException {
        Properties properties = new Properties();
        parser.parseStartElement(getTag());
        while (parser.hasStartElement()) {
            String key = parser.parseStartElement();
            String value = parser.parseText();
            parser.parseEndElement(key);
            properties.setProperty(key, value);
        }
        parser.parseEndElement(getTag());
        return properties;
    }
}
