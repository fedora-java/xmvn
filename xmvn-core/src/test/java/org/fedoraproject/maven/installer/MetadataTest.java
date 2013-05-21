/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.installer;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.util.BitBucketLogger;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataTest
    extends PlexusTestCase
{
    /**
     * Test if generated metadata file is parseable as XML document.
     * 
     * @throws Exception
     */
    public void testXmlStntax()
        throws Exception
    {
        Configuration configuration = lookup( Configurator.class ).getDefaultConfiguration();
        InstallerSettings installerSettings = configuration.getInstallerSettings();

        Path depmapFile = Files.createTempFile( "xmvn-test-", ".xml" );

        FragmentFile fragmentFile = new FragmentFile( new BitBucketLogger() );
        fragmentFile.write( depmapFile, false, installerSettings );

        assertTrue( Files.exists( depmapFile ) );
        assertTrue( Files.isRegularFile( depmapFile ) );
        assertTrue( Files.isReadable( depmapFile ) );

        XmlPullParser xpp = new MXParser();
        xpp.setInput( new FileReader( depmapFile.toFile() ) );

        while ( xpp.getEventType() != XmlPullParser.END_DOCUMENT )
            xpp.next();
    }
}
