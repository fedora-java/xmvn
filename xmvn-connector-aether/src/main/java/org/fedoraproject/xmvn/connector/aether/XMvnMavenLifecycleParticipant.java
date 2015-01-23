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
package org.fedoraproject.xmvn.connector.aether;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;

/**
 * Installs some of XMvn extensions for Maven.
 * 
 * @author Mikolaj Izdebski
 */
@Named( "XMvn" )
@Singleton
public class XMvnMavenLifecycleParticipant
    extends AbstractMavenLifecycleParticipant
{
    private final XMvnWorkspaceReader workspaceReader;

    @Inject
    public XMvnMavenLifecycleParticipant( XMvnWorkspaceReader workspaceReader )
    {
        this.workspaceReader = workspaceReader;
    }

    @Override
    public void afterSessionStart( MavenSession session )
        throws MavenExecutionException
    {
        MavenExecutionRequest request = session.getRequest();

        DependencyVersionReportGenerator reportGenerator = new DependencyVersionReportGenerator();
        workspaceReader.addResolutionListener( reportGenerator );

        ChainedExecutionListener chainedListener = new ChainedExecutionListener();
        chainedListener.addExecutionListener( request.getExecutionListener() );
        chainedListener.addExecutionListener( reportGenerator );
        request.setExecutionListener( chainedListener );
    }
}
