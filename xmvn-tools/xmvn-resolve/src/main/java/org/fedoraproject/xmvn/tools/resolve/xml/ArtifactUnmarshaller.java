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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.fedoraproject.xmvn.artifact.Artifact;

/**
 * @author Marian Koncek
 */
class ArtifactUnmarshaller
{
    private XMLEventReader eventReader;

    static class StringConstants
    {
        private static final String GROUP_ID = "groupId";

        private static final String ARTIFACT_ID = "artifactId";

        private static final String EXTENSION = "extension";

        private static final String CLASSIFIER = "classifier";

        private static final String VERSION = "version";

        private static final String PATH = "path";
    }

    public ArtifactUnmarshaller( XMLEventReader eventReader )
    {
        this.eventReader = eventReader;
    }

    /**
     * @return A String representation of the nested element or an empty string if end of section has been found
     * @throws IOException
     * @throws XMLStreamException
     */
    String readUntilEnd( String end )
        throws XMLStreamException
    {
        StringBuffer stringBuffer = new StringBuffer();

        while ( eventReader.hasNext() )
        {
            XMLEvent event = eventReader.nextEvent();

            if ( event.getEventType() == XMLStreamConstants.CHARACTERS )
            {
                stringBuffer.append( event.asCharacters().getData() );
            }
            else if ( event.getEventType() == XMLStreamConstants.END_ELEMENT
                && event.asEndElement().getName().getLocalPart().equals( end ) )
            {
                return stringBuffer.toString();
            }
        }

        throw new XMLStreamException( "XML stream does not have a proper format" );
    }

    Artifact unmarshal()
        throws XMLStreamException
    {
        ArtifactBean artifactBean = new ArtifactBean();
        Path path = null;

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
                        case StringConstants.GROUP_ID:
                            artifactBean.setGroupId( readUntilEnd( StringConstants.GROUP_ID ) );
                            if ( artifactBean.getGroupId().isEmpty() )
                            {
                                throw new XMLStreamException( "Xml read error: groupId must not be empty" );
                            }
                            break;

                        case StringConstants.ARTIFACT_ID:
                            artifactBean.setArtifactId( readUntilEnd( StringConstants.ARTIFACT_ID ) );
                            if ( artifactBean.getArtifactId().isEmpty() )
                            {
                                throw new XMLStreamException( "Xml read error: artifactId must not be empty" );
                            }
                            break;

                        case StringConstants.EXTENSION:
                            artifactBean.setExtension( readUntilEnd( StringConstants.EXTENSION ) );
                            break;

                        case StringConstants.CLASSIFIER:
                            artifactBean.setClassifier( readUntilEnd( StringConstants.CLASSIFIER ) );
                            break;

                        case StringConstants.VERSION:
                            artifactBean.setVersion( readUntilEnd( StringConstants.VERSION ) );
                            break;

                        case StringConstants.PATH:
                            path = Paths.get( readUntilEnd( StringConstants.PATH ) );
                            break;

                        default:
                            continue;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if ( event.asEndElement().getName().getLocalPart().equals( "artifact" ) )
                    {
                        try
                        {
                            return new ArtifactBean.Adapter().unmarshal( artifactBean ).setPath( path );
                        }
                        catch ( Exception e )
                        {
                            throw new XMLStreamException( "XML stream does not have a proper format", e );
                        }
                    }
                    break;

                default:
                    continue;
            }
        }

        throw new XMLStreamException( "XML stream does not have a proper format" );
    }
}
