/*-
 * Copyright (c) 2017-2020 Red Hat, Inc.
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
package org.fedoraproject.xmvn.model;

import java.io.StringWriter;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import org.fedoraproject.xmvn.model.impl.DefaultModelProcessor;

/**
 * @author Mikolaj Izdebski
 */
public class ModelProcessorTest
{
    private ModelProcessor mp;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        mp = new DefaultModelProcessor();
    }

    private Model full()
        throws Exception
    {
        return new MavenXpp3Reader().read( ModelProcessorTest.class.getClassLoader().getResourceAsStream( "full-pom.xml" ) );
    }

    private String m2s( Model m )
        throws Exception
    {
        StringWriter sw = new StringWriter();
        new MavenXpp3Writer().write( sw, m );
        return sw.toString();
    }

    @Test
    public void testNopProcessor()
        throws Exception
    {
        Model m = full();
        mp.processModel( m, new AbstractModelVisitor() );
    }

    @Test
    public void testRemovingProcessor()
        throws Exception
    {
        Model m = full();
        ModelVisitor mock = EasyMock.createNiceMock( ModelVisitor.class );
        EasyMock.replay( mock );
        mp.processModel( m, mock );
        XmlAssert.assertThat( "<?xml version=\"1.0\"?>" + //
            "<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0" + //
            " http://maven.apache.org/xsd/maven-4.0.0.xsd\"" + //
            " xmlns=\"http://maven.apache.org/POM/4.0.0\"" + //
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + //
            "<modelVersion/>" + //
            "<groupId/>" + //
            "<artifactId/>" + //
            "<version/>" + //
            "<packaging/>" + //
            "<name/>" + //
            "<description/>" + //
            "<url/>" + //
            "<inceptionYear/>" + //
            "</project>" ).and( m2s( m ) ).ignoreComments().ignoreWhitespace().areSimilar();
    }
}
