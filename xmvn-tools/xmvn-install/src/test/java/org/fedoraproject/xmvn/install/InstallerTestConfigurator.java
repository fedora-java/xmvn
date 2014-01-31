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
package org.fedoraproject.xmvn.install;

import java.util.Collections;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.ConfigurationMerger;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.config.Stereotype;
import org.fedoraproject.xmvn.config.impl.DefaultConfigurator;

/**
 * @author Mikolaj Izdebski
 */
@Named( "default" )
@Singleton
public class InstallerTestConfigurator
    extends DefaultConfigurator
{
    @Inject
    public InstallerTestConfigurator( ConfigurationMerger merger )
    {
        super( merger );
    }

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
        rule.setOptional( true );
        configuration.addArtifactManagement( rule );

        org.fedoraproject.xmvn.config.Artifact glob = new org.fedoraproject.xmvn.config.Artifact();
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
