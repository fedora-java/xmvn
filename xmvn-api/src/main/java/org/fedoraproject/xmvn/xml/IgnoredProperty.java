/*-
 * Copyright (c) 2024 Red Hat, Inc.
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
import io.kojan.xml.Property;
import io.kojan.xml.XMLDumper;
import io.kojan.xml.XMLException;
import io.kojan.xml.XMLParser;
import java.util.List;

/**
 * A {@link Property} of an {@link Entity} that is accepted only when reading input XML, but
 * otherwise not stored anywhere and never written out to output XML documents.
 *
 * <p><strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is
 * marked as public only for technical reasons. This class is not part of XMvn API. Client code
 * using XMvn should <strong>not</strong> reference it directly.
 *
 * @param <EnclosingType> data type of entity
 * @param <EnclosingBean> type of bean associated with the entity
 * @author Mikolaj Izdebski
 */
public class IgnoredProperty<EnclosingType, EnclosingBean>
        extends Property<EnclosingType, EnclosingBean, Void> {

    /**
     * Creates an ignored property.
     *
     * @param tag attribute XML tag name
     * @return created property
     */
    public static <EnclosingType, EnclosingBean> IgnoredProperty<EnclosingType, EnclosingBean> of(
            String tag) {
        return new IgnoredProperty<>(tag);
    }

    private IgnoredProperty(String tag) {
        super(tag, x -> List.of(), (x, y) -> {}, true, false);
    }

    @Override
    protected void dump(XMLDumper dumper, Void unused) throws XMLException {
        // do nothing
    }

    @Override
    protected Void parse(XMLParser parser) throws XMLException {
        String tag = parser.parseStartElement();
        while (parser.hasStartElement()) {
            parse(parser);
        }
        parser.parseText();
        parser.parseEndElement(tag);
        return null;
    }
}
