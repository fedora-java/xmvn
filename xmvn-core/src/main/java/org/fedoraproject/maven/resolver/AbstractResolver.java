/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.resolver;

import java.io.File;

import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
abstract class AbstractResolver
    implements Resolver
{
    @Override
    public File resolve( String groupId, String artifactId, String version, String extension )
    {
        Artifact artifact = new Artifact( groupId, artifactId, version, extension );
        return resolve( artifact );
    }

    @Override
    public abstract File resolve( Artifact artifact );
}
