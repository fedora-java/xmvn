/*-
 * Copyright (c) 2013-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.StringUtils;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
class BuildDependencyVisitor
{
    private static final List<String> BUILD_SCOPES = Arrays.asList( null, "compile", "provided", "test", "runtime" );

    private static final List<String> RUNTIME_SCOPES = Arrays.asList( null, "compile", "runtime" );

    private final Function<InputLocation, Boolean> isExternalLocation;

    private final Set<Artifact> artifacts = new LinkedHashSet<>();

    public BuildDependencyVisitor( Function<InputLocation, Boolean> isExternalLocation )
    {
        this.isExternalLocation = isExternalLocation;
    }

    public Set<Artifact> getArtifacts()
    {
        return Collections.unmodifiableSet( artifacts );
    }

    private boolean isExternal( InputLocation location )
    {
        return location == null || isExternalLocation.apply( location );
    }

    private void visitParent( Parent parent )
    {
        artifacts.add( new DefaultArtifact( parent.getGroupId(), parent.getArtifactId(), "pom", parent.getVersion() ) );
    }

    private void visitDependency( Dependency dependency )
    {
        if ( isExternal( dependency.getLocation( "" ) ) )
        {
            return;
        }
        if ( !BUILD_SCOPES.contains( dependency.getScope() ) )
        {
            return;
        }

        artifacts.add( ArtifactTypeRegistry.getDefaultRegistry().createTypedArtifact( dependency.getGroupId(),
                                                                                      dependency.getArtifactId(),
                                                                                      dependency.getType(),
                                                                                      dependency.getClassifier(),
                                                                                      dependency.getVersion() ) );
    }

    private void visitBuildExtension( Extension extension )
    {
        artifacts.add( new DefaultArtifact( extension.getGroupId(), extension.getArtifactId(),
                                            extension.getVersion() ) );
    }

    private void visitBuildPlugin( Plugin plugin )
    {
        if ( isExternal( plugin.getLocation( "" ) ) )
        {
            return;
        }

        String groupId = plugin.getGroupId();
        String artifactId = plugin.getArtifactId();
        String version = plugin.getVersion();
        if ( StringUtils.isEmpty( groupId ) )
        {
            groupId = "org.apache.maven.plugins";
        }
        if ( StringUtils.isEmpty( version ) )
        {
            version = Artifact.DEFAULT_VERSION;
        }

        Artifact pluginArtifact = new DefaultArtifact( groupId, artifactId, version );
        artifacts.add( pluginArtifact );

        for ( Dependency dependency : plugin.getDependencies() )
        {
            visitBuildPluginDependency( dependency );
        }
    }

    private void visitBuildPluginDependency( Dependency dependency )
    {
        if ( isExternal( dependency.getLocation( "" ) ) )
        {
            return;
        }
        if ( !RUNTIME_SCOPES.contains( dependency.getScope() ) )
        {
            return;
        }

        artifacts.add( ArtifactTypeRegistry.getDefaultRegistry().createTypedArtifact( dependency.getGroupId(),
                                                                                      dependency.getArtifactId(),
                                                                                      dependency.getType(),
                                                                                      dependency.getClassifier(),
                                                                                      dependency.getVersion() ) );
    }

    public void visitModel( Model model )
    {
        if ( model.getParent() != null )
        {
            visitParent( model.getParent() );
        }
        for ( Dependency dependency : model.getDependencies() )
        {
            visitDependency( dependency );
        }
        if ( model.getBuild() != null )
        {
            for ( Extension extension : model.getBuild().getExtensions() )
            {
                visitBuildExtension( extension );
            }
            for ( Plugin plugin : model.getBuild().getPlugins() )
            {
                visitBuildPlugin( plugin );
            }
        }
    }
}
