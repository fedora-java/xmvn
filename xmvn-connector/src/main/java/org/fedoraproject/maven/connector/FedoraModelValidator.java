/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.model.Artifact;

/**
 * Custom Maven object model (POM) validator that overrides default Maven model validator.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelValidator.class )
class FedoraModelValidator
    extends DefaultModelValidator
{
    @Requirement
    private Logger logger;

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateEffectiveModel( model, request, problems );
    }

    private void customizeModel( Model model )
    {
        customizeDependencies( model );
        customizePlugins( model );
    }

    private void customizeDependencies( Model model )
    {
        for ( Iterator<Dependency> iter = model.getDependencies().iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String scope = dependency.getScope();

            if ( isBlacklisted( groupId, artifactId ) )
            {
                logger.debug( "Removed dependency " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
                continue;
            }

            if ( Configuration.testsSkipped() && scope != null && scope.equals( "test" ) )
            {
                logger.debug( "Dropped dependency on " + groupId + ":" + artifactId + " because tests are skipped." );
                iter.remove();
                continue;
            }

            if ( dependency.getVersion() == null )
                dependency.setVersion( "SYSTEM" );
        }
    }

    private void customizePlugins( Model model )
    {
        Build build = model.getBuild();
        if ( build == null )
            return;

        for ( Iterator<Plugin> iter = build.getPlugins().iterator(); iter.hasNext(); )
        {
            Plugin plugin = iter.next();
            String groupId = plugin.getGroupId();
            String artifactId = plugin.getArtifactId();

            if ( isBlacklisted( groupId, artifactId ) )
            {
                logger.debug( "Removed plugin " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
                continue;
            }

            if ( plugin.getVersion() == null )
                plugin.setVersion( "SYSTEM" );

            if ( groupId.equals( "org.apache.maven.plugins" ) && artifactId.equals( "maven-compiler-plugin" ) )
                configureCompiler( plugin );
        }
    }

    private void configureCompiler( Plugin plugin )
    {
        Collection<Object> configurations = new LinkedList<>();
        configurations.add( plugin.getConfiguration() );

        Collection<PluginExecution> executions = plugin.getExecutions();
        for ( PluginExecution exec : executions )
            configurations.add( exec.getConfiguration() );

        for ( Object configObj : configurations )
        {
            try
            {
                Xpp3Dom config = (Xpp3Dom) configObj;
                BigDecimal source = new BigDecimal( config.getChild( "source" ).getValue().trim() );
                BigDecimal target = new BigDecimal( config.getChild( "target" ).getValue().trim() );

                // Source must be at least 1.5
                BigDecimal minSource = Configuration.getCompilerSource();
                if ( Configuration.isCompilerSourceSpecified() || source.compareTo( minSource ) < 0 )
                    source = minSource;

                // Target must not be less than source
                if ( target.compareTo( source ) < 0 )
                    target = source;

                config.getChild( "source" ).setValue( source.toString() );
                config.getChild( "target" ).setValue( target.toString() );
            }
            catch ( NullPointerException | NumberFormatException e )
            {
            }
        }
    }

    private static final Map<String, Set<String>> blacklist = new TreeMap<>();

    private boolean isBlacklisted( String groupId, String artifactId )
    {
        Set<String> group = blacklist.get( groupId );
        return group != null && group.contains( artifactId );
    }

    private static void blacklist( String groupId, String artifactId )
    {
        Set<String> group = blacklist.get( groupId );

        if ( group == null )
        {
            group = new TreeSet<>();
            blacklist.put( groupId, group );
        }

        group.add( artifactId );
    }

    private static void blacklist( Artifact artifact )
    {
        blacklist( artifact.getGroupId(), artifact.getArtifactId() );
    }

    static
    {
        blacklist( Artifact.DUMMY );
        blacklist( "org.codehaus.mojo", "clirr-maven-plugin" );
        blacklist( "org.codehaus.mojo", "animal-sniffer-maven-plugin" );
    }
}
