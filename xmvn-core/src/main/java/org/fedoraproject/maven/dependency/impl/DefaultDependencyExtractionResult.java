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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.fedoraproject.maven.dependency.DependencyExtractionResult;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
class DefaultDependencyExtractionResult
    implements DependencyExtractionResult
{
    private final Set<Artifact> dependencyArtifacts = new TreeSet<>();

    private String javaVersion;

    @Override
    public Set<Artifact> getDependencyArtifacts()
    {
        return Collections.unmodifiableSet( dependencyArtifacts );
    }

    public void addDependencyArtifact( Artifact artifact )
    {
        dependencyArtifacts.add( artifact );
    }

    public void addDependencyArtifact( String groupId, String artifactId, String version )
    {
        dependencyArtifacts.add( new Artifact( groupId, artifactId, version ) );
    }

    @Override
    public String getJavaVersion()
    {
        return javaVersion;
    }

    public void setJavaVersion( String javaVersion )
    {
        this.javaVersion = javaVersion;
    }
}
