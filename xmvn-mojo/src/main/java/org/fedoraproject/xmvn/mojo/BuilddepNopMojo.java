/*-
 * Copyright (c) 2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import javax.inject.Inject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.fedoraproject.xmvn.logging.Logger;

/**
 * @author Mikolaj Izdebski
 */
@Deprecated
@Mojo(name = "builddep", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE)
public class BuilddepNopMojo extends AbstractMojo {

    private final Logger logger;

    @Inject
    public BuilddepNopMojo(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.warn("Legacy deprecated builddep MOJO called, it does nothing");
    }
}
