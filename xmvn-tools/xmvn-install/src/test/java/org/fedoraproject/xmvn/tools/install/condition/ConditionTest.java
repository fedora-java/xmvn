/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collections;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.repository.ArtifactContext;

/**
 * @author Mikolaj Izdebski
 */
public class ConditionTest
{
    private ArtifactContext context1;

    private ArtifactContext context2;

    @Before
    public void setUp()
    {
        context1 =
            new ArtifactContext( new DefaultArtifact( "some-gid", "the-aid", "zip", "xyzzy", "1.2.3" ),
                                 Collections.singletonMap( "foo", "bar" ) );

        context2 =
            new ArtifactContext( new DefaultArtifact( "org.apache.maven", "maven-model", "3.0.5" ),
                                 Collections.singletonMap( "native", "true" ) );
    }

    /**
     * Test if null conditions are always met.
     * 
     * @throws Exception
     */
    @Test
    public void testNullCondition()
        throws Exception
    {
        Condition cond = new Condition( null );
        assertTrue( cond.getValue( context1 ) );
        assertTrue( cond.getValue( context2 ) );
    }

    /**
     * Test if basic conditions work.
     * 
     * @throws Exception
     */
    @Test
    public void testBasicCondition()
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "<filter>" );
        sb.append( "  <or>" );
        sb.append( "    <and>" );
        sb.append( "      <equals>" );
        sb.append( "        <extension/>" );
        sb.append( "        <string>jar</string>" );
        sb.append( "      </equals>" );
        sb.append( "      <not>" );
        sb.append( "        <equals>" );
        sb.append( "          <property>native</property>" );
        sb.append( "          <string>true</string>" );
        sb.append( "        </equals>" );
        sb.append( "      </not>" );
        sb.append( "    </and>" );
        sb.append( "    <!-- Maybe /usr/share/java is not the best place to store" );
        sb.append( "         ZIP files, but packages are doing so anyways and" );
        sb.append( "         allowing ZIPs here simplifies packaging.  TODO: find a" );
        sb.append( "         better location for ZIP files.  -->" );
        sb.append( "    <equals>" );
        sb.append( "      <extension/>" );
        sb.append( "      <string>zip</string>" );
        sb.append( "    </equals>" );
        sb.append( "  </or>" );
        sb.append( "</filter>" );

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( sb.toString() ) );
        Condition cond = new Condition( dom );
        assertTrue( cond.getValue( context1 ) );
        assertFalse( cond.getValue( context2 ) );
    }

    /**
     * Test if AND, OR and XOR operators allow more than one argument.
     * 
     * @throws Exception
     */
    @Test
    public void testTernaryOperators()
        throws Exception
    {

    }
}
