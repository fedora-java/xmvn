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

/**
 * @author Mikolaj Izdebski
 */
public class PluginsAndExtensionsDependencyTest
    extends AbstractDependencyTest
{
    public PluginsAndExtensionsDependencyTest()
        throws Exception
    {
        super( "plugins-and-extensions.xml" );
    }

    @Override
    public void configureBuild()
    {
        expect( "plG1:plA1:plV1" );
        expect( "plG2:plA2:plV2" );
        expect( "pdpG:pdpA:pdpV" );
        expect( "pdpG-r:pdpA-r:pdpV-r" );
        expect( "pdpG-opt:pdpA-opt:pdpV-opt" );
        expect( "extG:extA:extV" );
    }
}
