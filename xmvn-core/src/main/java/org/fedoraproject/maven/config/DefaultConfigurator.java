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
package org.fedoraproject.maven.config;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fedoraproject.maven.config.io.xpp3.ConfigurationXpp3Reader;

@Component( role = Configurator.class )
public class DefaultConfigurator
    implements Configurator
{
    private volatile Configuration configuration;

    @Override
    public Configuration getConfiguration()
    {
        if ( configuration == null )
        {
            try
            {
                ClassLoader loader = getClass().getClassLoader();
                InputStream stream = loader.getResourceAsStream( "default-configuration.xml" );
                ConfigurationXpp3Reader reader = new ConfigurationXpp3Reader();
                configuration = reader.read( stream );
            }
            catch ( IOException | XmlPullParserException e )
            {
                throw new RuntimeException( "Failed to load embedded default configuration", e );
            }
        }

        return configuration;
    }
}
