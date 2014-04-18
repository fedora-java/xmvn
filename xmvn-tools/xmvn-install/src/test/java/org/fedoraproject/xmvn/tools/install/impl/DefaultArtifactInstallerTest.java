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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.repository.Repository;
import org.fedoraproject.xmvn.repository.RepositoryConfigurator;
import org.fedoraproject.xmvn.repository.RepositoryPath;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Michael Simacek
 */
public class DefaultArtifactInstallerTest
{
    class MockRepositoryConfigurator
            implements RepositoryConfigurator
    {

        @Override
        public Repository configureRepository( String repoId )
        {
            return configureRepository( repoId, null );
        }

        @Override
        public Repository configureRepository( String repoId, String namespace )
        {
            return new MockRepository();
        }

    }

    static class MockRepositoryPath
            implements RepositoryPath
    {
        private final Path path;
        private final Repository repository;

        public MockRepositoryPath( Path path, Repository repository )
        {
            this.path = path;
            this.repository = repository;
        }

        @Override
        public Path getPath()
        {
            return path;
        }

        @Override
        public Repository getRepository()
        {
            return repository;
        }
    }

    class MockRepository
            implements Repository
    {

        @Override
        public RepositoryPath getPrimaryArtifactPath( Artifact artifact )
        {
            return getPrimaryArtifactPath( artifact, false );
        }

        @Override
        public RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType )
        {
            String coordinates = artifact.getGroupId() + '-' + artifact.getArtifactId();
            return new MockRepositoryPath( Paths.get( coordinates ), this );
        }

        @Override
        public List<RepositoryPath> getArtifactPaths( Artifact artifact )
        {
            throw new UnsupportedOperationException( "Not supported" );
        }

        @Override
        public List<RepositoryPath> getArtifactPaths( Artifact artifact, boolean ignoreType )
        {
            throw new UnsupportedOperationException( "Not supported" );
        }

        @Override
        public List<RepositoryPath> getArtifactPaths( List<Artifact> artifact )
        {
            throw new UnsupportedOperationException( "Not supported" );
        }

        @Override
        public List<RepositoryPath> getArtifactPaths( List<Artifact> artifact, boolean ignoreType )
        {
            throw new UnsupportedOperationException( "Not supported" );
        }

        @Override
        public String getNamespace()
        {
            return "ns";
        }

    }

    private final Injector injector = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( RepositoryConfigurator.class ).toInstance( new MockRepositoryConfigurator() );
        }
    } );

    protected static Path workdir;
    private final Path resources = Paths.get( "src/test/resources" );

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        String testName = new Throwable().getStackTrace()[0].getClassName();
        Path workPath = Paths.get( "target" ).resolve( "test-work" );
        Files.createDirectories( workPath );
        workdir = Files.createTempDirectory( workPath, testName ).toAbsolutePath();
    }

    @Test
    public void testInstallation()
            throws Exception
    {
        ArtifactMetadata artifact = new ArtifactMetadata();
        artifact.setGroupId( "com.example" );
        artifact.setArtifactId( "test" );
        artifact.setVersion( "4.5" );
        artifact.setPath( resources.resolve( "example.jar" ).toString() );

        JavaPackage pkg = new JavaPackage( "test", Paths.get( "usr/share/maven-metadata/test.xml" ) );

        PackagingRule rule = new PackagingRule();

        DefaultArtifactInstaller installer = injector.getInstance( DefaultArtifactInstaller.class );
        installer.install( pkg, artifact, rule );

        PackageMetadata metadata = pkg.getMetadata();
        ArtifactMetadata actualArtifact = metadata.getArtifacts().get( 0 );
        assertEquals( "ns", actualArtifact.getNamespace() );
        assertNotNull( actualArtifact.getUuid() );
    }
}
