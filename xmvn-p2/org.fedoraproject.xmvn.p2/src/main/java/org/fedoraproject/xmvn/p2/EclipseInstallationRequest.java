/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.p2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikolaj Izdebski
 */
public class EclipseInstallationRequest
{
    private final Set<Path> plugins = new LinkedHashSet<>();

    private final Set<Path> features = new LinkedHashSet<>();

    private Path buildRoot;

    private Path targetDropinDirectory;

    private final List<Path> prefixes = new ArrayList<>();

    private final Map<String, String> packageMappings = new LinkedHashMap<>();

    public Path getBuildRoot()
    {
        return buildRoot;
    }

    public void setBuildRoot( Path buildRoot )
    {
        this.buildRoot = buildRoot;
    }

    public Path getTargetDropinDirectory()
    {
        return targetDropinDirectory;
    }

    public void setTargetDropinDirectory( Path targetDropinDirectory )
    {
        this.targetDropinDirectory = targetDropinDirectory;
    }

    public Set<Path> getPlugins()
    {
        return Collections.unmodifiableSet( plugins );
    }

    public void addPlugin( Path plugin )
    {
        plugins.add( plugin );
    }

    public Set<Path> getFeatures()
    {
        return Collections.unmodifiableSet( features );
    }

    public void addFeature( Path feature )
    {
        features.add( feature );
    }

    public List<Path> getPrefixes()
    {
        return Collections.unmodifiableList( prefixes );
    }

    public void addPrefix( Path prefix )
    {
        prefixes.add( prefix );
    }

    public Map<String, String> getPackageMappings()
    {
        return Collections.unmodifiableMap( packageMappings );
    }

    public void addPackageMapping( String artifactId, String packageId )
    {
        packageMappings.put( artifactId, packageId );
    }
}
