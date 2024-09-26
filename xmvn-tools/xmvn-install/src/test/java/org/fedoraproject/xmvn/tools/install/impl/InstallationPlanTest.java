/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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

import static org.fedoraproject.xmvn.tools.install.impl.InstallationPlanLoader.createInstallationPlan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.tools.install.ArtifactInstallationException;

/**
 * @author Michael Simacek
 */
public class InstallationPlanTest
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

    @Test
    public void testUuid()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "uuid.xml" ) );
    }

    @Test
    public void testNamespace()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "namespace.xml" ) );
    }

    @Test
    public void testAlias()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "alias.xml" ) );
    }

    @Test
    public void testCompat()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "compat.xml" ) );
    }

    @Test
    public void testNoGroupId()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-gid.xml" ) );
    }

    @Test
    public void testNoArtifactId()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-aid.xml" ) );
    }

    @Test
    public void testNoVersion()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-version.xml" ) );
    }

    @Test
    public void testNoFile()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-file.xml" ) );
    }

    @Test
    public void testNonexistenFile()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "nonexistent-file.xml" ) );
    }

    @Test
    public void testNonregularFile()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "nonregular-file.xml" ) );
    }

    @Test
    public void testNonreadableFile()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "nonreadable-file.xml" ) );
    }

    @Test
    public void testNoArtifactIdDep()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-aid-dep.xml" ) );
    }

    @Test
    public void testNoGroupIdDep()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-gid-dep.xml" ) );
    }

    @Test
    public void testNoVersionDep()
        throws Exception
    {
        InstallationPlan plan = createInstallationPlan( "no-version-dep.xml" );

        List<ArtifactMetadata> artifacts = plan.getArtifacts();
        assertEquals( 2, artifacts.size() );

        List<Dependency> dependencies = artifacts.get( 1 ).getDependencies();
        assertEquals( 2, dependencies.size() );

        assertEquals( "4.1", dependencies.get( 0 ).getRequestedVersion() );
        assertEquals( Artifact.DEFAULT_VERSION, dependencies.get( 1 ).getRequestedVersion() );
    }

    @Test
    public void testNamespaceDep()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "namespace-dep.xml" ) );
    }

    @Test
    public void testResolvedVersionDep()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "resolved-version.xml" ) );
    }

    @Test
    public void testNoArtifactIdExclusion()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-aid-excl.xml" ) );
    }

    @Test
    public void testNoGroupIdExclusion()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "no-gid-excl.xml" ) );
    }

    @Test
    public void testSkipped()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "skipped.xml" ) );
    }

    @Test
    public void testMetadataUuid()
        throws Exception
    {
        assertThrows( ArtifactInstallationException.class, //
                      () -> createInstallationPlan( "metadata-uuid.xml" ) );
    }
}
