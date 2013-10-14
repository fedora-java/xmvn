package org.fedoraproject.maven.installer;

import java.util.Collections;
import java.util.Iterator;

import org.codehaus.plexus.component.annotations.Component;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.InstallerSettings;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.impl.DefaultConfigurator;

/**
 * @author Mikolaj Izdebski
 */
@Component( role = Configurator.class )
public class InstallerTestConfigurator
    extends DefaultConfigurator
{
    private void addRepo( Configuration configuration, String id, String type, String root )
    {
        Iterator<Repository> it = configuration.getRepositories().iterator();
        while ( it.hasNext() )
            if ( it.next().getId().equals( id ) )
                it.remove();

        Repository repo = new Repository();
        repo.setId( id );
        repo.setType( type );
        repo.addProperty( "root", root );
        configuration.addRepository( repo );
    }

    @Override
    public Configuration getConfiguration()
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

        addRepo( configuration, "install", "jpp", "repo/jar" );
        addRepo( configuration, "install-raw-pom", "flat", "repo/raw-pom" );
        addRepo( configuration, "install-effective-pom", "flat", "repo/effective-pom" );

        return configuration;
    }
}
