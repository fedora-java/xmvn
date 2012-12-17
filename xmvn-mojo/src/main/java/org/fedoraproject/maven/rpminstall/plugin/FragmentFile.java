/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.fedoraproject.maven.Configuration;
import org.fedoraproject.maven.model.Artifact;
import org.fedoraproject.maven.resolver.DependencyMap;

public class FragmentFile
    extends DependencyMap
{
    private final Set<Artifact> dependencies = new TreeSet<>();

    private final Set<Artifact> develDependencies = new TreeSet<>();

    private BigDecimal javaVersionRequirement;

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty() && dependencies.isEmpty() && develDependencies.isEmpty()
            && javaVersionRequirement == null;
    }

    public void addDependency( Artifact artifact )
    {
        dependencies.add( artifact.clearVersionAndExtension() );
    }

    public void addDevelDependency( Artifact artifact )
    {
        develDependencies.add( artifact.clearVersionAndExtension() );
    }

    public void addDependency( String groupId, String artifactId )
    {
        addDependency( new Artifact( groupId, artifactId ) );
    }

    public void addDevelDependency( String groupId, String artifactId )
    {
        addDevelDependency( new Artifact( groupId, artifactId ) );
    }

    public void addJavaVersionRequirement( BigDecimal version )
    {
        if ( javaVersionRequirement == null || javaVersionRequirement.compareTo( version ) < 0 )
            javaVersionRequirement = version;
    }

    public void optimize()
    {
        Set<Artifact> versionlessArtifacts = new TreeSet<>();
        for ( Artifact artifact : mapping.keySet() )
            versionlessArtifacts.add( artifact.clearVersionAndExtension() );

        for ( Iterator<Artifact> iter = dependencies.iterator(); iter.hasNext(); )
        {
            Artifact dependency = iter.next();
            if ( versionlessArtifacts.contains( dependency ) )
                iter.remove();
        }

        for ( Iterator<Artifact> iter = develDependencies.iterator(); iter.hasNext(); )
        {
            Artifact dependency = iter.next();
            if ( versionlessArtifacts.contains( dependency ) )
                iter.remove();
        }
    }

    public void write( Path path, boolean writeDevel )
        throws IOException
    {
        try (PrintStream ps = new PrintStream( path.toFile() ))
        {
            ps.println( "<!-- This depmap file was generated by XMvn. -->" );
            ps.println( "<dependencyMap>" );

            if ( Configuration.providesSkipped() )
                ps.println( "<skipProvides/>" );

            if ( javaVersionRequirement != null && !Configuration.requiresSkipped() )
                ps.println( "  <requiresJava>" + javaVersionRequirement + "</requiresJava>" );

            for ( Artifact mavenArtifact : mapping.keySet() )
            {
                Artifact jppArtifact = mapping.get( mavenArtifact );

                ps.println( "  <dependency>" );
                ps.println( "    <maven>" );
                ps.println( "      <groupId>" + mavenArtifact.getGroupId() + "</groupId>" );
                ps.println( "      <artifactId>" + mavenArtifact.getArtifactId() + "</artifactId>" );
                ps.println( "      <version>" + mavenArtifact.getVersion() + "</version>" );
                ps.println( "    </maven>" );
                ps.println( "    <jpp>" );
                ps.println( "      <groupId>" + jppArtifact.getGroupId() + "</groupId>" );
                ps.println( "      <artifactId>" + jppArtifact.getArtifactId() + "</artifactId>" );
                ps.println( "      <version>" + jppArtifact.getVersion() + "</version>" );
                ps.println( "    </jpp>" );
                ps.println( "  </dependency>" );
            }

            if ( !Configuration.requiresSkipped() )
            {
                Set<Artifact> combinedDependencies = new TreeSet<>( dependencies );
                if ( writeDevel )
                    combinedDependencies.addAll( develDependencies );

                for ( Artifact dependency : combinedDependencies )
                {
                    ps.println( "  <autoRequires>" );
                    ps.println( "    <groupId>" + dependency.getGroupId() + "</groupId>" );
                    ps.println( "    <artifactId>" + dependency.getArtifactId() + "</artifactId>" );
                    ps.println( "  </autoRequires>" );
                }
            }

            ps.println( "</dependencyMap>" );
        }
    }
}
