/*-
 * Copyright (c) 2018-2024 Red Hat, Inc.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * @author Marian Koncek
 */
public class ResolutionResultListMarshaller {
    private final List<ResolutionResult> resolutionResults;

    static class StringConstants {
        private static final String RESULT = "result";

        private static final String ARTIFACT_PATH = "artifactPath";

        private static final String PROVIDER = "provider";

        private static final String COMPAT_VERSION = "compatVersion";

        private static final String NAMESPACE = "namespace";
    }

    public ResolutionResultListMarshaller(List<ResolutionResult> resolutionResults) {
        this.resolutionResults = resolutionResults;
    }

    public void marshal(OutputStream stream) throws IOException, XMLStreamException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream))) {
            XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(bw);

            try {
                xsw.writeStartElement("results");

                for (ResolutionResult resolutionResult : resolutionResults) {
                    if (resolutionResult == null) {
                        continue;
                    }

                    xsw.writeStartElement(StringConstants.RESULT);

                    if (resolutionResult.getArtifactPath() != null) {
                        xsw.writeStartElement(StringConstants.ARTIFACT_PATH);
                        xsw.writeCharacters(resolutionResult.getArtifactPath().toString());
                        xsw.writeEndElement();
                    }

                    if (resolutionResult.getProvider() != null) {
                        xsw.writeStartElement(StringConstants.PROVIDER);
                        xsw.writeCharacters(resolutionResult.getProvider());
                        xsw.writeEndElement();
                    }

                    if (resolutionResult.getCompatVersion() != null) {
                        xsw.writeStartElement(StringConstants.COMPAT_VERSION);
                        xsw.writeCharacters(resolutionResult.getCompatVersion());
                        xsw.writeEndElement();
                    }

                    if (resolutionResult.getNamespace() != null) {
                        xsw.writeStartElement(StringConstants.NAMESPACE);
                        xsw.writeCharacters(resolutionResult.getNamespace());
                        xsw.writeEndElement();
                    }

                    xsw.writeEndElement();
                }

                xsw.writeEndElement();
            } finally {
                xsw.close();
            }
        }
    }
}
