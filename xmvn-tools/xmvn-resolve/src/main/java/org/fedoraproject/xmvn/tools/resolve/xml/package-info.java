/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
/**
 * @author Mikolaj Izdebski
 */
@XmlJavaTypeAdapters( { @XmlJavaTypeAdapter( value = ArtifactBean.Adapter.class, type = Artifact.class ),
    @XmlJavaTypeAdapter( value = ResolutionRequestBean.Adapter.class, type = ResolutionRequest.class ),
    @XmlJavaTypeAdapter( value = ResolutionResultBean.Adapter.class, type = ResolutionResult.class ) } )
package org.fedoraproject.xmvn.tools.resolve.xml;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
