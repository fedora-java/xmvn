/*-
 * Copyright (c) 2015-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.logging.impl.Logger;

/**
 * @author Mikolaj Izdebski
 */
class MockAgent
{
    private final String requestCommand;

    private final Logger logger;

    public MockAgent( Logger logger )
    {
        this.logger = logger;
        this.requestCommand = System.getProperty( "xmvn.resolver.requestArtifactCmd" );
    }

    public boolean tryInstallArtifact( Artifact artifact )
    {
        if ( requestCommand == null )
            return false;

        try
        {
            String cmd = String.format( "%s maven '%s'", requestCommand, artifact.toString() );
            logger.debug( "Trying to install artifact with external command: {}", cmd );

            ProcessBuilder pb = new ProcessBuilder( "sh", "-c", cmd );
            pb.redirectInput();
            pb.redirectOutput();
            pb.redirectError();
            int exit = pb.start().waitFor();

            if ( exit == 0 )
            {
                logger.info( "Artifact installed with external command: {}", artifact );
                return true;
            }
            else
            {
                logger.info( "External command failed, exit code is {}", exit );
                return false;
            }
        }
        catch ( IOException e )
        {
            logger.debug( "Failed to launch subprocess", e );
            return false;
        }
        catch ( InterruptedException e )
        {
            throw new RuntimeException( "Interrupted when waiting for subprocess to complete", e );
        }
    }
}
