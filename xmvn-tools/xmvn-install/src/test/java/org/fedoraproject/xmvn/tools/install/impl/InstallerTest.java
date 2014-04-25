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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fedoraproject.xmvn.tools.install.impl.InstallationPlanLoader.prepareInstallationPlanFile;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import com.google.inject.Binder;
import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.fedoraproject.xmvn.tools.install.Installer;

/**
 * @author Michael Simacek
 */
public class InstallerTest
        extends AbstractFileTest
{
    private final Configuration config = new Configuration();

    @Inject
    private Installer installer;

    private final Configurator configuratorMock = createMock( Configurator.class );
    private final Resolver resolverMock = createMock( Resolver.class );
    private final ResolutionResult resolutionResultMock = createNiceMock( ResolutionResult.class );

    @Before
    public void setUpSettings()
    {
        InstallerSettings settings = new InstallerSettings();
        settings.setMetadataDir( "usr/share/maven-metadata" );
        config.setInstallerSettings( settings );
    }

    private final ResolutionRequest request1 = new ResolutionRequest( "org.apache.lucene", "lucene-benchmark", "4.1", "jar" );
    private final ResolutionRequest request2 = new ResolutionRequest( "org.apache.lucene", "lucene-benchmark", "SYSTEM", "jar" );
    private final ResolutionRequest request3 = new ResolutionRequest( "org.apache.lucene", "lucene-spatial", "4.1", "jar" );
    private final ResolutionRequest request4 = new ResolutionRequest( "org.apache.lucene", "lucene-spatial", "SYSTEM", "jar" );

    class MockArtifactInstaller
            implements ArtifactInstaller
    {
        @Override
        public void install( JavaPackage targetPackage, ArtifactMetadata artifactMetadata, PackagingRule packagingRule )
                throws ArtifactInstallationException
        {
            Path path = Paths.get( "usr/share/java/" + artifactMetadata.getArtifactId() + ".jar" );
            File file = new RegularFile( path, Paths.get( artifactMetadata.getPath() ) );
            targetPackage.addFile( file );
            targetPackage.getMetadata().addArtifact( artifactMetadata );
        }
    }

    @Override
    public void configure( Binder binder )
    {
        binder.bind( Configurator.class ).toInstance( configuratorMock );
        binder.bind( ArtifactInstaller.class ).toInstance( new MockArtifactInstaller() );
        binder.bind( Resolver.class ).toInstance( resolverMock );
    }

    @Test
    public void testInstall()
            throws Exception
    {
        expect( configuratorMock.getConfiguration() ).andReturn( config );
        expect( resolverMock.resolve( request1 ) ).andReturn( resolutionResultMock );
        expect( resolverMock.resolve( request2 ) ).andReturn( resolutionResultMock );
        expect( resolverMock.resolve( request3 ) ).andReturn( resolutionResultMock );
        expect( resolverMock.resolve( request4 ) ).andReturn( resolutionResultMock );
        replay( resolverMock );
        replay( configuratorMock );
        replay( resolutionResultMock );

        InstallationRequest request = new InstallationRequest();
        request.setBasePackageName( "test-pkg" );
        request.setInstallRoot( installRoot );
        request.setInstallationPlan( prepareInstallationPlanFile( "valid.xml" ) );

        installer.install( request );

        verify( configuratorMock );
        verify( resolverMock );

        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "D /usr/share/maven-metadata",
                "F /usr/share/java/test.jar", "F /usr/share/java/test2.jar", "F /usr/share/maven-metadata/test-pkg.xml" );
        assertDescriptorEquals( Paths.get( ".mfiles" ), "%attr(0644,root,root) /usr/share/maven-metadata/test-pkg.xml",
                "%attr(0644,root,root) /usr/share/java/test.jar", "%attr(0644,root,root) /usr/share/java/test2.jar" );

        assertMetadataEqual( getResource( "test-pkg.xml" ), installRoot.resolve( "usr/share/maven-metadata/test-pkg.xml" ) );
    }
}
