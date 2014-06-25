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
package org.fedoraproject.xmvn.p2.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;

/**
 * @author Mikolaj Izdebski
 */
public class Package
{
    private final String id;

    private final Set<IInstallableUnit> contents = new LinkedHashSet<>();

    private final Set<IInstallableUnit> dependencies = new LinkedHashSet<>();

    public Package( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public Set<IInstallableUnit> getContents()
    {
        return contents;
    }

    public void addContent( IInstallableUnit unit )
    {
        contents.add( unit );
    }

    public void addContents( Set<IInstallableUnit> contents )
    {
        this.contents.addAll( contents );
    }

    public Set<IInstallableUnit> getDependencies()
    {
        return dependencies;
    }

    public void addDependencies( Set<IInstallableUnit> dependencies )
    {
        this.dependencies.addAll( dependencies );
    }

    @Override
    public boolean equals( Object rhs )
    {
        return rhs instanceof Package && id.equals( ( (Package) rhs ).id );
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
