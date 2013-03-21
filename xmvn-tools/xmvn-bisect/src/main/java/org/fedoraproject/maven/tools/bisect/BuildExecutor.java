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
package org.fedoraproject.maven.tools.bisect;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Executes Maven build and evaluates the results.
 * 
 * @author Mikolaj Izdebski
 */
public interface BuildExecutor
{
    /**
     * Execute Maven build and evaluate if is succeeded.
     * 
     * @param request invocation request to use to execute the build
     * @param logPath path to log file
     * @param verbose print logs to standard output
     * @return {@code true} iff the build succeeded
     * @throws MavenInvocationException if build failed due to an internal error
     */
    boolean executeBuild( InvocationRequest request, String logPath, boolean verbose )
        throws MavenInvocationException;
}
