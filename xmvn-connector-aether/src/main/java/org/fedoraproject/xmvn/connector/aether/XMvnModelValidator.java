/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.aether;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.BuildSettings;
import org.fedoraproject.xmvn.config.Configurator;

/**
 * Custom Maven object model (POM) validator that overrides default Maven model validator.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "default" )
@Singleton
public class XMvnModelValidator
    extends DefaultModelValidator
{
    private final Logger logger = LoggerFactory.getLogger( XMvnModelValidator.class );

    private final Configurator configurator;

    @Inject
    public XMvnModelValidator( Configurator configurator )
    {
        this.configurator = configurator;
    }

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateEffectiveModel( model, request, problems );
    }

    void customizeModel( Model model )
    {
        BuildSettings settings = configurator.getConfiguration().getBuildSettings();
        Build build = model.getBuild() != null ? model.getBuild() : new Build();
        List<Dependency> dependencies = model.getDependencies();
        List<Extension> extensions = build.getExtensions();
        List<Plugin> plugins = build.getPlugins();

        if ( settings.isSkipTests() )
            dependencies.removeIf( d -> StringUtils.equals( d.getScope(), "test" ) );

        dependencies.forEach( d -> d.setVersion( replaceVersion( d.getGroupId(), d.getArtifactId(),
                                                                 d.getVersion() ) ) );
        extensions.forEach( e -> e.setVersion( replaceVersion( e.getGroupId(), e.getArtifactId(), e.getVersion() ) ) );
        plugins.forEach( p -> p.setVersion( replaceVersion( p.getGroupId(), p.getArtifactId(), p.getVersion() ) ) );

        plugins.stream().filter( p -> p.getGroupId().equals( "org.apache.maven.plugins" )
            && p.getArtifactId().equals( "maven-compiler-plugin" ) ).forEach( p -> configureCompiler( p ) );
    }

    private String replaceVersion( String groupId, String artifactId, String version )
    {
        String id = groupId + ":" + artifactId;

        if ( StringUtils.isEmpty( version ) )
        {
            logger.debug( "Missing version of dependency {}, using {}.", id, Artifact.DEFAULT_VERSION );
            return Artifact.DEFAULT_VERSION;
        }

        try
        {
            if ( VersionRange.createFromVersionSpec( version ).getRecommendedVersion() == null )
            {
                logger.debug( "Dependency {} has no recommended version, falling back to {}.", id,
                              Artifact.DEFAULT_VERSION );
                return Artifact.DEFAULT_VERSION;
            }
        }
        catch ( InvalidVersionSpecificationException e )
        {
            logger.debug( "Dependency {} is using invalid version range, falling back to {}.", id,
                          Artifact.DEFAULT_VERSION );
            return Artifact.DEFAULT_VERSION;
        }

        return version;
    }

    private void configureCompiler( Plugin plugin )
    {
        boolean minSourceSpecified = false;
        BigDecimal minSource = new BigDecimal( "1.6" );
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

                // Source must be at least 1.6
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
