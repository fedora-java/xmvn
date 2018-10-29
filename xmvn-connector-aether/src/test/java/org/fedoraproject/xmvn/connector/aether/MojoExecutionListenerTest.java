/*-
 * Copyright (c) 2016-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.aether;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * @author Roman Vais
 */
@RunWith( EasyMockRunner.class )
public class MojoExecutionListenerTest
{
    private interface MojoBeanProperty
    {
        public String getReportOutputDirectory();

        public String getOutputDir();

        public String getSource();

        public String getTarget();

        public String getSourceLevel();

        public String getTargetLevel();

        // "getReportOutputDirectory", "getOutputDir", "getSource", "getTarget", "getSourceLevel", "getTargetLevel"
    }

    private XMvnMojoExecutionListener listener;

    @Mock( type = MockType.STRICT )
    // MojoBeanProperty interface extends Mojo interface, so this is ok (and required).
    private MojoBeanProperty mojo;

    @Mock( type = MockType.STRICT )
    private MojoExecution exec;

    @Mock( type = MockType.STRICT )
    private MavenProject project;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setUp()
        throws Exception
    {
        listener = new XMvnMojoExecutionListener();

        listener.setXmvnStateDir( tempDir.getRoot().toPath() );

        // MojoBeanProperty interface extends Mojo interface, so this is ok (and required).
        mojo = EasyMock.createMock( MojoBeanProperty.class );
        exec = EasyMock.createMock( MojoExecution.class );
        project = EasyMock.createMock( MavenProject.class );

        EasyMock.replay( mojo, exec, project );
    }

    @Test
    public void testListenerBeforeExecution()
        throws Exception
    {
        // tests javadoc aggregate
        EasyMock.reset( exec );
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.apache.maven.plugins" ).once();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "maven-javadoc-plugin" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "aggregate" ).once();
        EasyMock.replay( exec );

        listener.beforeMojoExecution( mojo, exec );
        EasyMock.verify( exec );

        // tests xmvn build deep
        EasyMock.reset( exec );
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.fedoraproject.xmvn" ).times( 2 );
        EasyMock.expect( exec.getArtifactId() ).andReturn( "xmvn-mojo" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "builddep" ).once();
        EasyMock.replay( exec );

        listener.beforeMojoExecution( mojo, exec );
        EasyMock.verify( exec );

        // tests nonexistent
        EasyMock.reset( exec );
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.example" ).times( 2 );
        EasyMock.expect( exec.getArtifactId() ).andReturn( "nonexistent" ).anyTimes();
        EasyMock.expect( exec.getGoal() ).andReturn( "builddep" ).anyTimes();
        EasyMock.replay( exec );

        listener.beforeMojoExecution( mojo, exec );
        EasyMock.verify( mojo, exec );
    }

    @Test
    public void testListenerAfterSuccess()
        throws Exception
    {
        // tests JAVADOC_AGGREGATE
        EasyMock.reset( exec, mojo );
        EasyMock.expect( mojo.getReportOutputDirectory() ).andReturn( "/tmp/foo/bar" ).once();
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.apache.maven.plugins" ).once();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "maven-javadoc-plugin" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "aggregate" ).once();
        EasyMock.replay( exec, mojo );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( exec );

        // tests XMVN_BUILDDEP
        EasyMock.reset( exec );
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.fedoraproject.xmvn" ).atLeastOnce();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "xmvn-mojo" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "builddep" ).once();
        EasyMock.replay( exec );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( exec );

        // tests XMVN_JAVADOC
        EasyMock.reset( exec, mojo );
        // EasyMock.expect( mojo.getReportOutputDirectory() ).andReturn( "/tmp/foo/bar/test.log" ).once();
        EasyMock.expect( mojo.getOutputDir() ).andReturn( "/tmp/foo/bar" ).once();
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.fedoraproject.xmvn" ).atLeastOnce();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "xmvn-mojo" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "javadoc" ).once();
        EasyMock.replay( exec, mojo );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( exec );

        // tests MAVEN_COMPILE
        EasyMock.reset( exec, mojo, project );
        EasyMock.expect( mojo.getSource() ).andReturn( "foo" ).once();
        EasyMock.expect( mojo.getTarget() ).andReturn( "bar" ).once();
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.apache.maven.plugins" ).atLeastOnce();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "maven-compiler-plugin" ).times( 2 );
        EasyMock.expect( exec.getGoal() ).andReturn( "compile" ).once();
        EasyMock.expect( project.getGroupId() ).andReturn( "xmvn.test" ).times( 2 );
        EasyMock.expect( project.getArtifactId() ).andReturn( "just-test" ).times( 2 );
        EasyMock.expect( project.getVersion() ).andReturn( "1.0.0" ).times( 2 );
        EasyMock.replay( exec, mojo, project );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( exec );

        // tests TYCHO_COMPILE
        EasyMock.reset( exec, mojo, project );
        EasyMock.expect( mojo.getSourceLevel() ).andReturn( "deep" ).once();
        EasyMock.expect( mojo.getTargetLevel() ).andReturn( "deeper" ).once();
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.eclipse.tycho" ).atLeastOnce();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "tycho-compiler-plugin" ).once();
        EasyMock.expect( exec.getGoal() ).andReturn( "compile" ).once();
        EasyMock.expect( project.getGroupId() ).andReturn( "xmvn.test" ).times( 2 );
        EasyMock.expect( project.getArtifactId() ).andReturn( "just-test" ).times( 2 );
        EasyMock.expect( project.getVersion() ).andReturn( "1.0.0" ).times( 2 );
        EasyMock.replay( exec, mojo, project );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( exec );

        // tests nonexistent
        EasyMock.reset( exec );
        EasyMock.expect( exec.getGroupId() ).andReturn( "org.example" ).atLeastOnce();
        EasyMock.expect( exec.getArtifactId() ).andReturn( "nonexistent" ).anyTimes();
        EasyMock.expect( exec.getGoal() ).andReturn( "builddep" ).anyTimes();
        EasyMock.replay( exec );

        listener.afterMojoExecution( mojo, exec, project );
        EasyMock.verify( mojo, exec );
    }
}
