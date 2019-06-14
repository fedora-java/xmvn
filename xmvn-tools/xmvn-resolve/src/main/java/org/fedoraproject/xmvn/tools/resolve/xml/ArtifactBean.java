/*-
 * Copyright (c) 2014-2019 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve.xml;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactBean
{
    private String groupId;

    private String artifactId;

    private String extension;

    private String classifier;

    private String version;

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension( String extension )
    {
        this.extension = extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    /**
     * @author Mikolaj Izdebski
     */
    public static class Adapter
    {
        private static String nullify( String value, String defaultValue )
        {
            return value.equals( defaultValue ) ? null : value;
        }

        public ArtifactBean marshal( Artifact artifact )
            throws Exception
        {
            ArtifactBean bean = new ArtifactBean();

            bean.setGroupId( artifact.getGroupId() );
            bean.setArtifactId( artifact.getArtifactId() );
            bean.setExtension( nullify( artifact.getExtension(), Artifact.DEFAULT_EXTENSION ) );
            bean.setClassifier( nullify( artifact.getClassifier(), "" ) );
            bean.setVersion( nullify( artifact.getVersion(), Artifact.DEFAULT_VERSION ) );

            return bean;
        }

        public Artifact unmarshal( ArtifactBean bean )
            throws Exception
        {
            return new DefaultArtifact( bean.getGroupId(), bean.getArtifactId(), bean.getExtension(),
                                        bean.getClassifier(), bean.getVersion() );
        }
    }
}
