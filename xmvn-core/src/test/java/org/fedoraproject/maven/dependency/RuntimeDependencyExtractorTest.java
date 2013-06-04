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
package org.fedoraproject.maven.dependency;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Mikolaj Izdebski
 */
public class RuntimeDependencyExtractorTest
    extends PlexusTestCase
{
    /**
     * Test if Plexus can load runtime dependency extractor component.
     * 
     * @throws Exception
     */
    public void testComponentLookup()
        throws Exception
    {
        DependencyExtractor extractor = lookup( DependencyExtractor.class, DependencyExtractor.RUNTIME );
        assertNotNull( extractor );
    }
}
