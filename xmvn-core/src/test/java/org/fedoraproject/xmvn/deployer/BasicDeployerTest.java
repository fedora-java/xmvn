/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.test.AbstractTest;

/**
 * @author Mikolaj Izdebski
 */
public class BasicDeployerTest
    extends AbstractTest
{
    /**
     * Test if Sisu can load deployer component.
     * 
     * @throws Exception
     */
    @Test
    public void testComponentLookup()
        throws Exception
    {
        Deployer deployer = getService( Deployer.class );
        assertNotNull( deployer );
    }

    @Test
    public void testDeployment()
        throws Exception
    {
        Deployer deployer = getService( Deployer.class );
        Path plan = Files.createTempDirectory( "xmvn-test" ).resolve( "plan.xml" );
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath( plan );
        req.setArtifact( new DefaultArtifact( "g:a:v" ).setPath( Paths.get( "src/test/resources/simple.xml" ) ) );
        req.addProperty( "foo", "bar" );
        req.addDependency( new DefaultArtifact( "g1:a1:e1:c1:v1" ) );
        req.addDependency( new DefaultArtifact( "g2:a2:e2:c2:v2" ), true,
                           Arrays.asList( new DefaultArtifact( "e:e:e:e:e" ),
                                          new DefaultArtifact( "eg2:ea2:ee2:ec2:ev2" ) ) );
        deployer.deploy( req );
        DeploymentRequest req2 = new DeploymentRequest();
        req2.setPlanPath( plan );
        req2.setArtifact( new DefaultArtifact( "foo:bar:pom:" ).setPath( Paths.get( "/dev/null" ) ) );
        deployer.deploy( req2 );

        XmlAssert.assertThat( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
            + "<metadata xmlns=\"http://fedorahosted.org/xmvn/METADATA/3.2.0\">\n" //
            + "  <artifacts>\n" //
            + "    <artifact>\n" //
            + "      <groupId>g</groupId>\n" //
            + "      <artifactId>a</artifactId>\n" //
            + "      <version>v</version>\n" //
            + "      <path>src/test/resources/simple.xml</path>\n" //
            + "      <properties>\n" //
            + "        <foo>bar</foo>\n" //
            + "      </properties>\n" //
            + "      <dependencies>\n" //
            + "        <dependency>\n" //
            + "          <groupId>g1</groupId>\n" //
            + "          <artifactId>a1</artifactId>\n" //
            + "          <extension>e1</extension>\n" //
            + "          <classifier>c1</classifier>\n" //
            + "          <requestedVersion>v1</requestedVersion>\n" //
            + "        </dependency>\n" //
            + "        <dependency>\n" //
            + "          <groupId>g2</groupId>\n" //
            + "          <artifactId>a2</artifactId>\n" //
            + "          <extension>e2</extension>\n" //
            + "          <classifier>c2</classifier>\n" //
            + "          <requestedVersion>v2</requestedVersion>\n" //
            + "          <optional>true</optional>\n" //
            + "          <exclusions>\n" //
            + "            <exclusion>\n" //
            + "              <groupId>e</groupId>\n" //
            + "              <artifactId>e</artifactId>\n" //
            + "            </exclusion>\n" //
            + "            <exclusion>\n" //
            + "              <groupId>eg2</groupId>\n" //
            + "              <artifactId>ea2</artifactId>\n" //
            + "            </exclusion>\n" //
            + "          </exclusions>\n" //
            + "        </dependency>\n" //
            + "      </dependencies>\n" //
            + "    </artifact>\n" //
            + "    <artifact>\n" //
            + "      <groupId>foo</groupId>\n" //
            + "      <artifactId>bar</artifactId>\n" //
            + "      <extension>pom</extension>\n" //
            + "      <version>SYSTEM</version>\n" //
            + "      <path>/dev/null</path>\n" //
            + "    </artifact>\n" //
            + "  </artifacts>\n" //
            + "</metadata>\n" ).and( plan.toFile() ).ignoreComments().ignoreWhitespace().areSimilar();
    }

    @Test
    public void testReadError()
        throws Exception
    {
        Deployer deployer = getService( Deployer.class );
        Path plan = Files.createTempDirectory( "xmvn-test" ).resolve( "plan.xml" );
        Files.createDirectory( plan );
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath( plan );
        req.setArtifact( new DefaultArtifact( "g:a:v" ).setPath( Paths.get( "src/test/resources/simple.xml" ) ) );
        DeploymentResult res = deployer.deploy( req );
        assertNotNull( res.getException() );
        assertTrue( IOException.class.isAssignableFrom( res.getException().getClass() ) );
        assertEquals( "Failed to parse reactor installation plan", res.getException().getMessage() );
    }

    static final Pattern PROCESS_UID_PATTERN = Pattern.compile( "^Uid:\\s+\\d+\\s+(\\d+)\\s+\\d+\\s+\\d+\\s*$" );

    private boolean runningAsRoot()
    {
        try
        {
            return Files.lines( Paths.get( "/proc/self/status" ) ).map( s ->
            {
                Matcher matcher = PROCESS_UID_PATTERN.matcher( s );

                if ( matcher.matches() )
                {
                    if ( "0".equals( matcher.group( 1 ) ) )
                    {
                        return true;
                    }
                }

                return false;
            } ).anyMatch( result -> result );
        }
        catch ( IOException ex )
        {
            System.err.println( "Unable to read from \"/proc/self/status\"" );
            return false;
        }
    }

    @Test
    public void testWriteError()
        throws Exception
    {
        assumeFalse( runningAsRoot() );
        Deployer deployer = getService( Deployer.class );
        Path plan = Files.createTempDirectory( "xmvn-test" ).resolve( "plan.xml" );
        try ( BufferedWriter bw = Files.newBufferedWriter( plan ) )
        {
            bw.write( "<metadata/>" );
        }
        Files.setPosixFilePermissions( plan, Collections.singleton( PosixFilePermission.OTHERS_READ ) );
        DeploymentRequest req = new DeploymentRequest();
        req.setPlanPath( plan );
        req.setArtifact( new DefaultArtifact( "g:a:v" ).setPath( Paths.get( "src/test/resources/simple.xml" ) ) );
        DeploymentResult res = deployer.deploy( req );
        assertNotNull( res.getException() );
        assertTrue( IOException.class.isAssignableFrom( res.getException().getClass() ) );
    }
}
