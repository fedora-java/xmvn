/*-
 * Copyright (c) 2018-2019 Red Hat, Inc.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;

/**
 * @author Marian Koncek
 */
public class ResolutionRequestListUnmarshaller
{
    private InputStream inputStream;

    public ResolutionRequestListUnmarshaller( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public List<ResolutionRequest> unmarshal()
        throws IOException, XMLStreamException
    {
        List<ResolutionRequest> resolutionRequests = null;

        try ( BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) ) )
        {
            XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader( bufferedReader );

            try
            {
                mainLoop: while ( eventReader.hasNext() )
                {
                    XMLEvent event = eventReader.nextEvent();

                    switch ( event.getEventType() )
                    {
                        case XMLStreamConstants.START_ELEMENT:
                            StartElement startElement = event.asStartElement();
                            String startName = startElement.getName().getLocalPart();

                            switch ( startName )
                            {
                                case "requests":
                                    resolutionRequests = new ArrayList<ResolutionRequest>();
                                    break;
                                case "request":
                                    resolutionRequests.add( new ResolutionRequestUnmarshaller( eventReader ).unmarshal() );
                                    break;

                                default:
                                    continue;
                            }
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            EndElement endElement = event.asEndElement();
                            String endName = endElement.getName().getLocalPart();

                            if ( endName.equals( "requests" ) )
                            {
                                break mainLoop;
                            }
                            break;

                        default:
                            continue;
                    }
                }
            }
            finally
            {
                eventReader.close();
            }
        }

        return resolutionRequests;
    }
}
