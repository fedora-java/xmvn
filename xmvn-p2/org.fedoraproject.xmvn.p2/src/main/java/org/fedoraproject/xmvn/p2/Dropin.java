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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Mikolaj Izdebski
 */
public class Dropin
{
    private final String id;

    private final Path path;

    private final Set<Provide> osgiProvides = new LinkedHashSet<>();

    public Dropin( String id, Path path )
    {
        this.id = id;
        this.path = path;
    }

    public String getId()
    {
        return id;
    }

    public Path getPath()
    {
        return path;
    }

    public Set<Provide> getOsgiProvides()
    {
        return osgiProvides;
    }

    public void addProvide( Provide provide )
    {
        osgiProvides.add( provide );
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj != null && obj instanceof Dropin && id.equals( ( (Dropin) obj ).id );
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
