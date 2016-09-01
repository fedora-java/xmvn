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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;

/**
 * @author Mikolaj Izdebski
 */
@XmlRootElement( name = "requests" )
public class CompoundRequest
{
    private List<ResolutionRequest> requests;

    public CompoundRequest()
    {
    }

    public CompoundRequest( List<ResolutionRequest> requests )
    {
        this.requests = requests;
    }

    @XmlElement( name = "request" )
    public List<ResolutionRequest> getRequests()
    {
        return requests;
    }

    public void setRequests( List<ResolutionRequest> requests )
    {
        this.requests = requests;
    }
}
