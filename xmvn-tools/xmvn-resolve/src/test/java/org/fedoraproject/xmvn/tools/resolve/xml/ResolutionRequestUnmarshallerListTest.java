/*-
 * Copyright (c) 2018-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.resolve.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;

/**
 * @author Marian Koncek
 */
public class ResolutionRequestUnmarshallerListTest
{
    private final String resourcePath = "src/test/resources/org/fedoraproject/xmvn/tools/resolve/xml";

    @Test
    public void testEmptierList()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-emptier-list.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            // The implementation may change. Any of the two results is correct.
            assertTrue( list == null || list.isEmpty() );
        }
    }

    @Test
    public void testEmptyList()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-empty-list.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            // The implementation may change. Any of the two results is correct.
            assertTrue( list == null || list.isEmpty() );
        }
    }

    @Test
    public void testFullArtifact()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-full-artifact.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            assertEquals( 1, list.size() );

            Artifact artifact = list.get( 0 ).getArtifact();

            assertEquals( "test1", artifact.getArtifactId() );
            assertEquals( "test1", artifact.getGroupId() );
            assertEquals( "test1", artifact.getExtension() );
            assertEquals( "test1", artifact.getClassifier() );
            assertEquals( "test1", artifact.getVersion() );
            assertEquals( "/dev/null", artifact.getPath().toString() );
        }
    }

    @Test
    public void testFullRequests()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-full-requests.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            assertEquals( 6, list.size() );

            assertEquals( true, list.get( 0 ).isProviderNeeded() );
            assertEquals( true, list.get( 0 ).isPersistentFileNeeded() );

            assertEquals( false, list.get( 1 ).isProviderNeeded() );
            assertEquals( false, list.get( 1 ).isPersistentFileNeeded() );

            assertEquals( false, list.get( 2 ).isProviderNeeded() );
            assertEquals( true, list.get( 2 ).isPersistentFileNeeded() );

            assertEquals( true, list.get( 3 ).isProviderNeeded() );
            assertEquals( false, list.get( 3 ).isPersistentFileNeeded() );

            assertEquals( true, list.get( 4 ).isPersistentFileNeeded() );

            assertEquals( true, list.get( 5 ).isProviderNeeded() );
        }
    }

    @Test
    public void testIntegrationExample()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-integration-example.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            assertEquals( 2, list.size() );

            assertEquals( "foobar", list.get( 0 ).getArtifact().getGroupId() );
            assertEquals( "xyzzy", list.get( 0 ).getArtifact().getArtifactId() );

            assertEquals( "junit", list.get( 1 ).getArtifact().getArtifactId() );
            assertEquals( "junit", list.get( 1 ).getArtifact().getGroupId() );
        }
    }

    @Test
    public void testMinimalArtifacts()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-minimal-artifacts.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            assertEquals( 5, list.size() );

            int artifactNum = 1;

            for ( ResolutionRequest rr : list )
            {
                Artifact artifact = rr.getArtifact();

                assertEquals( "test" + Integer.toString( artifactNum ), artifact.getArtifactId() );
                assertEquals( "test" + Integer.toString( artifactNum ), artifact.getGroupId() );
                ++artifactNum;
            }
        }
    }

    @Test
    public void testNestedBrackets()
        throws IOException, XMLStreamException
    {
        try ( InputStream is = new FileInputStream( resourcePath + "/test-nested-brackets.xml" ) )
        {
            List<ResolutionRequest> list = new ResolutionRequestListUnmarshaller( is ).unmarshal();

            assertEquals( 1, list.size() );

            assertEquals( false, list.get( 0 ).isPersistentFileNeeded() );

            list.get( 0 ).getArtifact();
            list.get( 0 ).getArtifact().getExtension();
            assertEquals( "jar", list.get( 0 ).getArtifact().getExtension() );
            assertEquals( "aliased-component-metadata", list.get( 0 ).getArtifact().getArtifactId() );
            assertEquals( "any", list.get( 0 ).getArtifact().getVersion() );
            assertEquals( "alias-test", list.get( 0 ).getArtifact().getGroupId() );

            assertEquals( false, list.get( 0 ).isProviderNeeded() );
        }
    }
}
