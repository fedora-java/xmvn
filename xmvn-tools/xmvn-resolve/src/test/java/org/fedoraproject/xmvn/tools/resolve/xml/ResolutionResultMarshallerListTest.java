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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import org.fedoraproject.xmvn.resolver.ResolutionResult;

/**
 * @author Marian Koncek
 */
public class ResolutionResultMarshallerListTest
{
    @Test
    public void testEmpty()
        throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ResolutionResultListMarshaller( Arrays.asList( new ResolutionResult[] { null } ) ).marshal( bos );

        XmlAssert.assertThat( "<results></results>" ) //
                 .and( bos.toString() ) //
                 .ignoreComments() //
                 .ignoreWhitespace() //
                 .areIdentical();
    }

    @Test
    public void testMultiple()
        throws Exception
    {
        List<ResolutionResult> list = new ArrayList<>();

        ResolutionResultBean temp;
        temp = new ResolutionResultBean();
        temp.setArtifactPath( "/dev/null" );
        temp.setCompatVersion( "comp1" );
        temp.setNamespace( "namespace1" );
        temp.setProvider( "provider1" );
        list.add( new ResolutionResultBean.Adapter().unmarshal( temp ) );

        temp = new ResolutionResultBean();
        temp.setArtifactPath( "/dev/null" );
        temp.setCompatVersion( "comp2" );
        temp.setNamespace( "namespace2" );
        temp.setProvider( "provider2" );
        list.add( new ResolutionResultBean.Adapter().unmarshal( temp ) );

        list.add( null );

        temp = new ResolutionResultBean();
        temp.setArtifactPath( "/dev/null" );
        temp.setCompatVersion( "comp3" );
        temp.setNamespace( "namespace3" );
        temp.setProvider( "provider3" );
        list.add( new ResolutionResultBean.Adapter().unmarshal( temp ) );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ResolutionResultListMarshaller( list ).marshal( bos );

        XmlAssert.assertThat( "" + //
            "<results>" + //
            "  <result>" + //
            "    <artifactPath>/dev/null</artifactPath>" + //
            "    <provider>provider1</provider>" + //
            "    <compatVersion>comp1</compatVersion>" + //
            "    <namespace>namespace1</namespace>" + //
            "  </result>" + //
            "  <result>" + //
            "    <artifactPath>/dev/null</artifactPath>" + //
            "    <provider>provider2</provider>" + //
            "    <compatVersion>comp2</compatVersion>" + //
            "    <namespace>namespace2</namespace>" + //
            "  </result>" + //
            "  <result>" + //
            "    <artifactPath>/dev/null</artifactPath>" + //
            "    <provider>provider3</provider>" + //
            "    <compatVersion>comp3</compatVersion>" + //
            "    <namespace>namespace3</namespace>" + //
            "  </result>" + //
            "</results>" ) //
                 .and( bos.toString() ) //
                 .ignoreComments() //
                 .ignoreWhitespace() //
                 .areIdentical();
    }

    @Test
    public void testSingle()
        throws Exception
    {
        ResolutionResult rr = new ResolutionResultBean.Adapter().unmarshal( new ResolutionResultBean() );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ResolutionResultListMarshaller( Arrays.asList( new ResolutionResult[] { rr } ) ).marshal( bos );

        XmlAssert.assertThat( "<results><result/></results>" ) //
                 .and( bos.toString() ) //
                 .ignoreComments() //
                 .ignoreWhitespace() //
                 .areIdentical();
    }
}
