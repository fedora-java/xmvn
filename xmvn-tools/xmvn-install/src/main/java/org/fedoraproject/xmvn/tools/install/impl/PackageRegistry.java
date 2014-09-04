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
package org.fedoraproject.xmvn.tools.install.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.tools.install.JavaPackage;

/**
 * @author Mikolaj Izdebski
 */
class PackageRegistry
{
    private final Map<String, JavaPackage> packages = new LinkedHashMap<>();

    private final InstallerSettings settings;

    private final String basePackageName;

    public PackageRegistry( InstallerSettings settings, String basePackageName )
    {
        this.settings = settings;
        this.basePackageName = basePackageName;
    }

    private Path getMetadataFilePath( String id )
    {
        String metadataBaseName = basePackageName + ( id.isEmpty() ? "" : "-" ) + id;

        Path metadatDir = Paths.get( settings.getMetadataDir() );
        return metadatDir.resolve( metadataBaseName + ".xml" );
    }

    public JavaPackage getPackageById( String id )
    {
        if ( id == null || id.equals( "__default" ) )
            id = "";

        if ( id.equals( "__noinstall" ) )
            return null;

        JavaPackage pkg = packages.get( id );

        if ( pkg == null )
        {
            pkg = new JavaPackage( id, getMetadataFilePath( id ) );
            packages.put( id, pkg );
        }

        return pkg;
    }

    public Set<JavaPackage> getPackages()
    {
        return new LinkedHashSet<>( packages.values() );
    }
}
