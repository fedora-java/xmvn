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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class InstallationRequest
{
    private boolean checkForUnmatchedRules;

    private final Set<Artifact> artifacts = new LinkedHashSet<>();

    public boolean isCheckForUnmatchedRules()
    {
        return checkForUnmatchedRules;
    }

    public void setCheckForUnmatchedRules( boolean checkForUnmatchedRules )
    {
        this.checkForUnmatchedRules = checkForUnmatchedRules;
    }

    public void addArtifact( Artifact artifact, Path rawModelPath, Path effectiveModelPath )
    {
        Map<String, String> properties = new HashMap<>();
        if ( rawModelPath != null )
            properties.put( "xmvn.installer.rawModelPath", rawModelPath.toString() );
        if ( effectiveModelPath != null )
            properties.put( "xmvn.installer.effectiveModelPath", effectiveModelPath.toString() );
        artifacts.add( artifact.setProperties( properties ) );
    }

    public Set<Artifact> getArtifacts()
    {
        return Collections.unmodifiableSet( artifacts );
    }
}
