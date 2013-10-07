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

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.fedoraproject.maven.model.AbstractModelVisitor;
import org.fedoraproject.maven.utils.ArtifactUtils;

/**
 * @author Mikolaj Izdebski
 */
class RuntimeDependencyVisitor
    extends AbstractModelVisitor
{
    private final DefaultDependencyExtractionResult result;

    private static final List<String> scopes = Arrays.asList( null, "compile", "runtime" );

    public RuntimeDependencyVisitor( DefaultDependencyExtractionResult result )
    {
        this.result = result;
    }

    @Override
    public void visitDependency( Dependency dependency )
    {
        if ( !scopes.contains( dependency.getScope() ) )
            return;

        result.addDependencyArtifact( ArtifactUtils.createTypedArtifact( dependency.getGroupId(),
                                                                         dependency.getArtifactId(),
                                                                         dependency.getType(),
                                                                         dependency.getClassifier(),
                                                                         dependency.getVersion() ) );
    }
}
