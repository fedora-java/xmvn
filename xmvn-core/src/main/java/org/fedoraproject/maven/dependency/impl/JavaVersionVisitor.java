/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.dependency.impl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.model.AbstractModelVisitor;

/**
 * @author Mikolaj Izdebski
 */
class JavaVersionVisitor
    extends AbstractModelVisitor
{
    private static final Map<String, BigDecimal> sourceMap = new TreeMap<>();

    private static final Map<String, BigDecimal> targetMap = new TreeMap<>();

    static
    {
        // TODO: extract this as configuration
        sourceMap.put( "1.1", new BigDecimal( "1.1" ) );
        sourceMap.put( "1.2", new BigDecimal( "1.2" ) );
        sourceMap.put( "1.3", new BigDecimal( "1.3" ) );
        sourceMap.put( "1.4", new BigDecimal( "1.4" ) );
        sourceMap.put( "1.5", new BigDecimal( "1.5" ) );
        sourceMap.put( "5", new BigDecimal( "1.5" ) );
        sourceMap.put( "5.0", new BigDecimal( "1.5" ) );
        sourceMap.put( "1.6", new BigDecimal( "1.6" ) );
        sourceMap.put( "6", new BigDecimal( "1.6" ) );
        sourceMap.put( "6.0", new BigDecimal( "1.6" ) );
        sourceMap.put( "1.7", new BigDecimal( "1.7" ) );
        sourceMap.put( "7", new BigDecimal( "1.7" ) );
        sourceMap.put( "7.0", new BigDecimal( "1.7" ) );
        sourceMap.put( "1.8", new BigDecimal( "1.8" ) );
        sourceMap.put( "8", new BigDecimal( "1.8" ) );
        sourceMap.put( "8.0", new BigDecimal( "1.8" ) );

        targetMap.putAll( sourceMap );
        targetMap.put( "cldc1.1", new BigDecimal( "1.1" ) );
        targetMap.put( "jsr14", new BigDecimal( "1.4" ) );
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
