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
package org.fedoraproject.maven.installer;

import java.util.Collections;
import java.util.Iterator;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.Stereotype;
import org.fedoraproject.maven.config.impl.DefaultConfigurator;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Configurator.class )
public class InstallerTestConfigurator
    extends DefaultConfigurator
{
    private void removeRepo( Configuration configuration, String id )
    {
        Iterator<Repository> it = configuration.getRepositories().iterator();
        while ( it.hasNext() )
            if ( it.next().getId().equals( id ) )
                it.remove();
    }

    private Repository addCompound( Configuration configuration, String id, String... childreen )
    {
        removeRepo( configuration, id );

        Repository repo = new Repository();
        repo.setId( id );
        repo.setType( "compound" );

        Xpp3Dom config = new Xpp3Dom( "configuration" );
        repo.setConfiguration( config );
        Xpp3Dom repos = new Xpp3Dom( "repositories" );
        config.addChild( repos );

        for ( String childId : childreen )
        {
            Xpp3Dom child = new Xpp3Dom( "repository" );
            child.setValue( childId );
            repos.addChild( child );
        }

        configuration.addRepository( repo );
        return repo;
    }

    private Repository addRepo( Configuration configuration, String id, String type, String root )
    {
        removeRepo( configuration, id );

        Repository repo = new Repository();
        repo.setId( id );
        repo.setType( type );
        repo.addProperty( "root", root );

        configuration.addRepository( repo );
        return repo;
    }

    @Override
    public synchronized Configuration getConfiguration()
    {
        Configuration configuration = getDefaultConfiguration();

        InstallerSettings installerSettings = configuration.getInstallerSettings();
        installerSettings.setEnableRawPoms( true );
        installerSettings.setEnableEffectivePoms( true );
        installerSettings.setMetadataDir( "depmaps" );

        PackagingRule rule = new PackagingRule();
        configuration.addArtifactManagement( rule );

        org.fedoraproject.maven.config.Artifact glob = new org.fedoraproject.maven.config.Artifact();
        glob.setArtifactId( "{*}" );
        rule.setArtifactGlob( glob );
        rule.setFiles( Collections.singletonList( "@1" ) );

        addCompound( configuration, "install", "install-native", "install-jar" );

        {
            Repository repo = addRepo( configuration, "install-native", "jpp", "repo/native" );
            Stereotype stereotype = new Stereotype();
            stereotype.setType( "native" );
            repo.addStereotype( stereotype );
        }

        addRepo( configuration, "install-jar", "jpp", "repo/jar" );

        addRepo( configuration, "install-raw-pom", "flat", "repo/raw-pom" );

        addRepo( configuration, "install-effective-pom", "flat", "repo/effective-pom" );

        return configuration;
    }
}
