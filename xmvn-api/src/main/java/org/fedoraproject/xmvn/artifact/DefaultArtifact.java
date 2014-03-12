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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mikolaj Izdebski
 */
public final class DefaultArtifact
    implements Artifact
{
    private static final String KEY_SCOPE = "xmvn.artifact.scope";

    private static final String KEY_STEREOTYPE = "xmvn.artifact.stereotype";

    private final org.eclipse.aether.artifact.Artifact artifact;

    private DefaultArtifact( org.eclipse.aether.artifact.Artifact artifact )
    {
        this.artifact = artifact;
    }

    public DefaultArtifact( String coords )
    {
        artifact = new org.eclipse.aether.artifact.DefaultArtifact( coords );
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String version )
    {
        artifact = new org.eclipse.aether.artifact.DefaultArtifact( groupId, artifactId, extension, version );
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version )
    {
        artifact =
            new org.eclipse.aether.artifact.DefaultArtifact( groupId, artifactId, classifier, extension, version );
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
        return getProperty( KEY_SCOPE, "" );
    }

    @Override
    public String getStereotype()
    {
        return getProperty( KEY_STEREOTYPE, "" );
    }

    @Override
    public Artifact setVersion( String version )
    {
        return new DefaultArtifact( artifact.setVersion( version ) );
    }

    @Override
    public Artifact setFile( File file )
    {
        return new DefaultArtifact( artifact.setFile( file ) );
    }

    @Override
    public Artifact setScope( String scope )
    {
        return setProperty( KEY_SCOPE, scope );
    }

    @Override
    public Artifact setStereotype( String stereotype )
    {
        return setProperty( KEY_STEREOTYPE, stereotype );
    }

    @Override
    public String getProperty( String key, String defaultValue )
    {
        return getProperty( key, defaultValue );
    }

    @Override
    public Map<String, String> getProperties()
    {
        return getProperties();
    }

    @Override
    public Artifact setProperties( Map<String, String> properties )
    {
        return new DefaultArtifact( artifact.setProperties( properties ) );
    }

    private Artifact setProperty( String key, String value )
    {
        Map<String, String> properties = new LinkedHashMap<>( artifact.getProperties() );
        properties.put( key, value );
        return setProperties( properties );
    }
}
