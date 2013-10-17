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
package org.fedoraproject.maven.installer;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;

import java.nio.file.Files;

/**
 * @author Mikolaj Izdebski
 */
public class NativeCodeInstallerTest
    extends AbstractInstallerTest
{
    /**
     * Test installation of JARs containing or using native code.
     * 
     * @throws Exception
     */
    public void testNativeJarInstallation()
        throws Exception
    {
        addJarArtifact( "dummy-1" );
        addJarArtifact( "dummy-2", "uses-native-code" );
        addJarArtifact( "dummy-3", "contains-native-code" );
        performInstallation();

        setIgnoreWhitespace( true );

        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/jar/dummy-1.jar" ) ) );
        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/native/dummy-2.jar" ) ) );
        assertTrue( Files.isRegularFile( installRoot.resolve( "repo/native/dummy-3.jar" ) ) );

        assertFalse( Files.exists( installRoot.resolve( "repo/native/dummy-1.jar" ) ) );
        assertFalse( Files.exists( installRoot.resolve( "repo/jar/dummy-2.jar" ) ) );
        assertFalse( Files.exists( installRoot.resolve( "repo/jar/dummy-3.jar" ) ) );
    }
}
