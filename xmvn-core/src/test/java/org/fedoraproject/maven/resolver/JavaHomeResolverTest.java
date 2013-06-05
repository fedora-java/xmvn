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
package org.fedoraproject.maven.resolver;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.utils.FileUtils;

/**
 * @author Mikolaj Izdebski
 */
public class JavaHomeResolverTest
    extends PlexusTestCase
{
    /**
     * Test if artifacts are resolved correctly from Java home.
     * 
     * @throws Exception
     */
    public void testJavaHomeResolver()
        throws Exception
    {
        ResolverSettings settings = new ResolverSettings();
        assertTrue( settings.getPrefixes().isEmpty() );
        assertTrue( settings.getMetadataRepositories().isEmpty() );
        settings.addPrefix( FileUtils.CWD.getAbsolutePath() );
        settings.addMetadataRepository( "src/test/resources/java-home-resolver-depmaps" );

        File root = new File( settings.getPrefixes().iterator().next() );
        assertTrue( root.isDirectory() );
        JavaHomeResolver javaHomeResolver = new JavaHomeResolver( root, settings, lookup( Logger.class ) );

        ResolutionRequest comSunToolsRequest = new ResolutionRequest( "com.sun", "tools", "SYSTEM", "jar" );
        ResolutionResult comSunToolsResult = javaHomeResolver.resolve( comSunToolsRequest );
        assertNotNull( comSunToolsResult.getArtifactFile() );
        assertTrue( comSunToolsResult.getArtifactFile().exists() );

        ResolutionRequest toolsRequest = new ResolutionRequest( "JAVA_HOME", "../lib/tools", "SYSTEM", "jar" );
        ResolutionResult toolsResult = javaHomeResolver.resolve( toolsRequest );
        assertNotNull( toolsResult.getArtifactFile() );
        assertTrue( toolsResult.getArtifactFile().exists() );

        ResolutionRequest toolzRequest = new ResolutionRequest( "JAVA_HOME", "../lib/toolz", "SYSTEM", "jar" );
        ResolutionResult toolzResult = javaHomeResolver.resolve( toolzRequest );
        assertNull( toolzResult.getArtifactFile() );

        ResolutionRequest xpp3Request = new ResolutionRequest( "JPP/xpp3", "xpp3", "SYSTEM", "jar" );
        ResolutionResult xpp3Result = javaHomeResolver.resolve( xpp3Request );
        assertNull( xpp3Result.getArtifactFile() );
    }
}
