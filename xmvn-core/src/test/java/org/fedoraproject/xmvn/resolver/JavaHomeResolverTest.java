/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.ResolverSettings;
import org.fedoraproject.xmvn.utils.FileUtils;

/**
 * @author Mikolaj Izdebski
 */
public class JavaHomeResolverTest
    extends InjectedTest
{
    /**
     * Test if artifacts are resolved correctly from Java home.
     * 
     * @throws Exception
     */
    @Test
    public void testJavaHomeResolver()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        ResolverSettings settings = configurator.getConfiguration().getResolverSettings();
        assertTrue( settings.getPrefixes().isEmpty() );
        assertTrue( settings.getMetadataRepositories().isEmpty() );
        settings.addPrefix( FileUtils.CWD.getAbsolutePath() );
        settings.addMetadataRepository( "src/test/resources/java-home-resolver-metadata" );

        Resolver javaHomeResolver = lookup( Resolver.class );

        ResolutionRequest comSunToolsRequest =
            new ResolutionRequest( new DefaultArtifact( "com.sun", "tools", "jar", "SYSTEM" ) );
        ResolutionResult comSunToolsResult = javaHomeResolver.resolve( comSunToolsRequest );
        assertNotNull( comSunToolsResult.getArtifactPath() );
        assertTrue( Files.exists( comSunToolsResult.getArtifactPath() ) );

        ResolutionRequest toolsRequest =
            new ResolutionRequest( new DefaultArtifact( "JAVA_HOME", "../lib/tools", "jar", "SYSTEM" ) );
        ResolutionResult toolsResult = javaHomeResolver.resolve( toolsRequest );
        assertNotNull( toolsResult.getArtifactPath() );
        assertTrue( Files.exists( toolsResult.getArtifactPath() ) );

        ResolutionRequest toolzRequest =
            new ResolutionRequest( new DefaultArtifact( "JAVA_HOME", "../lib/toolz", "jar", "SYSTEM" ) );
        ResolutionResult toolzResult = javaHomeResolver.resolve( toolzRequest );
        assertNull( toolzResult.getArtifactPath() );

        ResolutionRequest xpp3Request =
            new ResolutionRequest( new DefaultArtifact( "JPP/xpp3", "xpp3", "jar", "SYSTEM" ) );
        ResolutionResult xpp3Result = javaHomeResolver.resolve( xpp3Request );
        assertNull( xpp3Result.getArtifactPath() );
    }
}
