/*-
 * Copyright (c) 2016-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

/**
 * Integration tests for XMvn Resolver tool.
 * 
 * @author Mikolaj Izdebski
 */
public class ResolverIntegrationTest
    extends AbstractToolIntegrationTest
{
    @Test
    public void testResolveNone()
        throws Exception
    {
        assertEquals( 0, invokeTool( "xmvn-resolve" ) );
        assertFalse( getStderr().findAny().isPresent() );
        assertFalse( getStdout().findAny().isPresent() );
    }

    @Test
    public void testResolveOne()
        throws Exception
    {
        assertEquals( 0, invokeTool( "xmvn-resolve", "junit:junit" ) );
        assertFalse( getStderr().findAny().isPresent() );

        List<String> out = getStdout().collect( Collectors.toList() );
        assertEquals( 1, out.size() );

        Path jar = Paths.get( out.iterator().next() );
        assertTrue( jar.endsWith( "src/test/resources/empty.jar" ) );
        assertTrue( Files.isRegularFile( jar ) );
    }

    @Test
    public void testResolveFail()
        throws Exception
    {
        assertEquals( 1, invokeTool( "xmvn-resolve", "foobar:xyzzy" ) );
        assertTrue( getStderr().anyMatch( s -> s.endsWith( "Unable to resolve artifact foobar:xyzzy:jar:SYSTEM" ) ) );
        assertFalse( getStdout().findAny().isPresent() );
    }

    @Test
    public void testResolveRawNull()
        throws Exception
    {
        assertEquals( 2, invokeToolWithInput( "", "xmvn-resolve", "--raw-request" ) );
        assertTrue( getStderr().findAny().isPresent() );
        assertFalse( getStdout().findAny().isPresent() );
    }

    @Test
    public void testResolveRawTrash()
        throws Exception
    {
        assertEquals( 2, invokeToolWithInput( "xyzzy", "xmvn-resolve", "--raw-request" ) );
        assertTrue( getStderr().findAny().isPresent() );
        assertFalse( getStdout().findAny().isPresent() );
    }

    @Test
    public void testResolveRawNone()
        throws Exception
    {
        assertEquals( 0, invokeToolWithInput( "<requests/>", "xmvn-resolve", "--raw-request" ) );
        assertFalse( getStderr().findAny().isPresent() );
        XmlAssert.assertThat( "<results/>" ).and( getStdout().collect( Collectors.joining() ) ).areSimilar();
    }

    @Test
    public void testResolveRawOne()
        throws Exception
    {
        String input = String.join( "\n", //
                                    "<requests>", //
                                    " <![CDATA[ ]]>", //
                                    " <request>", //
                                    "  <persistentFileNeeded>false</persistentFileNeeded>", //
                                    "  <artifact>", //
                                    "   <extension>jar</extension>", //
                                    "   <artifactId>aliased-component-metadata</artifactId>", //
                                    "   <classifier/>", //
                                    "   <!-- huh -->", //
                                    "   <version>any</version>", //
                                    "   <groupId>alias-<![CDATA[test]]></groupId>", //
                                    "  </artifact>", //
                                    "  <providerNeeded><![CDATA[false]]></providerNeeded>", //
                                    " </request>", //
                                    "</requests>" );
        assertEquals( 0, invokeToolWithInput( input, "xmvn-resolve", "--raw-request" ) );
        assertFalse( getStderr().findAny().isPresent() );
        Path absPath = getBaseDir().resolve( "../dependency/plexus-component-metadata-1.7.1.jar" ).toRealPath();
        String expectedOutput = String.join( "\n", //
                                             "<results>", //
                                             " <result>", //
                                             "  <artifactPath>" + absPath + "</artifactPath>", //
                                             "  <namespace/>", //
                                             " </result>", //
                                             "</results>" );
        XmlAssert.assertThat( expectedOutput ).and( getStdout().collect( Collectors.joining( "\n" ) ) ).ignoreComments().ignoreWhitespace().areSimilar();
    }

    @Disabled
    @Test
    public void testResolveRawTwo()
        throws Exception
    {
        String input = String.join( "\n", //
                                    "<requests>", //
                                    " <request>", //
                                    "  <artifact>", //
                                    "   <groupId>foobar</groupId>", //
                                    "   <artifactId>xyzzy</artifactId>", //
                                    "  </artifact>", //
                                    " </request>", //
                                    " <request>", //
                                    "  <artifact>", //
                                    "   <artifactId>junit</artifactId>", //
                                    "   <groupId>junit</groupId>", //
                                    "  </artifact>", //
                                    " </request>", //
                                    "</requests>" );
        assertEquals( 0, invokeToolWithInput( input, "xmvn-resolve", "--raw-request" ) );
        assertTrue( getStderr().anyMatch( s -> s.endsWith( "Unable to resolve artifact foobar:xyzzy:jar:SYSTEM" ) ) );
        Path absPath = getBaseDir().resolve( "../../src/test/resources/empty.jar" ).toRealPath();
        String expectedOutput = String.join( "\n", //
                                             "<results>", //
                                             " <result/>", //
                                             " <result>", //
                                             "  <artifactPath>" + absPath + "</artifactPath>", //
                                             "  <namespace/>", //
                                             "  <compatVersion>SYSTEM</compatVersion>", //
                                             " </result>", //
                                             "</results>" );
        XmlAssert.assertThat( expectedOutput ).and( getStdout().collect( Collectors.joining( "\n" ) ) ).ignoreComments().ignoreWhitespace().areSimilar();
    }
}
