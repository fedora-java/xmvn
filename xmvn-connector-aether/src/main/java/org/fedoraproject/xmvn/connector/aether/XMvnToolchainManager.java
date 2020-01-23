/*-
 * Copyright (c) 2020 Red Hat, Inc.
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

import java.util.Collections;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.DefaultToolchainManagerPrivate;
import org.apache.maven.toolchain.MisconfiguredToolchainException;
import org.codehaus.plexus.component.annotations.Component;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = XMvnToolchainManager.class )
public class XMvnToolchainManager
    extends DefaultToolchainManagerPrivate
{
    public void activate( MavenSession session )
        throws MavenExecutionException
    {
        MavenProject currentProject = session.getCurrentProject();

        try
        {
            for ( var toolchain : getToolchainsForType( "jdk", session ) )
            {
                if ( toolchain.matchesRequirements( Collections.singletonMap( "xmvn", "xmvn" ) ) )
                {
                    for ( var project : session.getAllProjects() )
                    {
                        session.setCurrentProject( project );
                        storeToolchainToBuildContext( toolchain, session );
                    }
                }
            }
        }
        catch ( MisconfiguredToolchainException e )
        {
            throw new MavenExecutionException( "Unable to configure XMvn toolchain", e );
        }
        finally
        {
            session.setCurrentProject( currentProject );
        }
    }
}
