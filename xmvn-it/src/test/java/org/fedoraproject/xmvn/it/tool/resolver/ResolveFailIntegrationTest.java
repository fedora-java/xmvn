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

import org.junit.jupiter.api.Test;

import org.fedoraproject.xmvn.it.tool.AbstractToolIntegrationTest;

/**
 * Integration tests for XMvn Resolver tool.
 * 
 * @author Mikolaj Izdebski
 */
public class ResolveFailIntegrationTest
    extends AbstractToolIntegrationTest
{
    @Test
    public void testResolveFail()
        throws Exception
    {
        assertEquals( 1, invokeTool( "xmvn-resolve", "foobar:xyzzy" ) );
        assertTrue( getStderr().anyMatch( s -> s.endsWith( "Unable to resolve artifact foobar:xyzzy:jar:SYSTEM" ) ) );
        assertFalse( getStdout().findAny().isPresent() );
    }
}
