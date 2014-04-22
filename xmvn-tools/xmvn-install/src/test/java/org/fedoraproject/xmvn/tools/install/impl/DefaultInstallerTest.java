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
import java.nio.file.Path;
import java.nio.file.Paths;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.Configurator;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.resolver.ResolutionRequest;
import org.fedoraproject.xmvn.resolver.ResolutionResult;
import org.fedoraproject.xmvn.resolver.Resolver;
import org.fedoraproject.xmvn.tools.install.InstallationRequest;
import org.junit.Test;

/**
 *
 * @author Michael Simacek
 */
public class DefaultInstallerTest
        extends AbstractFileTest
{

    private static final Configuration config = new Configuration();

    static
    {
        InstallerSettings settings = new InstallerSettings();
        settings.setMetadataDir( "usr/share/maven-metadata" );
        config.setInstallerSettings( settings );
    }

    class MockConfigurator
            implements Configurator
    {

        @Override
        public Configuration getDefaultConfiguration()
        {
            return config;
        }

        @Override
        public Configuration getConfiguration()
        {
            return config;
        }

        @Override
        public void dumpConfiguration()
        {
        }
    }

    class MockArtifactInstaller
            implements ArtifactInstaller
    {
        @Override
        public void install( JavaPackage targetPackage, ArtifactMetadata artifactMetadata, PackagingRule packagingRule )
                throws ArtifactInstallationException
        {
            Path path = Paths.get( "usr/share/java/" + artifactMetadata.getArtifactId() );
            File file = new RegularFile( path, Paths.get( artifactMetadata.getPath() ) );
            targetPackage.addFile( file );
            targetPackage.getMetadata().addArtifact( artifactMetadata );
        }

    }

    class MockResolver
            implements Resolver
    {
        @Override
        public ResolutionResult resolve( ResolutionRequest request )
        {
            return new ResolutionResult()
            {
                @Override
                public Path getArtifactPath()
                {
                    return null;
                }

                @Override
                public String getProvider()
                {
                    return null;
                }

                @Override
                public String getCompatVersion()
                {
                    return null;
                }

                @Override
                public String getNamespace()
                {
                    return null;
                }
            };
        }
    }

    private final Injector injector = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Configurator.class ).toInstance( new MockConfigurator() );
            bind( ArtifactInstaller.class ).toInstance( new MockArtifactInstaller() );
            bind( Resolver.class ).toInstance( new MockResolver() );
        }
    } );

    @Test
    public void testInstall()
            throws Exception
    {
        InstallationRequest request = new InstallationRequest();
        request.setBasePackageName( "test-pkg" );
        request.setInstallRoot( workdir );
        request.setInstallationPlan( prepareInstallationPlanFile( "valid.xml" ) );
        DefaultInstaller installer = injector.getInstance( DefaultInstaller.class );
        installer.install( request );
    }
}
