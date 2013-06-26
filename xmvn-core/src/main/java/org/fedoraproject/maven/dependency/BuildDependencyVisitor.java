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
package org.fedoraproject.maven.dependency;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Plugin;
import org.fedoraproject.maven.model.AbstractModelVisitor;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class BuildDependencyVisitor
    extends AbstractModelVisitor
{
    private static final Set<Artifact> commonPlugins = new TreeSet<>();
    static
    {
        commonPlugins.add( new Artifact( "org.apache.maven.plugins", "maven-compiler-plugin" ) );
    }

    private final DefaultDependencyExtractionResult result;

    private static final List<String> buildScopes = Arrays.asList( null, "compile", "provided", "test" );

    private static final List<String> runtimeScopes = Arrays.asList( null, "compile", "runtime" );

    public BuildDependencyVisitor( DefaultDependencyExtractionResult result )
    {
        this.result = result;
    }

    @Override
    public void visitDependency( Dependency dependency )
    {
        if ( !buildScopes.contains( dependency.getScope() ) )
            return;

        result.addDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    @Override
    public void visitBuildExtension( Extension extension )
    {
        result.addDependencyArtifact( extension.getGroupId(), extension.getArtifactId(), extension.getVersion() );
    }

    @Override
    public void visitBuildPlugin( Plugin plugin )
    {
        String groupId = plugin.getGroupId();
        String artifactId = plugin.getArtifactId();
        String version = plugin.getVersion();
        Artifact pluginArtifact = new Artifact( groupId, artifactId, version );

        if ( !commonPlugins.contains( pluginArtifact ) )
            result.addDependencyArtifact( plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion() );
    }

    @Override
    public void visitBuildPluginDependency( Dependency dependency )
    {
        if ( !runtimeScopes.contains( dependency.getScope() ) )
            return;

        result.addDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }
}
