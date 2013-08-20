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

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.config.BuildSettings;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.resolver.ArtifactBlacklist;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * Custom Maven object model (POM) validator that overrides default Maven model validator.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelValidator.class )
public class FedoraModelValidator
    extends DefaultModelValidator
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Requirement
    private ArtifactBlacklist blacklist;

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateEffectiveModel( model, request, problems );
    }

    private void customizeModel( Model model )
    {
        customizeDependencies( model );
        customizeExtensions( model );
        customizePlugins( model );
    }

    private void customizeDependencies( Model model )
    {
        BuildSettings settings = configurator.getConfiguration().getBuildSettings();

        for ( Iterator<Dependency> iter = model.getDependencies().iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String scope = dependency.getScope();

            if ( blacklist.contains( groupId, artifactId ) )
            {
                logger.debug( "Removed dependency " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
                continue;
            }

            if ( settings.isSkipTests() && scope != null && scope.equals( "test" ) )
            {
                logger.debug( "Dropped dependency on " + groupId + ":" + artifactId + " because tests are skipped." );
                iter.remove();
                continue;
            }

            dependency.setVersion( replaceVersion( groupId, artifactId, dependency.getVersion() ) );
        }
    }

    private void customizeExtensions( Model model )
    {
        Build build = model.getBuild();
        if ( build == null )
            return;

        for ( Iterator<Extension> iter = build.getExtensions().iterator(); iter.hasNext(); )
        {
            Extension extension = iter.next();
            String groupId = extension.getGroupId();
            String artifactId = extension.getArtifactId();

            if ( blacklist.contains( groupId, artifactId ) )
            {
                logger.debug( "Removed extension " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
                continue;
            }

            extension.setVersion( replaceVersion( groupId, artifactId, extension.getVersion() ) );
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

            if ( blacklist.contains( groupId, artifactId ) )
            {
                logger.debug( "Removed plugin " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
                continue;
            }

            plugin.setVersion( replaceVersion( groupId, artifactId, plugin.getVersion() ) );

            if ( groupId.equals( "org.apache.maven.plugins" ) && artifactId.equals( "maven-compiler-plugin" ) )
                configureCompiler( plugin );
        }
    }

    private String replaceVersion( String groupId, String artifactId, String version )
    {
        String id = groupId + ":" + artifactId;

        if ( StringUtils.isEmpty( version ) )
        {
            logger.debug( "Missing version of dependency " + id + ", using SYSTEM." );
            return ArtifactUtils.DEFAULT_VERSION;
        }

        try
        {
            if ( VersionRange.createFromVersionSpec( version ).getRecommendedVersion() == null )
            {
                logger.debug( "Dependency " + id + " has no recommended version, falling back to SYSTEM." );
                return ArtifactUtils.DEFAULT_VERSION;
            }
        }
        catch ( InvalidVersionSpecificationException e )
        {
            logger.debug( "Dependency " + id + " is using invalid version range, falling back to SYSTEM." );
            return ArtifactUtils.DEFAULT_VERSION;
        }

        return version;
    }

    private void configureCompiler( Plugin plugin )
    {
        boolean minSourceSpecified = false;
        BigDecimal minSource = new BigDecimal( "1.5" );
        String compilerSource = configurator.getConfiguration().getBuildSettings().getCompilerSource();
        if ( compilerSource != null )
        {
            minSourceSpecified = true;
            minSource = new BigDecimal( compilerSource );
        }

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
                if ( minSourceSpecified || source.compareTo( minSource ) < 0 )
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
}
