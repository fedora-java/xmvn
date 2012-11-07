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
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

/**
 * Model customizer that removes all dependencies with test scope if tests are being skipped.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelCustomizer.class, hint = "TestDependencyRemover" )
public class TestDependencyRemover
    implements ModelCustomizer
{
    @Requirement
    private Logger logger;

    @Override
    public void customizeModel( Model model )
    {
        if ( Parameters.SKIP_TESTS == false )
            return;

        List<Dependency> dependencies = model.getDependencies();

        for ( Iterator<Dependency> iter = dependencies.iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            String scope = dependency.getScope();
            if ( scope != null && scope.equals( "test" ) )
            {
                iter.remove();
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                logger.debug( "Dropped dependency on " + groupId + ":" + artifactId + " because tests are skipped." );
            }
        }
    }
}
