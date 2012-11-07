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
package org.fedoraproject.maven.connector;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Model customizer that sets missing dependency verions to "SYSTEM".
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelCustomizer.class )
public class DependencyVersionSanitizer
    implements ModelCustomizer
{
    @Override
    public void customizeModel( Model model )
    {
        List<Dependency> dependencies = model.getDependencies();

        for ( Iterator<Dependency> iter = dependencies.iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            if ( dependency.getVersion() == null )
                dependency.setVersion( "SYSTEM" );
        }
    }
}
