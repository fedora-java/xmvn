/*-
 * Copyright (c) 2018-2019 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
        new ResolutionResultListMarshaller( Arrays.asList( new ResolutionResult[] { null } ) ).marshal( System.out );
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

        new ResolutionResultListMarshaller( list ).marshal( System.out );
    }

    @Test
    public void testSingle()
        throws Exception
    {
        ResolutionResult rr = new ResolutionResultBean.Adapter().unmarshal( new ResolutionResultBean() );
        new ResolutionResultListMarshaller( Arrays.asList( new ResolutionResult[] { rr } ) ).marshal( System.out );
    }
}
