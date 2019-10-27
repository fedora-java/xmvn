/*-
 * Copyright (c) 2013-2019 Red Hat, Inc.
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
        assertEquals( "A94A8FE5CCB19BA61C4C0873D391E987982FBBD3",
                      mgr.hash( "test".getBytes( StandardCharsets.US_ASCII ) ) );
    }

    @Test
    public void testHashing2()
    {
        CacheManager mgr = new CacheManager();
        assertEquals( "0AEC4D9BC52AB96E424CD057A59CC45EFF314107",
                      mgr.hash( "TEST2".getBytes( StandardCharsets.US_ASCII ) ) );
    }
}
