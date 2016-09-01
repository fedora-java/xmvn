/*-
 * Copyright (c) 2014-2016 Red Hat, Inc.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.sisu.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class ArtifactInstallerFactory
{
    private final Logger logger = LoggerFactory.getLogger( ArtifactInstallerFactory.class );

    @Inject
    private ArtifactInstaller defaultArtifactInstaller;

    @Inject
    @Nullable
    @Named( "eclipse" )
    private ArtifactInstaller eclipseArtifactInstaller;

    /**
     * List of Tycho pacgkaging types.
     */
    private static final Collection<String> ECLIPSE_PACKAGING_TYPES = Arrays.asList( "eclipse-plugin", //
                                                                                     "eclipse-test-plugin", //
                                                                                     "eclipse-feature", //
                                                                                     "eclipse-repository", //
                                                                                     "eclipse-application", //
                                                                                     "eclipse-update-site", //
                                                                                     "eclipse-target-definition" );

    @SuppressWarnings( "unused" )
    public ArtifactInstaller getInstallerFor( Artifact artifact, Properties properties )
    {
        String type = properties.getProperty( "type" );
        if ( type != null && ECLIPSE_PACKAGING_TYPES.contains( type ) )
        {
            if ( eclipseArtifactInstaller != null )
                return eclipseArtifactInstaller;

            logger.error( "Unable to load XMvn P2 plugin, Eclipse artifact installation will be impossible" );
            throw new RuntimeException( "Unable to load XMvn P2 plugin" );
        }

        return defaultArtifactInstaller;
    }
}
