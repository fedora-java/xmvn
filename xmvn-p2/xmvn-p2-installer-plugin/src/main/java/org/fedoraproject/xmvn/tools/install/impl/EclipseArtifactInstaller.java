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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.osgi.OSGiServiceLocator;
import org.fedoraproject.xmvn.p2.EclipseInstallationRequest;
import org.fedoraproject.xmvn.p2.EclipseInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named( "eclipse" )
@Singleton
public class EclipseArtifactInstaller
    implements ArtifactInstaller
{
    private final Logger logger = LoggerFactory.getLogger( EclipseArtifactInstaller.class );

    @Inject
    private OSGiServiceLocator equinox;

    private final EclipseInstallationRequest request = new EclipseInstallationRequest();

    private final Map<JavaPackage, String> pkgMap = new LinkedHashMap<>();

    @Override
    public void install( JavaPackage targetPackage, ArtifactMetadata am, PackagingRule rule, String basePackageName )
        throws ArtifactInstallationException
    {
        Path path = Paths.get( am.getPath() );

        String type = am.getProperties().getProperty( "type" );
        boolean isFeature = type.equals( "eclipse-feature" );
        if ( type.equals( "eclipse-plugin" ) )
            request.addPlugin( path );
        else if ( isFeature )
            request.addFeature( path );
        else
            return;

        Artifact artifact =
            new DefaultArtifact( am.getGroupId(), am.getArtifactId(), am.getExtension(), am.getClassifier(),
                                 am.getVersion() );
        logger.info( "Installing artifact {}", artifact );

        String commonId = basePackageName.replaceAll( "^eclipse-", "" );
        String subpackageId = targetPackage.getId().replaceAll( "^eclipse-", "" );
        if ( !subpackageId.startsWith( commonId ) )
            subpackageId = commonId + "-" + subpackageId;
        subpackageId = subpackageId.replaceAll( "-+$", "" );
        subpackageId = subpackageId.replaceAll( "^-+", "" );

        if ( isFeature || StringUtils.isNotEmpty( rule.getTargetPackage() ) )
        {
            String unitId = isFeature ? am.getArtifactId() + ".feature.group" : am.getArtifactId();
            request.addPackageMapping( unitId, subpackageId );
            pkgMap.put( targetPackage, subpackageId );
        }

        // FIXME: Set correct path
        am.setPath( "/dev/null" );
        targetPackage.getMetadata().addArtifact( am );
    }

    @Override
    public void postInstallation()
        throws ArtifactInstallationException
    {
        try
        {
            request.addPrefix( Paths.get( "/" ) );

            Path tempRoot = Files.createTempDirectory( "xmvn-root-" );
            request.setBuildRoot( tempRoot );
            Path dropinRoot = Paths.get( "usr/share/eclipse/dropins" );
            request.setTargetDropinDirectory( dropinRoot );

            EclipseInstaller installer = equinox.getService( EclipseInstaller.class );
            installer.performInstallation( request );

            for ( Entry<JavaPackage, String> entry : pkgMap.entrySet() )
            {
                JavaPackage pkg = entry.getKey();
                String id = entry.getValue();
                Path dropin = tempRoot.resolve( dropinRoot ).resolve( id );
                addAllFiles( pkg, dropin, tempRoot );
            }
        }
        catch ( Exception e )
        {
            throw new ArtifactInstallationException( "Unable to install Eclipse artifacts", e );
        }
    }

    private void addAllFiles( JavaPackage pkg, Path dropin, Path root )
        throws IOException
    {
        pkg.addFile( new Directory( root.relativize( dropin ) ) );

        if ( Files.isDirectory( dropin ) )
        {
            for ( Path path : Files.newDirectoryStream( dropin ) )
            {
                Path relativePath = root.relativize( path );
                if ( Files.isDirectory( path ) )
                    addAllFiles( pkg, path, root );
                else
                {
                    File f;

                    if ( Files.isSymbolicLink( path ) )
                        f = new SymbolicLink( relativePath, Files.readSymbolicLink( path ) );
                    else
                        f = new RegularFile( relativePath, path );
                    pkg.addFile( f );
                }
            }
        }
    }
}
