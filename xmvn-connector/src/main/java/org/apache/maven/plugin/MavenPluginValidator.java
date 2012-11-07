/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.apache.maven.plugin;

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

/**
 * This is a simple Maven plugin validator that pretends that all plugins have valid descriptors.
 * 
 * @author Mikolaj Izdebski
 */
public class MavenPluginValidator
{
    public MavenPluginValidator( Artifact pluginArtifact )
    {
    }

    public void validate( PluginDescriptor pluginDescriptor )
    {
        pluginDescriptor.setVersion( "SYSTEM" );
    }

    public boolean hasErrors()
    {
        return false;
    }

    public List<String> getErrors()
    {
        return Collections.emptyList();
    }
}
