/*-
 * Copyright (c) 2015-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.maven.mojo.builddep;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Integration tests for builddep MOJO.
 * 
 * @author Mikolaj Izdebski
 */
public class PackagingBuilddepIntegrationTest
    extends AbstractBuilddepIntegrationTest
{
    @Test
    public void testBuilddepPackaging()
        throws Exception
    {
        assumeTrue( Files.isExecutable( Paths.get( "/usr/bin/gcc" ) ),
                    "native-maven-plugin requires a C compiler to work" );
        expectBuildDependency( "org.codehaus.mojo", "native-maven-plugin" );
        expectBuildDependency( "junit", "junit" );
        performBuilddepTest();
    }
}
