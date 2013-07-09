package org.fedoraproject.maven.repository;

import java.nio.file.Paths;

import org.codehaus.plexus.PlexusTestCase;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.model.Artifact;

public class FlatRepositoryTest
    extends PlexusTestCase
{
    public void testJppRepository()
        throws Exception
    {
        Configurator configurator = lookup( Configurator.class );
        Configuration configuration = configurator.getConfiguration();

        Repository repository = new Repository();
        repository.setId( "test123" );
        repository.setType( "flat" );
        configuration.addRepository( repository );

        RepositoryConfigurator repoConfigurator = lookup( RepositoryConfigurator.class );
        org.fedoraproject.maven.repository.Repository repo = repoConfigurator.configureRepository( "test123" );
        assertNotNull( repo );

        Artifact artifact = new Artifact( "JPP/foo.bar-bazz", "the-artifact", "1.2.3", "baz" );
        assertEquals( Paths.get( "JPP.foo.bar-bazz-the-artifact-1.2.3.baz" ), repo.getPrimaryArtifactPath( artifact ) );
        assertEquals( Paths.get( "JPP.foo.bar-bazz-the-artifact.baz" ),
                      repo.getPrimaryArtifactPath( artifact.clearVersion() ) );
    }
}
