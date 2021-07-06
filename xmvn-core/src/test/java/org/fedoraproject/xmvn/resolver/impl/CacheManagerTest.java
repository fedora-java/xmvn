/*-
 * Copyright (c) 2013-2021 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class CacheManagerTest
{
    @Test
    public void testHashing()
    {
        CacheManager mgr = new CacheManager();
        assertEquals( "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08",
                      mgr.hash( "test".getBytes( StandardCharsets.US_ASCII ) ) );
    }

    @Test
    /**
     * Test case when the first hex letter is a zero.
     */
    public void testHashing2()
    {
        CacheManager mgr = new CacheManager();
        assertEquals( "033C0C34DCC7390311EF0D2CECF963B42A9C6E19D798117A66AF811FB0040A45",
                      mgr.hash( "TEST4".getBytes( StandardCharsets.US_ASCII ) ) );
    }
}
