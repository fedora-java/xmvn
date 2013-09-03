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
package org.fedoraproject.maven.installer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class InstallationRequest
{
    private boolean checkForUnmatchedRules;

    private final Set<Artifact> artifacts = new LinkedHashSet<>();

    private String basePackageName;

    private Path installRoot;

    public boolean isCheckForUnmatchedRules()
    {
        return checkForUnmatchedRules;
    }

    public void setCheckForUnmatchedRules( boolean checkForUnmatchedRules )
    {
        this.checkForUnmatchedRules = checkForUnmatchedRules;
    }

    public void addArtifact( Artifact artifact )
    {
        artifacts.add( artifact );
    }

    public Set<Artifact> getArtifacts()
    {
        return Collections.unmodifiableSet( artifacts );
    }

    public String getBasePackageName()
    {
        return basePackageName;
    }

    public void setBasePackageName( String basePackageName )
    {
        this.basePackageName = basePackageName;
    }

    public Path getInstallRoot()
    {
        return installRoot;
    }

    public void setInstallRoot( Path installRoot )
    {
        this.installRoot = installRoot;
    }
}
