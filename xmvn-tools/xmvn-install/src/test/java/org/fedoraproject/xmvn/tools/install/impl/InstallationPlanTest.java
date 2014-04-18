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

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Michael Simacek
 */
public class InstallationPlanTest
{
    private final Path resources = Paths.get( "src/test/resources/" ).toAbsolutePath();

    @Test
    public void testNonexistent()
            throws ArtifactInstallationException
    {
        InstallationPlan plan = new InstallationPlan( resources.resolve( "not-there" ) );
        assertTrue( plan.getArtifacts().isEmpty() );
    }

    @Test
    public void testValid()
            throws ArtifactInstallationException
    {
        InstallationPlan plan = new InstallationPlan( resources.resolve( "valid.xml" ) );
        assertFalse( plan.getArtifacts().isEmpty() );
        assertEquals( 2, plan.getArtifacts().size() );
        assertEquals( "test", plan.getArtifacts().get( 0 ).getArtifactId() );
        assertEquals( "test2", plan.getArtifacts().get( 1 ).getArtifactId() );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testUuid()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "uuid.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNamespace()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "namespace.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testAlias()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "alias.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testCompat()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "compat.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupId()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-gid.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactId()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-aid.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoVersion()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-version.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoFile()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-file.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonexistenFile()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "nonexistent-file.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonregularFile()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "nonregular-file.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonreadableFile()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "nonreadable-file.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactIdDep()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-aid-dep.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupIdDep()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-gid-dep.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoVersionDep()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-version-dep.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNamespaceDep()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "namespace-dep.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testResolvedVersionDep()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "resolved-version.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactIdExclusion()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-aid-excl.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupIdExclusion()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "no-gid-excl.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testSkipped()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "skipped.xml" ) );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testMetadataUuid()
            throws ArtifactInstallationException
    {
        new InstallationPlan( resources.resolve( "metadata-uuid.xml" ) );
    }
}
