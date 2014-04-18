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
package org.fedoraproject.xmvn.model.impl;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.fedoraproject.xmvn.model.ModelFormatException;
import org.fedoraproject.xmvn.model.ModelReader;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultModelReader
    implements ModelReader
{
    @Override
    public Model readModel( Path modelPath )
        throws IOException, ModelFormatException
    {
        try (Reader fileReader = Files.newBufferedReader( modelPath, StandardCharsets.UTF_8 ))
        {
            MavenXpp3Reader modelReader = new MavenXpp3Reader();
            return modelReader.read( fileReader );
        }
        catch ( XmlPullParserException e )
        {
            throw new ModelFormatException( "Unparseable Maven POM", e );
        }
    }
}
