/*-
 * Copyright (c) 2014-2017 Red Hat, Inc.
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

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.repository.WorkspaceReader;

/**
 * Installs some of XMvn extensions for Maven.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = AbstractMavenLifecycleParticipant.class )
public class XMvnMavenLifecycleParticipant
    extends AbstractMavenLifecycleParticipant
{
    @Requirement
    private Logger logger;

    @Requirement( role = WorkspaceReader.class, hint = "ide", optional = true )
    private XMvnWorkspaceReader workspaceReader;

    @Requirement( role = XMvnMojoExecutionListener.class )
    private XMvnMojoExecutionListener mojoExecutionListener;

    @Override
    public void afterSessionStart( MavenSession session )
        throws MavenExecutionException
    {
        MavenExecutionRequest request = session.getRequest();

        DependencyVersionReportGenerator reportGenerator = new DependencyVersionReportGenerator( logger );

        if ( workspaceReader != null )
        {
            workspaceReader.addResolutionListener( mojoExecutionListener );
            workspaceReader.addResolutionListener( reportGenerator );
        }

        ChainedExecutionListener chainedListener = new ChainedExecutionListener();
        chainedListener.addExecutionListener( request.getExecutionListener() );
        chainedListener.addExecutionListener( reportGenerator );
        request.setExecutionListener( chainedListener );
    }
}
