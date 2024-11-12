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
package org.fedoraproject.xmvn.tools.resolve.xml;

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * Data Access Object for reading {@link List}s of {@link ResolutionRequest}s from {@link
 * InputStream}s and writing {@link ResolutionResult}s to {@link OutputStream}s.
 *
 * @author Mikolaj Izdebski
 */
public class ResolverDAO {

    /**
     * Deserializes {@link List} of {@link ResolutionRequest}s, reading XML data from given {@link
     * InputStream}.
     *
     * @param is the source to read XML data from
     * @return deserialized list of resolution requests
     * @throws IOException in case I/O exception occurs when reading form the stream
     * @throws XMLException in case exception occurs during XML deserialization
     */
    public static List<ResolutionRequest> unmarshalRequests(InputStream is)
            throws IOException, XMLException {
        try (Reader r = new InputStreamReader(is)) {
            return ResolverERM.requestsEntity.readFromXML(r);
        }
    }

    /**
     * Serializes entity into XML format, writing XML data to given {@link Writer}.
     *
     * @param os the sink to write XML data to
     * @param results list of resolution requests to serialize
     * @throws IOException in case I/O exception occurs when writing to the stream
     * @throws XMLException in case exception occurs during XML serialization
     */
    public static void marshalResults(OutputStream os, List<ResolutionResult> results)
            throws IOException, XMLException {
        try (Writer w = new OutputStreamWriter(os)) {
            ResolverERM.resultsEntity.writeToXML(w, results);
        }
    }
}
