/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;

@XmlRootElement( name = "request" )
public class ResolutionRequestBean
{
    private Artifact artifact;

    private boolean isProviderNeeded;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public boolean isProviderNeeded()
    {
        return isProviderNeeded;
    }

    public void setProviderNeeded( boolean isProviderNeeded )
    {
        this.isProviderNeeded = isProviderNeeded;
    }

    public static class Adapter
        extends XmlAdapter<ResolutionRequestBean, ResolutionRequest>
    {
        @Override
        public ResolutionRequestBean marshal( ResolutionRequest request )
            throws Exception
        {
            ResolutionRequestBean bean = new ResolutionRequestBean();

            bean.setArtifact( request.getArtifact() );
            bean.setProviderNeeded( request.isProviderNeeded() );

            return bean;
        }

        @Override
        public ResolutionRequest unmarshal( ResolutionRequestBean bean )
            throws Exception
        {
            ResolutionRequest request = new ResolutionRequest();

            request.setArtifact( bean.getArtifact() );
            request.setProviderNeeded( bean.isProviderNeeded() );
            request.setPersistentFileNeeded( true );

            return request;
        }
    }
}
