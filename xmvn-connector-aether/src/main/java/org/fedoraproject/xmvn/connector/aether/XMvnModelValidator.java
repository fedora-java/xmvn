/*-
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import java.util.List;
import java.util.Objects;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.Configurator;

/**
 * Custom Maven object model (POM) validator that overrides default Maven model validator.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelValidator.class )
public class XMvnModelValidator
    extends DefaultModelValidator
{
    @Requirement
    private Logger logger;

    @Requirement
    private Configurator configurator;

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateEffectiveModel( model, request, problems );
    }

    void customizeModel( Model model )
    {
        Build build = model.getBuild() != null ? model.getBuild() : new Build();
        List<Dependency> dependencies = model.getDependencies();
        List<Extension> extensions = build.getExtensions();
        List<Plugin> plugins = build.getPlugins();

        dependencies.removeIf( this::isSkippedDependency );
        plugins.removeIf( this::isSkippedPlugin );

        dependencies.forEach( d -> d.setVersion( replaceVersion( d.getGroupId(), d.getArtifactId(),
                                                                 d.getVersion() ) ) );
        extensions.forEach( e -> e.setVersion( replaceVersion( e.getGroupId(), e.getArtifactId(), e.getVersion() ) ) );
        plugins.forEach( p -> p.setVersion( replaceVersion( p.getGroupId(), p.getArtifactId(), p.getVersion() ) ) );
    }

    private boolean matches( String field, String pattern )
    {
        return StringUtils.isEmpty( pattern ) || Objects.equals( field, pattern );
    }

    private boolean isSkippedDependency( Dependency d )
    {
        return matches( d.getScope(), "test" ) && configurator.getConfiguration().getBuildSettings().isSkipTests();
    }

    private boolean isSkippedPlugin( Plugin p )
    {
        return configurator.getConfiguration().getBuildSettings().getSkippedPlugins().stream() //
                           .anyMatch( sp -> matches( p.getGroupId(), sp.getGroupId() )
                               && matches( p.getArtifactId(), sp.getArtifactId() )
                               && StringUtils.isEmpty( sp.getExtension() ) && StringUtils.isEmpty( sp.getClassifier() )
                               && matches( p.getVersion(), sp.getVersion() ) );
    }

    private String replaceVersion( String groupId, String artifactId, String version )
    {
        String id = groupId + ":" + artifactId;

        if ( StringUtils.isEmpty( version ) )
        {
            logger.debug( "Missing version of dependency " + id + ", using " + Artifact.DEFAULT_VERSION + "." );
            return Artifact.DEFAULT_VERSION;
        }

        try
        {
            if ( VersionRange.createFromVersionSpec( version ).getRecommendedVersion() == null )
            {
                logger.debug( "Dependency " + id + " has no recommended version, falling back to "
                    + Artifact.DEFAULT_VERSION + "." );
                return Artifact.DEFAULT_VERSION;
            }
        }
        catch ( InvalidVersionSpecificationException e )
        {
            logger.debug( "Dependency " + id + " is using invalid version range, falling back to "
                + Artifact.DEFAULT_VERSION + "." );
            return Artifact.DEFAULT_VERSION;
        }

        return version;
    }
}
