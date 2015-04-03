/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;

/**
 * Generates effective POM files from package metadata.
 * 
 * @author Mikolaj Izdebski
 */
class EffectivePomGenerator
{
    private final DocumentBuilderFactory documentBuilderFactory;

    private final TransformerFactory transformerFactory;

    public EffectivePomGenerator()
    {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
    }

    private void addTextElement( Document document, Element parent, String name, String value )
    {
        addTextElement( document, parent, name, value, null );
    }

    private void addTextElement( Document document, Element parent, String name, String value, String defaultValue )
    {
        if ( value == null || defaultValue == null || !value.equals( defaultValue ) )
        {
            Element child = document.createElement( name );
            parent.appendChild( child );
            child.appendChild( document.createTextNode( value == null ? defaultValue : value ) );
        }
    }

    private void addExclusion( Document document, Element exclusions, DependencyExclusion exclusion )
    {
        Element exclusionNode = document.createElement( "exclusion" );
        exclusions.appendChild( exclusionNode );
        addTextElement( document, exclusionNode, "groupId", exclusion.getGroupId() );
        addTextElement( document, exclusionNode, "artifactId", exclusion.getArtifactId() );
    }

    private void addDependency( Document document, Element dependencies, Dependency dependency )
    {
        Element dependencyNode = document.createElement( "dependency" );
        dependencies.appendChild( dependencyNode );
        addTextElement( document, dependencyNode, "groupId", dependency.getGroupId() );
        addTextElement( document, dependencyNode, "artifactId", dependency.getArtifactId() );
        addTextElement( document, dependencyNode, "type", dependency.getExtension(), Artifact.DEFAULT_EXTENSION );
        addTextElement( document, dependencyNode, "classifier", dependency.getClassifier(), "" );
        addTextElement( document, dependencyNode, "version", dependency.getRequestedVersion() );
        Boolean optional = Boolean.valueOf( dependency.isOptional() != null && dependency.isOptional() );
        addTextElement( document, dependencyNode, "optional", optional.toString(), "false" );

        Element exclusions = document.createElement( "exclusions" );
        for ( DependencyExclusion exclusion : dependency.getExclusions() )
            addExclusion( document, exclusions, exclusion );
        if ( exclusions.hasChildNodes() )
            dependencyNode.appendChild( exclusions );
    }

    private void addProject( Document document, ArtifactMetadata metadata, Artifact artifact )
    {
        Element project = document.createElement( "project" );
        document.appendChild( project );
        addTextElement( document, project, "modelVersion", "4.0.0" );
        addTextElement( document, project, "groupId", artifact.getGroupId() );
        addTextElement( document, project, "artifactId", artifact.getArtifactId() );
        addTextElement( document, project, "version", artifact.getVersion() );

        Element dependencies = document.createElement( "dependencies" );
        for ( Dependency dependency : metadata.getDependencies() )
            addDependency( document, dependencies, dependency );
        if ( dependencies.hasChildNodes() )
            project.appendChild( dependencies );
    }

    public Path generateEffectivePom( ArtifactMetadata metadata, Artifact artifact )
        throws IOException
    {
        Path pomPath = Files.createTempFile( "xmvn-" + metadata.getUuid(), ".pom" );

        try (OutputStream os = Files.newOutputStream( pomPath ))
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.setXmlStandalone( true );
            addProject( document, metadata, artifact );

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
            transformer.transform( new DOMSource( document ), new StreamResult( os ) );

            return pomPath;
        }
        catch ( ParserConfigurationException | TransformerException e )
        {
            throw new IOException( "Unable to generate effectvie POM", e );
        }
    }
}
