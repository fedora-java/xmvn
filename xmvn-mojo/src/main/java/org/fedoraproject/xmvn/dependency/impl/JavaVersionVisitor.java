/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.dependency.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.fedoraproject.xmvn.model.AbstractModelVisitor;

/**
 * @author Mikolaj Izdebski
 */
class JavaVersionVisitor
    extends AbstractModelVisitor
{
    private static final Map<String, BigDecimal> sourceMap = new TreeMap<>();

    private static final Map<String, BigDecimal> targetMap = new TreeMap<>();

    private static void addMappings( NodeList mappings, Map<String, BigDecimal> map )
    {
        for ( int i = 0; i < mappings.getLength(); i++ )
        {
            Element mapping = (Element) mappings.item( i );
            map.put( mapping.getAttribute( "from" ), new BigDecimal( mapping.getAttribute( "to" ) ) );
        }
    }

    private static void loadVersionMap()
    {
        try ( InputStream xmlStream = JavaVersionVisitor.class.getResourceAsStream( "/version-map.xml" ) )
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse( xmlStream );
            NodeList sourceMappings = doc.getElementsByTagName( "sourceMapping" );
            addMappings( sourceMappings, sourceMap );
            targetMap.putAll( sourceMap );
            NodeList targetMappings = doc.getElementsByTagName( "targetMapping" );
            addMappings( targetMappings, targetMap );
        }
        catch ( ParserConfigurationException | IOException | SAXException ex )
        {
            throw new RuntimeException( "Couldnt load resource 'version-map.xml'", ex );
        }
    }

    static
    {
        loadVersionMap();
    }

    private final DefaultDependencyExtractionResult result;

    private Plugin plugin;

    public JavaVersionVisitor( DefaultDependencyExtractionResult result )
    {
        this.result = result;
    }

    private void visitSetting( Xpp3Dom child, Map<String, BigDecimal> versionMap )
    {
        BigDecimal version;
        if ( child.getValue() != null && ( version = versionMap.get( child.getValue().trim() ) ) != null
            && ( result.getJavaVersion() == null || version.compareTo( new BigDecimal( result.getJavaVersion() ) ) > 0 ) )
        {
            result.setJavaVersion( version.toString() );
        }
    }

    private void visitConfiguration( Object configObject )
    {
        if ( plugin.getGroupId() != null && !plugin.getGroupId().equals( "org.apache.maven.plugins" ) )
            return;
        if ( plugin.getArtifactId() == null || !plugin.getArtifactId().equals( "maven-compiler-plugin" ) )
            return;
        if ( configObject == null || !( configObject instanceof Xpp3Dom ) )
            return;
        Xpp3Dom config = (Xpp3Dom) configObject;

        for ( Xpp3Dom child : config.getChildren( "source" ) )
            visitSetting( child, sourceMap );

        for ( Xpp3Dom child : config.getChildren( "target" ) )
            visitSetting( child, targetMap );
    }

    @Override
    public void visitBuildPlugin( Plugin plugin )
    {
        this.plugin = plugin;
        visitConfiguration( plugin.getConfiguration() );
    }

    @Override
    public void visitBuildPluginExecution( PluginExecution execution )
    {
        visitConfiguration( execution.getConfiguration() );
    }

    @Override
    public void visitBuildPluginManagementPlugin( Plugin plugin )
    {
        this.plugin = plugin;
        visitConfiguration( plugin.getConfiguration() );
    }

    @Override
    public void visitBuildPluginManagementPluginExecution( PluginExecution execution )
    {
        visitConfiguration( execution.getConfiguration() );
    }
}
