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

import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * @author Mikolaj Izdebski
 */
@XmlRootElement( name = "results" )
public class CompoundResult
{
    private List<ResolutionResult> results;

    public CompoundResult()
    {
    }

    public CompoundResult( List<ResolutionResult> results )
    {
        this.results = results;
    }

    @XmlElement( name = "result" )
    public List<ResolutionResult> getResults()
    {
        return results;
    }

    public void setResults( List<ResolutionResult> results )
    {
        this.results = results;
    }
}
