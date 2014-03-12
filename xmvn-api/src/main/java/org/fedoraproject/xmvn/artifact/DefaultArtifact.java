/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.artifact;

import java.io.File;

/**
 * @author Mikolaj Izdebski
 */
public final class DefaultArtifact
    implements Artifact
{
    private final org.eclipse.aether.artifact.Artifact artifact;

    private final String scope;

    private final String stereotype;

    private DefaultArtifact( org.eclipse.aether.artifact.Artifact artifact, String scope, String stereotype )
    {
        this.artifact = artifact;
        this.scope = scope;
        this.stereotype = stereotype;
    }

    public DefaultArtifact( String coords )
    {
        artifact = new org.eclipse.aether.artifact.DefaultArtifact( coords );
        scope = null;
        stereotype = null;
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String version )
    {
        artifact = new org.eclipse.aether.artifact.DefaultArtifact( groupId, artifactId, extension, version );
        scope = null;
        stereotype = null;
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version )
    {
        artifact =
            new org.eclipse.aether.artifact.DefaultArtifact( groupId, artifactId, classifier, extension, version );
        scope = null;
        stereotype = null;
    }

    @Override
    public String getGroupId()
    {
        return artifact.getGroupId();
    }

    @Override
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }

    @Override
    public String getExtension()
    {
        return artifact.getExtension();
    }

    @Override
    public String getClassifier()
    {
        return artifact.getClassifier();
    }

    @Override
    public String getVersion()
    {
        return artifact.getVersion();
    }

    @Override
    public File getFile()
    {
        return artifact.getFile();
    }

    @Override
    public String getScope()
    {
        return scope;
    }

    @Override
    public String getStereotype()
    {
        return stereotype;
    }

    @Override
    public Artifact setVersion( String version )
    {
        return new DefaultArtifact( artifact.setVersion( version ), scope, stereotype );
    }

    @Override
    public Artifact setFile( File file )
    {
        return new DefaultArtifact( artifact.setFile( file ), scope, stereotype );
    }

    @Override
    public Artifact setScope( String scope )
    {
        return new DefaultArtifact( artifact, scope, stereotype );
    }

    @Override
    public Artifact setStereotype( String stereotype )
    {
        return new DefaultArtifact( artifact, scope, stereotype );
    }
}
