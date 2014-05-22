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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.Binder;
import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.fedoraproject.xmvn.config.Artifact;
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
@RunWith( EasyMockRunner.class )
public class InstallerTest
    extends AbstractFileTest
{
    private final Configuration config = new Configuration();

    @Inject
    private Installer installer;

    private final Configurator configuratorMock = createMock( Configurator.class );

    private final Resolver resolverMock = createMock( Resolver.class );

    private final List<ResolutionResult> resolutionResults = new ArrayList<>();

    @Before
    public void setUpSettings()
    {
        InstallerSettings settings = new InstallerSettings();
        settings.setMetadataDir( "usr/share/maven-metadata" );
        config.setInstallerSettings( settings );
    }

    class MockArtifactInstaller
        implements ArtifactInstaller
    {
        @Override
        public void install( JavaPackage targetPackage, ArtifactMetadata artifactMetadata, PackagingRule packagingRule,
                             String basePackageName )
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

    private void addResolution( String coordinates, String compatVersion, String namespace, Path path )
    {
        String[] split = coordinates.split( ":" );
        ResolutionRequest request = new ResolutionRequest( split[0], split[1], split[2], split[3] );
        ResolutionResult result = createNiceMock( ResolutionResult.class );
        expect( result.getCompatVersion() ).andReturn( compatVersion ).anyTimes();
        expect( result.getNamespace() ).andReturn( namespace ).anyTimes();
        expect( result.getArtifactPath() ).andReturn( path ).anyTimes();
        replay( result );
        resolutionResults.add( result );
        expect( resolverMock.resolve( request ) ).andReturn( result );
    }

    private void addResolution( String coordinates, Path path )
    {
        addResolution( coordinates, null, null, path );
    }

    private void addResolution( String coordinates )
    {
        addResolution( coordinates, null, null, null );
    }

    private void install( String planName )
        throws Exception
    {
        expect( configuratorMock.getConfiguration() ).andReturn( config );
        replay( resolverMock, configuratorMock );

        InstallationRequest request = new InstallationRequest();
        request.setBasePackageName( "test-pkg" );
        request.setInstallRoot( installRoot );
        request.setInstallationPlan( prepareInstallationPlanFile( planName ) );

        installer.install( request );

        verify( resolverMock, configuratorMock );
        for ( ResolutionResult result : resolutionResults )
            verify( result );
    }

    private void addEmptyResolutions()
    {
        addResolution( "org.apache.lucene:lucene-benchmark:4.1:jar" );
        addResolution( "org.apache.lucene:lucene-benchmark:SYSTEM:jar" );
        addResolution( "org.apache.lucene:lucene-spatial:4.1:jar" );
        addResolution( "org.apache.lucene:lucene-spatial:SYSTEM:jar" );
    }

    @Test
    public void testInstall()
        throws Exception
    {
        addEmptyResolutions();

        install( "valid.xml" );

        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "D /usr/share/maven-metadata",
                                  "F /usr/share/java/test.jar", "F /usr/share/java/test2.jar",
                                  "F /usr/share/maven-metadata/test-pkg.xml" );
        assertDescriptorEquals( Paths.get( ".mfiles" ), "%attr(0644,root,root) /usr/share/maven-metadata/test-pkg.xml",
                                "%attr(0644,root,root) /usr/share/java/test.jar",
                                "%attr(0644,root,root) /usr/share/java/test2.jar" );

        assertMetadataEqual( getResource( "test-pkg.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/test-pkg.xml" ) );
    }

    @Test
    public void testResolution()
        throws Exception
    {
        Path dependencyJar = Paths.get( "/tmp/bla.jar" );
        addResolution( "org.apache.lucene:lucene-benchmark:4.1:jar" );
        addResolution( "org.apache.lucene:lucene-benchmark:SYSTEM:jar", "4", "ns", dependencyJar );
        addResolution( "org.apache.lucene:lucene-spatial:4.1:jar" );
        addResolution( "org.apache.lucene:lucene-spatial:SYSTEM:jar", dependencyJar );

        install( "valid.xml" );

        assertMetadataEqual( getResource( "test-pkg-resolved.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/test-pkg.xml" ) );
    }

    @Test
    public void testSubpackage()
        throws Exception
    {
        addEmptyResolutions();

        PackagingRule rule = new PackagingRule();
        Artifact subArtifact = new Artifact();
        subArtifact.setArtifactId( "test2" );
        rule.setArtifactGlob( subArtifact );
        rule.setTargetPackage( "subpackage" );
        config.addArtifactManagement( rule );

        install( "valid.xml" );

        assertDirectoryStructure( "D /usr", "D /usr/share", "D /usr/share/java", "D /usr/share/maven-metadata",
                                  "F /usr/share/java/test.jar", "F /usr/share/java/test2.jar",
                                  "F /usr/share/maven-metadata/test-pkg.xml",
                                  "F /usr/share/maven-metadata/subpackage.xml" );

        assertDescriptorEquals( Paths.get( ".mfiles" ), "%attr(0644,root,root) /usr/share/maven-metadata/test-pkg.xml",
                                "%attr(0644,root,root) /usr/share/java/test.jar" );
        assertDescriptorEquals( Paths.get( ".mfiles-subpackage" ),
                                "%attr(0644,root,root) /usr/share/maven-metadata/subpackage.xml",
                                "%attr(0644,root,root) /usr/share/java/test2.jar" );

        assertMetadataEqual( getResource( "test-pkg-main.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/test-pkg.xml" ) );
        assertMetadataEqual( getResource( "test-pkg-sub.xml" ),
                             installRoot.resolve( "usr/share/maven-metadata/subpackage.xml" ) );
    }
}
