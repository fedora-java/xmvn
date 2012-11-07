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
 * Command used to verify model correctness and presence of all dependencies in system repository.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Command.class, hint = "prep" )
public class PrepCommand
    extends Command
{
    @Requirement
    private Logger logger;

    private boolean skipTests;

    private boolean debug;

    public PrepCommand()
    {
        super( "prep" );
    }

    @Override
    public Options getOptions()
    {
        Options options = new Options();
        options.addOption( "T", "skip-tests", false, "assume tests will be skipped during build" );
        options.addOption( "X", "debug", false, "display lots of debugging information" );
        return options;
    }

    private void parseOptions( CommandLine cli )
    {
        skipTests = cli.hasOption( "skip-tests" );
        debug = cli.hasOption( "debug" );
    }

    @Override
    public int execute( PlexusContainer container, CommandLine cli )
        throws Throwable
    {
        parseOptions( cli );

        if ( skipTests )
            System.setProperty( "maven.test.skip", "true" );

        MavenExecutor executor = new MavenExecutor();
        executor.setDebug( debug );

        logger.info( "Verifying project..." );
        executor.execute( "org.apache.maven.plugins:maven-dependency-plugin:resolve",
                          "org.apache.maven.plugins:maven-dependency-plugin:resolve-plugins" );

        SystemResolver.printInvolvedPackages();
        return 0;
    }

    @Override
    public String getDescription()
    {
        return "verify model correctness and dependency availability";
    }
}
