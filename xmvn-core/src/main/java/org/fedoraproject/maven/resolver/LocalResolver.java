package org.fedoraproject.maven.resolver;

import java.io.File;

import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.repository.Layout;
import org.fedoraproject.maven.repository.Repository;
import org.fedoraproject.maven.repository.SingletonRepository;

public class LocalResolver
    extends AbstractResolver
{
    private final Repository repo = new SingletonRepository( new File( ".xm2" ), Layout.MAVEN );

    @Override
    public File resolve( Artifact artifact )
    {
        return repo.findArtifact( artifact );
    }
}
