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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Michael Simacek
 */
public class InstallationPlanTest
    extends AbstractFileTest
{
    @Test
    public void testNonexistent()
        throws Exception
    {
        InstallationPlan plan = new InstallationPlan( Paths.get( "not-there" ) );
        assertTrue( plan.getArtifacts().isEmpty() );
    }

    @Test
    public void testValid()
        throws Exception
    {
        InstallationPlan plan = createInstallationPlan( "valid.xml" );
        assertFalse( plan.getArtifacts().isEmpty() );
        assertEquals( 2, plan.getArtifacts().size() );
        assertEquals( "test", plan.getArtifacts().get( 0 ).getArtifactId() );
        assertEquals( "test2", plan.getArtifacts().get( 1 ).getArtifactId() );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testUuid()
        throws Exception
    {
        createInstallationPlan( "uuid.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNamespace()
        throws Exception
    {
        createInstallationPlan( "namespace.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testAlias()
        throws Exception
    {
        createInstallationPlan( "alias.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testCompat()
        throws Exception
    {
        createInstallationPlan( "compat.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupId()
        throws Exception
    {
        createInstallationPlan( "no-gid.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactId()
        throws Exception
    {
        createInstallationPlan( "no-aid.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoVersion()
        throws Exception
    {
        createInstallationPlan( "no-version.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoFile()
        throws Exception
    {
        createInstallationPlan( "no-file.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonexistenFile()
        throws Exception
    {
        createInstallationPlan( "nonexistent-file.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonregularFile()
        throws Exception
    {
        createInstallationPlan( "nonregular-file.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNonreadableFile()
        throws Exception
    {
        createInstallationPlan( "nonreadable-file.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactIdDep()
        throws Exception
    {
        createInstallationPlan( "no-aid-dep.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupIdDep()
        throws Exception
    {
        createInstallationPlan( "no-gid-dep.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoVersionDep()
        throws Exception
    {
        createInstallationPlan( "no-version-dep.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNamespaceDep()
        throws Exception
    {
        createInstallationPlan( "namespace-dep.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testResolvedVersionDep()
        throws Exception
    {
        createInstallationPlan( "resolved-version.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoArtifactIdExclusion()
        throws Exception
    {
        createInstallationPlan( "no-aid-excl.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testNoGroupIdExclusion()
        throws Exception
    {
        createInstallationPlan( "no-gid-excl.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testSkipped()
        throws Exception
    {
        createInstallationPlan( "skipped.xml" );
    }

    @Test( expected = ArtifactInstallationException.class )
    public void testMetadataUuid()
        throws Exception
    {
        createInstallationPlan( "metadata-uuid.xml" );
    }
}
