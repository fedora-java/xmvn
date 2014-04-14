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
package org.fedoraproject.xmvn.install;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Simacek
 */
public class JavaVersionTest
    extends AbstractInstallerTest
{
    static final StringBuilder depmap = new StringBuilder();
    static
    {
        depmap.append( "<dependencyMap>" );
        depmap.append( "  <requiresJava>1.7</requiresJava>" );
        depmap.append( "  <dependency>" );
        depmap.append( "    <maven>" );
        depmap.append( "      <groupId>org.jboss.naming</groupId>" );
        depmap.append( "      <artifactId>jnpserver</artifactId>" );
        depmap.append( "      <version>5.0.6.CR1</version>" );
        depmap.append( "    </maven>" );
        depmap.append( "    <jpp>" );
        depmap.append( "      <groupId>JPP</groupId>" );
        depmap.append( "      <artifactId>jnpserver</artifactId>" );
        depmap.append( "    </jpp>" );
        depmap.append( "  </dependency>" );
        depmap.append( "</dependencyMap>" );
    }

    /**
     * Test for variable expansion inside configuration
     * <p>
     * Test disabled for now. It's not clear if and how this functionality will be implemented.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testJavaVersionExpansion()
        throws Exception
    {
        addJarArtifact( "jnpserver_vars" );
        performInstallation();
        setIgnoreWhitespace( true );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }

    /**
     * Test for generating Java version requirement from compiler commandline arguments
     * <p>
     * Test disabled for now. Implementing this functionality will be considered after installer refactoring.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testJavaVersionArguments()
        throws Exception
    {
        addJarArtifact( "jnpserver_args" );
        performInstallation();
        setIgnoreWhitespace( true );
        assertXmlEqual( depmap, "depmaps/package.xml" );
    }
}
