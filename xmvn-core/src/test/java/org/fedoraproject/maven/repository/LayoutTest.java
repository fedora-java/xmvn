/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.repository;

import org.codehaus.plexus.PlexusTestCase;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
public class LayoutTest
    extends PlexusTestCase
{
    /**
     * Test layout objects.
     * 
     * @throws Exception
     */
    public void testLayouts()
        throws Exception
    {
        assertTrue( Layout.MAVEN.isVersioned() );
        assertTrue( Layout.JPP.isVersioned() );
        assertTrue( Layout.FLAT.isVersioned() );
        assertFalse( Layout.JPP_VERSIONLESS.isVersioned() );
        assertFalse( Layout.FLAT_VERSIONLESS.isVersioned() );

        Artifact artifact = new Artifact( "an-example.artifact", "used-FOR42.testing", "blah-1.2.3-foo", "ext-ens.ion" );

        assertEquals( "an-example/artifact/used-FOR42.testing/blah-1.2.3-foo/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion",
                      Layout.MAVEN.getArtifactPath( artifact ) );
        assertEquals( "an-example.artifact/used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion",
                      Layout.JPP.getArtifactPath( artifact ) );
        assertEquals( "an-example.artifact/used-FOR42.testing.ext-ens.ion",
                      Layout.JPP_VERSIONLESS.getArtifactPath( artifact ) );
        assertEquals( "an-example.artifact-used-FOR42.testing-blah-1.2.3-foo.ext-ens.ion",
                      Layout.FLAT.getArtifactPath( artifact ) );
        assertEquals( "an-example.artifact-used-FOR42.testing.ext-ens.ion",
                      Layout.FLAT_VERSIONLESS.getArtifactPath( artifact ) );
    }

    /**
     * Test is JPP prefixes in groupId are handled correctly.
     * 
     * @throws Exception
     */
    public void testJppPrefixes()
        throws Exception
    {
        Artifact artifact1 = new Artifact( "JPP", "testing", "1.2.3", "abc" );
        Artifact artifact2 = new Artifact( "JPP/group", "testing", "1.2.3", "abc" );
        Artifact artifact3 = new Artifact( "JPP-group", "testing", "1.2.3", "abc" );

        assertEquals( "testing.abc", Layout.JPP_VERSIONLESS.getArtifactPath( artifact1 ) );
        assertEquals( "group/testing.abc", Layout.JPP_VERSIONLESS.getArtifactPath( artifact2 ) );
        assertEquals( "JPP-group/testing.abc", Layout.JPP_VERSIONLESS.getArtifactPath( artifact3 ) );
    }
}
