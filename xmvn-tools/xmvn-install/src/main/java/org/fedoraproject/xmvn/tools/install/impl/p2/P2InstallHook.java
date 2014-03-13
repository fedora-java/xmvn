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
package org.fedoraproject.xmvn.tools.install.impl.p2;

import static org.fedoraproject.xmvn.tools.install.impl.JarUtils.containsNativeCode;
import static org.fedoraproject.xmvn.tools.install.impl.JarUtils.usesNativeCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fedoraproject.xmvn.tools.install.impl.Package;
import org.fedoraproject.xmvn.tools.install.impl.PackagePreInstallHook;

/**
 * @author Mikolaj Izdebski
 */
class P2InstallHook
    implements PackagePreInstallHook
{
    private static String ARCH_DEP_DROPIN_DIR = "usr/lib64/eclipse/dropins";

    private static String ARCH_INDEP_DROPIN_DIR = "usr/share/eclipse/dropins";

    private final P2RepoDescriptor request;

    public P2InstallHook( P2RepoDescriptor request )
    {
        this.request = request;
    }

    /**
     * Determine base path of Eclipse dropin directory depending on whether this subpackage is arch-dependant or not.
     * 
     * @return base path of Eclipse dropin directory
     * @throws IOException
     */
    private Path getDropinBasePath( P2RepoDescriptor request )
        throws IOException
    {
        Set<Path> bundles = new LinkedHashSet<>();
        bundles.addAll( request.getPlugins() );
        bundles.addAll( request.getFeatures() );

        for ( Path bundle : bundles )
        {
            if ( containsNativeCode( bundle ) || usesNativeCode( bundle ) )
                return Paths.get( ARCH_DEP_DROPIN_DIR );
        }

        return Paths.get( ARCH_INDEP_DROPIN_DIR );
    }

    private void symlinkBundles( Path baseRepo, String subdir, Collection<Path> bundles )
        throws IOException
    {
        Path targetRepo = baseRepo.resolve( subdir );
        Files.createDirectories( targetRepo );

        int index = 0;
        for ( Path bundle : bundles )
        {
            String bundleName = "bundle_" + ++index + ".jar";
            Files.createSymbolicLink( targetRepo.resolve( bundleName ), bundle.toAbsolutePath() );
        }
    }

    @Override
    public void beforePackageInstallation( Package pkg )
        throws IOException
    {
        Path tempRepo1 = Files.createTempDirectory( "xmvn-p2-repo1-" );
        Path tempRepo2 = Files.createTempDirectory( "xmvn-p2-repo2-" );
        Path tempRepo3 = Files.createTempDirectory( "xmvn-p2-repo3-" );

        symlinkBundles( tempRepo1, "plugins", request.getPlugins() );
        symlinkBundles( tempRepo1, "features", request.getFeatures() );

        String repoName = "P2 repository " + request.getRepoId();

        P2Director director = new P2Director();
        director.publishArtifacts( tempRepo1, tempRepo2, repoName );
        director.repo2runnable( tempRepo2, tempRepo3 );

        Files.delete( tempRepo3.resolve( "artifacts.jar" ) );
        Files.delete( tempRepo3.resolve( "content.jar" ) );

        Path dropinBasePath = getDropinBasePath( request );
        Path targetRepo = dropinBasePath.resolve( request.getRepoId() ).resolve( "eclipse" );
        Files.walkFileTree( tempRepo3, new FileInstallationVisitor( pkg, tempRepo3, targetRepo ) );
    }
}
