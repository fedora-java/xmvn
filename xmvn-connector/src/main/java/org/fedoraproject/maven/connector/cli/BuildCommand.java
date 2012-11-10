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
package org.fedoraproject.maven.connector.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.fedoraproject.maven.connector.MavenExecutor;
import org.fedoraproject.maven.resolver.SystemResolver;

/**
 * Command used to build and package project.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Command.class, hint = "build" )
public class BuildCommand
    implements Command
{
    @Requirement
    private Logger logger;

    private boolean skipTests;

    private boolean skipJavadoc;

    private boolean debug;

    @Override
    public Options getOptions()
    {
        Options options = new Options();
        options.addOption( "T", "skip-tests", false, "skip compiling and running tests" );
        options.addOption( "J", "skip-javadoc", false, "skip generating API documentation" );
        options.addOption( "X", "debug", false, "display lots of debugging information" );
        return options;
    }

    private void parseOptions( CommandLine cli )
    {
        skipTests = cli.hasOption( "skip-tests" );
        skipJavadoc = cli.hasOption( "skip-javadoc" );
        debug = cli.hasOption( "debug" );
    }

    @Override
    public int execute( PlexusContainer container, CommandLine cli )
        throws Throwable
    {
        parseOptions( cli );

        String baseGoal = "verify";

        if ( skipTests )
        {
            baseGoal = "package";
            System.setProperty( "maven.test.skip", "true" );
        }

        MavenExecutor executor = new MavenExecutor();
        executor.setDebug( debug );

        logger.info( "Building project..." );
        executor.execute( baseGoal, "org.fedoraproject.xmvn:xmvn-mojo:install" );

        if ( !skipJavadoc )
        {
            logger.info( "Generating javadocs..." );
            executor.execute( "org.apache.maven.plugins:maven-javadoc-plugin:aggregate" );
        }

        logger.info( "Build finished SUCCESSFULLY" );

        SystemResolver.printInvolvedPackages();
        return 0;
    }
}
