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
package org.fedoraproject.maven.dependency.impl;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.dependency.DependencyExtractionRequest;
import org.fedoraproject.maven.dependency.DependencyExtractionResult;
import org.fedoraproject.maven.dependency.DependencyExtractor;
import org.fedoraproject.maven.model.AbstractModelVisitor;
import org.fedoraproject.maven.model.ModelFormatException;
import org.fedoraproject.maven.model.ModelProcessor;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = DependencyExtractor.class, hint = DependencyExtractor.RUNTIME )
public class RuntimeDependencyExtractor
    extends AbstractModelVisitor
    implements DependencyExtractor
{
    @Requirement
    private ModelProcessor modelProcessor;

    @Override
    public DependencyExtractionResult extract( DependencyExtractionRequest request )
        throws IOException, ModelFormatException
    {
        DefaultDependencyExtractionResult result = new DefaultDependencyExtractionResult();
        modelProcessor.processModel( request.getModelPath(), new RuntimeDependencyVisitor( result ) );
        modelProcessor.processModel( request.getModelPath(), new JavaVersionVisitor( result ) );
        return result;
    }
}
