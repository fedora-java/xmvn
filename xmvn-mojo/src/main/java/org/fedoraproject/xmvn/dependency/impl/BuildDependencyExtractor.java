/*-
 * Copyright (c) 2013-2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.dependency.impl;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.fedoraproject.xmvn.dependency.DependencyExtractionRequest;
import org.fedoraproject.xmvn.dependency.DependencyExtractionResult;
import org.fedoraproject.xmvn.dependency.DependencyExtractor;
import org.fedoraproject.xmvn.model.AbstractModelVisitor;
import org.fedoraproject.xmvn.model.ModelFormatException;
import org.fedoraproject.xmvn.model.ModelProcessor;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named( DependencyExtractor.BUILD )
@Singleton
public class BuildDependencyExtractor
    extends AbstractModelVisitor
    implements DependencyExtractor
{
    private final ModelProcessor modelProcessor;

    @Inject
    public BuildDependencyExtractor( ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
    }

    @Override
    public DependencyExtractionResult extract( DependencyExtractionRequest request )
        throws IOException, ModelFormatException
    {
        DefaultDependencyExtractionResult result = new DefaultDependencyExtractionResult();
        modelProcessor.processModel( request.getModelPath(), new BuildDependencyVisitor( result ) );
        modelProcessor.processModel( request.getModelPath(), new JavaVersionVisitor( result ) );
        return result;
    }
}
