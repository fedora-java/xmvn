/*-
 * Copyright (c) 2018-2021 Red Hat, Inc.
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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;

/**
 * @author Marian Koncek
 */
class ResolutionRequestUnmarshaller
{
    private final XMLEventReader eventReader;

    public ResolutionRequestUnmarshaller( XMLEventReader eventReader )
    {
        this.eventReader = eventReader;
    }

    ResolutionRequest unmarshal()
        throws XMLStreamException
    {
        ResolutionRequestBean resolutionRequestBean = new ResolutionRequestBean();
        boolean isPersistentFileNeeded = true;

        while ( eventReader.hasNext() )
        {
            XMLEvent event = eventReader.nextEvent();

            switch ( event.getEventType() )
            {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startName = startElement.getName().getLocalPart();

                    switch ( startName )
                    {
                        case "artifact":
                            resolutionRequestBean.setArtifact( new ArtifactUnmarshaller( eventReader ).unmarshal() );
                            break;

                        case "providerNeeded":
                            resolutionRequestBean.setProviderNeeded( Boolean.valueOf( eventReader.nextEvent().asCharacters().getData() ) );
                            break;

                        case "persistentFileNeeded":
                            isPersistentFileNeeded =
                                Boolean.valueOf( eventReader.nextEvent().asCharacters().getData() );
                            break;

                        default:
                            continue;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if ( "request".equals( event.asEndElement().getName().getLocalPart() ) )
                    {
                        ResolutionRequest resolutionRequest;
                        try
                        {
                            resolutionRequest = new ResolutionRequestBean.Adapter().unmarshal( resolutionRequestBean );
                        }
                        catch ( Exception e )
                        {
                            throw new XMLStreamException( "XML stream does not have a proper format", e );
                        }
                        resolutionRequest.setPersistentFileNeeded( isPersistentFileNeeded );
                        return resolutionRequest;
                    }
                    break;

                default:
                    continue;
            }
        }

        throw new XMLStreamException( "XML stream does not have a proper format" );
    }
}
