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
package org.apache.maven.cli;

import org.codehaus.plexus.classworlds.ClassWorld;

/**
 * This is XMvn specific class and it resides in {@code org.apache.maven} namespace only becuse it needs to access
 * package-private class {@code MavenCli.CliRequest}.
 * <p>
 * The only reason for existence of this class is exporting private Maven API, which in XMvn authors' opinion should be
 * public.
 * <p>
 * TODO: This should be worked with Maven upstream.
 * 
 * @author Mikolaj Izdebski
 */
public class CliRequest
    extends MavenCli.CliRequest
{
    public CliRequest( String[] args, ClassWorld classWorld )
    {
        super( args, classWorld );
    }
}
