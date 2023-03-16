/*-
 * Copyright (c) 2016-2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.it.tool.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;

/**
 * Integration tests for XMvn Resolver tool.
 * 
 * @author Mikolaj Izdebski
 */
public class ResolveOneIntegrationTest
    extends AbstractToolIntegrationTest
{
    @Test
    public void testResolveOne()
        throws Exception
    {
        assertEquals( 0, invokeTool( "xmvn-resolve", "junit:junit" ) );
        assertFalse( getStderr().findAny().isPresent() );

        List<String> out = getStdout().collect( Collectors.toList() );
        assertEquals( 1, out.size() );

        Path jar = Paths.get( out.iterator().next() );
        assertTrue( jar.endsWith( "src/test/resources/empty.jar" ) );
        assertTrue( Files.isRegularFile( jar ) );
    }
}
