/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.nio.file.Paths;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.tools.install.File;
import org.fedoraproject.xmvn.tools.install.JavaPackage;

/**
 * @author Michael Simacek
 */
public class PackageRegistryTest
{
    private InstallerSettings settings;

    private PackageRegistry registry;

    @Before
    public void setUpRegistry()
    {
        settings = new InstallerSettings();
        settings.setMetadataDir( "usr/share/maven-metadata" );
        registry = new PackageRegistry( settings, "test-package" );
    }

    @Test
    public void testDefaultPackage()
    {
        JavaPackage pkg1 = registry.getPackageById( null );
        JavaPackage pkg2 = registry.getPackageById( null );
        assertSame( pkg1, pkg2 );
        JavaPackage pkg3 = registry.getPackageById( "" );
        assertSame( pkg2, pkg3 );
        assertEquals( 1, registry.getPackages().size() );
    }

    @Test
    public void testMetadata()
        throws Exception
    {
        JavaPackage pkg = registry.getPackageById( null );
        Set<File> files = pkg.getFiles();
        assertEquals( 1, files.size() );
        File metadataFile = files.iterator().next();
        assertEquals( Paths.get( "usr/share/maven-metadata/test-package.xml" ), metadataFile.getTargetPath() );
        assertNotNull( pkg.getMetadata() );
    }

    @Test
    public void testNonDefault()
        throws Exception
    {
        JavaPackage pkg = registry.getPackageById( "subpackage" );
        Set<File> files = pkg.getFiles();
        assertEquals( 1, files.size() );
        File metadataFile = files.iterator().next();
        assertEquals( Paths.get( "usr/share/maven-metadata/test-package-subpackage.xml" ), metadataFile.getTargetPath() );
    }

    @Test
    public void testMultiple()
        throws Exception
    {
        registry.getPackageById( null );
        registry.getPackageById( "subpackage" );
        assertEquals( 2, registry.getPackages().size() );
        assertNull( registry.getPackageById( "__noinstall" ) );
        assertEquals( 2, registry.getPackages().size() );
    }
}
