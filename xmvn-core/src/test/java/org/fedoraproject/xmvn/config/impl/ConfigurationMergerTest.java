/*-
 * Copyright (c) 2016-2019 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxReader;
import org.fedoraproject.xmvn.config.io.stax.ConfigurationStaxWriter;
import org.fedoraproject.xmvn.test.AbstractTest;

/**
 * @author Mikolaj Izdebski
 */
public class ConfigurationMergerTest
    extends AbstractTest
{
    private ConfigurationMerger merger;

    @BeforeEach
    public void setUp()
    {
        merger = new ConfigurationMerger();
    }

    private static String toString( Configuration conf )
        throws Exception
    {
        StringWriter sw = new StringWriter();
        new ConfigurationStaxWriter().write( sw, conf );
        return sw.getBuffer().toString().replaceFirst( "^<\\?xml[^>]+>", "" );
    }

    @Test
    public void testMerge()
        throws Exception
    {
        Configuration c1 = new ConfigurationStaxReader().read( "src/test/resources/conf-dominant.xml" );
        Configuration c2 = new ConfigurationStaxReader().read( "src/test/resources/conf-recessive.xml" );
        Configuration c4 = new ConfigurationStaxReader().read( "src/test/resources/conf-superdominant.xml" );

        Configuration c3 = merger.merge( null, c2 );
        assertEquals( toString( c2 ), toString( c3 ) );

        Configuration c5 = merger.merge( c1, c2 );

        Configuration out = merger.merge( c4, c5 );

        assertEquals( 3, out.getProperties().size() );
        assertEquals( "v1", out.getProperties().get( "p1" ) );
        assertEquals( "v2", out.getProperties().get( "p2" ) );
        assertEquals( "v3", out.getProperties().get( "p3" ) );
        assertEquals( true, out.getBuildSettings().isDebug() );
        assertEquals( false, out.getBuildSettings().isSkipTests() );
        assertEquals( true, out.getResolverSettings().isDebug() );
        assertEquals( true, out.getInstallerSettings().isDebug() );
        assertEquals( "/foo/bar", out.getInstallerSettings().getMetadataDir() );
        assertEquals( false, out.getResolverSettings().isIgnoreDuplicateMetadata() );

        Configuration c6 = merger.merge( c2, c2.clone() );
        assertEquals( toString( c2 ), toString( c6 ) );
    }
}
