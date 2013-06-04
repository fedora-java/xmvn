package org.fedoraproject.maven.dependency;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.fedoraproject.maven.model.AbstractModelVisitor;

public class RuntimeDependencyVisitor
    extends AbstractModelVisitor
{
    private final DefaultDependencyExtractionResult result;

    private static final List<String> runtimeScopes = Arrays.asList( null, "compile", "runtime" );

    public RuntimeDependencyVisitor( DefaultDependencyExtractionResult result )
    {
        this.result = result;
    }

    @Override
    public void visitDependency( Dependency dependency )
    {
        if ( !runtimeScopes.contains( dependency.getScope() ) )
            return;

        result.addDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }
}
