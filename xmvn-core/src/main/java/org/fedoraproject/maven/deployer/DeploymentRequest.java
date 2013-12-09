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
package org.fedoraproject.maven.deployer;

import java.nio.file.Path;

import org.eclipse.aether.artifact.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class DeploymentRequest
{
    private Artifact artifact;

    private Path rawModelPath;

    private Path effectiveModelPath;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public Path getRawModelPath()
    {
        return rawModelPath;
    }

    public void setRawModelPath( Path rawModelPath )
    {
        this.rawModelPath = rawModelPath;
    }

    public Path getEffectiveModelPath()
    {
        return effectiveModelPath;
    }

    public void setEffectiveModelPath( Path effectiveModelPath )
    {
        this.effectiveModelPath = effectiveModelPath;
    }
}
