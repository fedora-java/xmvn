/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.Stereotype;

@Component( role = Repository.class, hint = "my-type" )
public class MyRepository
    implements Repository
{
    @Override
    public void configure( List<Stereotype> stereotypes, Properties properties, Xpp3Dom configuration )
    {
        assertNotNull( properties );
        assertNotNull( configuration );
        assertEquals( "bar", properties.get( "foo" ) );
        assertNull( properties.get( "baz" ) );
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact )
    {
        fail( "getPrimaryArtifactPath() was not expected to be called" );
        throw null;
    }

    @Override
    public RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType )
    {
        fail( "getPrimaryArtifactPath() was not expected to be called" );
        throw null;
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( Artifact artifact )
    {
        fail( "getArtifactPaths() was not expected to be called" );
        throw null;
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( Artifact artifact, boolean ignoreType )
    {
        fail( "getArtifactPaths() was not expected to be called" );
        throw null;
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifact )
    {
        fail( "getArtifactPaths() was not expected to be called" );
        throw null;
    }

    @Override
    public List<RepositoryPath> getArtifactPaths( List<Artifact> artifact, boolean ignoreType )
    {
        fail( "getArtifactPaths() was not expected to be called" );
        throw null;
    }

    @Override
    public String getNamespace()
    {
        fail( "getNamespace was not expected to be called" );
        throw null;
    }

    @Override
    public void setNamespace( String namespace )
    {
        fail( "setNamespace was not expected to be called" );
        throw null;
    }
}
