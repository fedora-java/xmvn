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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
class P2Director
{
    private final EclipseApplication repo2runnable =
        new EclipseApplication( "org.eclipse.equinox.p2.repository.repo2runnable" );

    private final EclipseApplication featuresAndBundlesPublisher =
        new EclipseApplication( "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher" );

    public void publishArtifacts( Path source, Path target, String repoName )
    {
        List<String> args = new ArrayList<>();

        args.add( "-publishArtifacts" );

        args.add( "-source" );
        args.add( source.toAbsolutePath().toString() );

        args.add( "-artifactRepository" );
        args.add( "file:" + target.toAbsolutePath() );

        args.add( "-metadataRepository" );
        args.add( "file:" + target.toAbsolutePath() );

        args.add( "-artifactRepositoryName" );
        args.add( repoName );

        args.add( "-metadataRepositoryName" );
        args.add( repoName );

        args.add( "-compress" );

        featuresAndBundlesPublisher.run( args );
    }

    public void repo2runnable( Path source, Path target )
    {
        List<String> args = new ArrayList<>();

        args.add( "-flagAsRunnable" );

        args.add( "-source" );
        args.add( source.toAbsolutePath().toString() );

        args.add( "-destination" );
        args.add( target.toAbsolutePath().toString() );

        repo2runnable.run( args );
    }
}
