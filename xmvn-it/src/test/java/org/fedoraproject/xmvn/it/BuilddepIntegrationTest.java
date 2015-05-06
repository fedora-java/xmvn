/*-
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class BuilddepIntegrationTest
    extends AbstractIntegrationTest
{
    private void assertBuilddepEqual( String... expected )
        throws Exception
    {
        setIgnoreWhitespace( true );
        Path builddepPath = Paths.get( ".xmvn-builddep" );
        assertTrue( Files.isRegularFile( builddepPath ) );

        StringBuilder sb = new StringBuilder();
        for ( String s : expected )
            sb.append( s );
        assertXMLEqual( new StringReader( sb.toString() ), Files.newBufferedReader( builddepPath ) );
    }

    @Test
    @Ignore
    public void testBuilddepExpandVariables()
        throws Exception
    {
        performTest( "verify", "org.fedoraproject.xmvn:xmvn-mojo:builddep" );

        assertBuilddepEqual( "<dependencies>", //
                             "  <dependency>", //
                             "    <groupId>junit</groupId>", //
                             "    <artifactId>junit</artifactId>", //
                             "  </dependency>", //
                             "</dependencies>" );
    }

    @Test
    @Ignore
    public void testBuilddepReactorDependencies()
        throws Exception
    {
        performTest( "verify", "org.fedoraproject.xmvn:xmvn-mojo:builddep" );

        assertBuilddepEqual( "<dependencies/>" );
    }
}
