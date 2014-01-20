package org.fedoraproject.xmvn.connector.aether;

import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;

public interface ResolutionListener
{
    void resolutionRequested( ResolutionRequest request );

    void resolutionCompleted( ResolutionRequest request, ResolutionResult result );
}
